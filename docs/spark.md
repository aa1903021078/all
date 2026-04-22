# 阅趣阁 · Spark 离线作业设计

本文档描述阅趣阁的离线计算链路：每日 T+1 从 MySQL 采数，计算个性化推荐，回写 MySQL。

## 1. 作业清单

| 作业                     | 调度      | 输入                                 | 输出                                     |
| ------------------------ | --------- | ------------------------------------ | ---------------------------------------- |
| **ItemCFRecommendJob**   | 每日 03:00 | `user_favorite`, `user_action_log`   | `recommend_result` (`algo=ITEM_CF`)      |
| **ALSRecommendJob**      | 每日 04:00 | 同上 + `order_info`（强信号）         | `recommend_result` (`algo=ALS`)          |
| **DailyMetricsJob**      | 每日 00:15 | `user_action_log`, `order_info`      | `daily_metrics`（给 BI / 历史趋势）      |

> 当前仓库提供 Java 本地等价实现（`RecommendService`，每 6h 重建 ItemCF），便于无 Spark 环境下体验。

## 2. 数据准备

MySQL → Hive / HDFS：

```
sqoop import \
  --connect jdbc:mysql://... \
  --table user_favorite \
  --hive-import --hive-table ods.user_favorite_df
```

也可用 DataX / Flink CDC 做全量 + 增量同步。

## 3. ItemCFRecommendJob

### 3.1 PySpark 参考实现

```python
from pyspark.sql import SparkSession, functions as F
from pyspark.sql.types import *
from pyspark.sql.window import Window
import math

spark = SparkSession.builder.appName("ItemCFRecommend").getOrCreate()

fav = spark.table("ods.user_favorite_df").select("user_id", "book_id")
action = (spark.table("ods.user_action_log_df")
          .where(F.col("action").isin("READ", "ORDER", "PAY", "COMMENT"))
          .select("user_id", F.col("target_id").alias("book_id")))
reads = fav.unionByName(action).dropDuplicates(["user_id", "book_id"])

pop = reads.groupBy("book_id").agg(F.count("*").alias("cnt"))
user_items = reads.groupBy("user_id").agg(F.collect_set("book_id").alias("items"))

@F.udf(returnType=ArrayType(StructType([
    StructField("i", LongType()),
    StructField("j", LongType()),
    StructField("w", DoubleType())
])))
def pairs(items):
    n = len(items)
    if n < 2: return []
    w = 1.0 / math.log(1 + n)
    return [(a, b, w) for a in items for b in items if a != b]

cooc = (user_items.select(F.explode(pairs("items")).alias("p"))
        .select(F.col("p.i").alias("i"), F.col("p.j").alias("j"), F.col("p.w").alias("w"))
        .groupBy("i", "j").agg(F.sum("w").alias("cooc")))

sim = (cooc
       .join(pop.selectExpr("book_id as i", "cnt as popI"), "i")
       .join(pop.selectExpr("book_id as j", "cnt as popJ"), "j")
       .withColumn("sim", F.col("cooc") / F.sqrt(F.col("popI") * F.col("popJ")))
       .select("i", "j", "sim"))

w_neigh = Window.partitionBy("i").orderBy(F.col("sim").desc())
top_sim = sim.withColumn("rn", F.row_number().over(w_neigh)).where("rn <= 200").drop("rn")

user_items_df = reads.withColumnRenamed("book_id", "i")
recos = (user_items_df.join(top_sim, "i")
         .join(reads.withColumnRenamed("book_id", "j"), ["user_id", "j"], "left_anti")
         .groupBy("user_id", "j").agg(F.sum("sim").alias("score")))

w_top = Window.partitionBy("user_id").orderBy(F.col("score").desc())
top = recos.withColumn("rn", F.row_number().over(w_top)).where("rn <= 20").drop("rn")

(top.withColumnRenamed("j", "book_id")
    .withColumn("algo", F.lit("ITEM_CF"))
    .write.mode("overwrite")
    .jdbc(MYSQL_URL, "recommend_result", properties=MYSQL_PROPS))
```

### 3.2 对应本仓库的 Java 版本

`com.yuequge.service.RecommendService#rebuild` 使用完全一致的数学公式：

- 倒排表：`userToItems`
- 活跃用户惩罚：`1 / log(1 + |items|)`
- 余弦归一：`cooc / sqrt(popI * popJ)`
- 写 `recommend_result`（`deleteByUser` + `insert`，Spark 版本则是 `overwrite`）

线上查询层 **永远读同一张表**：`RecommendService#recommendBooks` 的缓存读路径对 Spark 产物和 Java 产物一视同仁。

## 4. ALSRecommendJob（Spark MLlib）

```python
from pyspark.ml.recommendation import ALS

ratings = reads.withColumn("rating", F.lit(1.0))
als = ALS(userCol="user_id", itemCol="book_id", ratingCol="rating",
          rank=32, maxIter=15, regParam=0.1,
          implicitPrefs=True, alpha=40.0, coldStartStrategy="drop")
model = als.fit(ratings)

user_recs = model.recommendForAllUsers(20)

(user_recs
  .select("user_id", F.explode("recommendations").alias("r"))
  .select("user_id", F.col("r.book_id").alias("book_id"), F.col("r.rating").alias("score"))
  .withColumn("algo", F.lit("ALS"))
  .write.mode("overwrite")
  .jdbc(MYSQL_URL, "recommend_result", properties=MYSQL_PROPS))
```

- **隐式反馈**：`implicitPrefs=True` 把 1 当作置信度，0 视为未知（适合收藏 / 阅读场景）
- **评估**：对训练集按 80/20 hold-out，指标 `RMSE / precision@10`
- 建议把 `recommend_result` 的唯一键改为 `(user_id, book_id, algo)`，让 ItemCF 与 ALS 可并存做 A/B

## 5. 部署

```
spark-submit \
  --master k8s://https://k8s-master \
  --deploy-mode cluster \
  --conf spark.executor.instances=4 \
  --conf spark.executor.memory=4g \
  --conf spark.kubernetes.container.image=yuequge/spark:3.5.0 \
  --conf spark.jars.packages=mysql:mysql-connector-java:8.0.33 \
  local:///opt/apps/item_cf_recommend.py
```

Airflow DAG：

```
daily_metrics >> item_cf_recommend
              >> als_recommend
```

## 6. 回写策略

- **全量覆写**：`DELETE FROM recommend_result WHERE algo=?` + `INSERT` （当前 Java 版等价做法）
- **上线**：写入成功后 flush Redis 缓存 `yuequge:recommend:user:{id}`，或由后端在查询时懒更新
- **回滚**：保留最近 7 天的 Spark 输出到 `recommend_result_history`，出问题可秒级回切

## 7. 和 Flink 实时链路的边界

| 场景                    | 数据来源              | 典型延迟 |
| ----------------------- | --------------------- | -------- |
| 「为你推荐」            | Spark / Java 离线矩阵 | T+1 / 6h |
| 「看过此书的人还看了」  | 同上，按 item 查相似  | T+1 / 6h |
| 首页「热门推荐」         | Flink 实时 ZSet       | 分钟级   |
| 大屏热榜                 | Flink 实时 ZSet       | 分钟级   |
| 大屏事件流               | Flink / Kafka         | 秒级     |

两条链路对上层透明：上层只认 `recommend_result` 表 + Redis Key 两个契约。

## 8. 切换清单

从本地 Java 模式升到 Spark 模式：

1. 把 `RecommendService#scheduledRebuild` 上的 `@Scheduled` 注释掉
2. 部署上述 Spark 作业 + Airflow DAG
3. 检查 `recommend_result` 写入是否按 `algo` 分区（升级唯一键如前述）
