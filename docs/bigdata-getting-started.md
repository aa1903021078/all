# 大数据框架零基础入门 · Flink & Spark

> 面向「没用过大数据框架」的同学。读完这份文档，你应该能：
>
> 1. 说清楚 Flink 和 Spark 各自是干什么的、本项目为什么要用它们
> 2. 在本机把两个框架跑起来，跑通一个 Demo
> 3. 知道本项目里有哪些作业、怎么提交、怎么看结果
> 4. 遇到问题知道去哪看日志、去哪查文档
>
> 本文档是「入门 + 操作手册」。作业的详细算法和设计请看 `docs/flink.md` 和 `docs/spark.md`。

---

## 0. 先搞清楚两个问题

### Q1：什么是「大数据框架」？

一句话：**把一台机器跑不下 / 跑不快的数据处理任务，自动切成很多小块，丢到一堆机器上并行跑，再把结果汇总回来**。

你平时写的 Java `for` 循环、MySQL `group by` 都是「单机计算」；当数据量大到单机内存装不下、或者业务要求「1 秒内必须出结果」时，就需要分布式计算框架。

### Q2：Flink 和 Spark 有什么区别？

| 维度         | Spark                     | Flink                          |
| ------------ | ------------------------- | ------------------------------ |
| 擅长场景     | **离线批处理**（T+1 跑数） | **实时流处理**（秒级 / 分钟级）|
| 核心数据模型 | RDD / DataFrame（有边界） | DataStream（无边界）           |
| 典型延迟     | 分钟 ~ 小时               | 毫秒 ~ 秒                      |
| 机器学习     | MLlib 成熟（ALS / 树模型）| 有但弱                         |
| 本项目用它做 | 每天凌晨算「为你推荐」     | 实时算「首页热榜」/ 大屏事件流 |

**简单记忆**：
- 「一晚上算完明天一天用」→ Spark
- 「秒级刷新、现在就要」→ Flink

本项目**两个都用**：离线推荐用 Spark，实时热榜用 Flink，各司其职。

---

## 1. Spark 入门

### 1.1 Spark 是什么

Spark 是一个**分布式计算引擎**。你写一段代码，它会帮你把数据切片（partition）、分发到多台机器、并行执行、容错重试、最后汇总。

核心概念 4 个就够你入门：

| 概念            | 类比                         | 说明                                           |
| --------------- | ---------------------------- | ---------------------------------------------- |
| **RDD**         | 一个超级大 List              | 弹性分布式数据集，底层概念，现在很少直接写     |
| **DataFrame**   | 一张 SQL 表                  | 带 schema 的 RDD，**日常用这个**               |
| **Transformation** | `map` / `filter` / `join` | **懒执行**，只是在记账，不真算                 |
| **Action**      | `count` / `show` / `write`   | 触发真正计算，**此时 Spark 才把任务派出去**    |

一个 Spark 程序的骨架：

```python
# 1. 起一个 SparkSession（相当于 JDBC 里的 Connection）
spark = SparkSession.builder.appName("demo").getOrCreate()

# 2. 读数据（从 MySQL / HDFS / Hive / CSV…）
df = spark.read.jdbc(url, "user_favorite", properties=props)

# 3. 变换（懒执行）
result = df.groupBy("book_id").count().orderBy(F.col("count").desc())

# 4. 触发计算 + 写出去
result.write.mode("overwrite").jdbc(url, "book_heat", properties=props)

spark.stop()
```

### 1.2 本地装一个 Spark 跑 Demo

**方式 A：pip 装 PySpark（最快，推荐入门）**

```bash
# 需要 Java 11 或 Java 17
java -version

pip install pyspark==3.5.1

# 跑一个最小 Demo
cat > /tmp/hello_spark.py <<'EOF'
from pyspark.sql import SparkSession
spark = SparkSession.builder.appName("hello").master("local[*]").getOrCreate()
df = spark.createDataFrame([(1, "三体"), (2, "活着"), (1, "三体")], ["uid", "book"])
df.groupBy("book").count().show()
spark.stop()
EOF

python /tmp/hello_spark.py
# 能看到 +----+-----+ | book|count| 就算成功
```

`master("local[*]")` 表示本地用所有 CPU 核，不需要任何集群。

**方式 B：Docker 起一个单机集群**

```bash
docker run -it --rm -p 4040:4040 apache/spark:3.5.1 /opt/spark/bin/spark-shell
```

浏览器打开 <http://localhost:4040> 能看到 Spark Web UI。

### 1.3 本项目的 Spark 作业清单

看 `docs/spark.md` 有完整代码，这里只说**每个作业干啥、什么时候跑、结果写到哪**：

| 作业                     | 调度      | 读什么                                      | 写到哪                                  | 给谁用                      |
| ------------------------ | --------- | ------------------------------------------- | --------------------------------------- | --------------------------- |
| `ItemCFRecommendJob`     | 每天 03:00 | `user_favorite` + `user_action_log`         | `recommend_result` (`algo=ITEM_CF`)     | 首页「为你推荐」            |
| `ALSRecommendJob`        | 每天 04:00 | 同上 + `order_info`                         | `recommend_result` (`algo=ALS`)         | 首页「为你推荐」（A/B 对照）|
| `DailyMetricsJob`        | 每天 00:15 | `user_action_log` + `order_info`            | `daily_metrics`                         | 后台数据大屏历史趋势        |

> **关键契约**：不管是本地 Java 版（`RecommendService`）还是 Spark 版，**写的都是同一张 `recommend_result` 表**。上层查询（`RecommendController`）根本不知道背后是谁算的，可以随时替换。

### 1.4 怎么把本项目的 Spark 作业跑起来

**第一步：准备数据源**

项目 MySQL 里要有这几张表（`zzx.sql` 已经建好）：

- `user_favorite`（谁收藏了哪本书）
- `user_action_log`（谁对哪本书做了什么动作）
- `order_info`（谁买了哪本书）
- `recommend_result`（推荐结果，作业要写这里）

**第二步：本机跑 PySpark 版本**

把 `docs/spark.md` 第 3.1 节的代码保存为 `/tmp/item_cf.py`，开头加上：

```python
MYSQL_URL = "jdbc:mysql://127.0.0.1:3306/yuequge?useSSL=false&serverTimezone=UTC"
MYSQL_PROPS = {"user": "root", "password": "your_pwd", "driver": "com.mysql.cj.jdbc.Driver"}
```

提交：

```bash
spark-submit \
  --master local[*] \
  --packages mysql:mysql-connector-java:8.0.33 \
  /tmp/item_cf.py
```

`--packages` 会自动下载 MySQL JDBC 驱动。跑完去数据库查：

```sql
SELECT * FROM recommend_result WHERE algo='ITEM_CF' LIMIT 10;
```

有数据就成功了。前端登录后刷新首页，「为你推荐」就会用上这份数据。

**第三步：上集群（可选）**

生产上 Spark 一般跑在 YARN / Kubernetes 上，调度器用 Airflow。参考 `docs/spark.md` 第 5、6 节的 `spark-submit` 命令和 Airflow DAG。入门阶段 `local[*]` 就够用。

### 1.5 Spark 排错速查

| 症状                                | 八成是                                 | 解决                                                      |
| ----------------------------------- | -------------------------------------- | --------------------------------------------------------- |
| `ClassNotFoundException: com.mysql` | 没带 JDBC 驱动                         | `--packages mysql:mysql-connector-java:8.0.33`            |
| 卡在 `stage 0` 不动                 | 数据倾斜 / 某个 key 数据特别多         | `repartition(200)` 或 `salt` 打散 key                     |
| `OutOfMemoryError`                  | executor 内存不够                      | `--conf spark.executor.memory=4g`                         |
| `SparkSession` 起不来               | Java 版本不对（要 Java 11 / 17）       | `java -version`，必要时 `export JAVA_HOME=...`            |
| Web UI 4040 打不开                  | 作业已经结束了                         | 用 `spark.stop()` 前 `input("press enter to exit")` 卡住   |

---

## 2. Flink 入门

### 2.1 Flink 是什么

Flink 是一个**分布式流处理引擎**。Spark 处理的数据默认是有边界的（一份文件、一张表），Flink 处理的数据默认是**无边界的**（一条永远流淌的消息河，比如 Kafka topic）。

核心概念 5 个：

| 概念              | 说明                                                        |
| ----------------- | ----------------------------------------------------------- |
| **DataStream**    | 无限流，Flink 的主角                                        |
| **Source / Sink** | 从哪读、往哪写（Kafka / MySQL CDC / Redis / Elasticsearch） |
| **Window**        | 把无限流切成一段段来算（滚动 / 滑动 / 会话）                |
| **Watermark**     | 「水位线」，告诉 Flink「比 X 早的数据不会再来了，可以出结果」|
| **State**         | 算子里记的小账本（比如每个 key 的计数），支持容错恢复       |

一个 Flink 程序的骨架（Java）：

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
env.enableCheckpointing(60_000); // 每 60 秒做一次快照，挂了能从快照恢复

DataStream<Event> stream = env
    .addSource(new FlinkKafkaConsumer<>("yuequge.actions", new JsonSchema(), props))
    .assignTimestampsAndWatermarks(
        WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ofSeconds(5))
            .withTimestampAssigner((e, ts) -> e.getTimestamp()));

stream.keyBy(Event::getBookId)
      .window(SlidingEventTimeWindows.of(Time.minutes(30), Time.minutes(1)))
      .aggregate(new WeightedSumAgg())
      .addSink(new RedisZSetSink("yuequge:rt:hot:book"));

env.execute("RealtimeHotRankJob");
```

读这段代码的思路：**每条事件按 bookId 分组 → 每分钟算一次最近 30 分钟的加权热度 → 写到 Redis 排行榜**。

### 2.2 两个最容易踩坑的概念

**（1）Event Time vs Processing Time**

- *Processing Time*：「机器现在几点」—— 简单但不准，比如一条迟到 1 小时的消息会被算到「现在」
- *Event Time*：「事件本身发生在几点」—— 准确，但需要 Watermark 告诉系统「可以出结果了」

**生产作业一律用 Event Time**，本项目也是。

**（2）Window = 攒一段时间再算**

- *Tumbling 滚动窗口*：每 5 分钟一个窗口，互不重叠 → 适合「每 5 分钟的 UV」
- *Sliding 滑动窗口*：窗口大小 30 分钟、步长 1 分钟 → 适合「最近 30 分钟的热度，每分钟刷新」，**本项目热榜用这个**
- *Session 会话窗口*：用户静默超过 N 分钟就切一个窗口 → 适合用户行为分析

### 2.3 本地装一个 Flink 跑 Demo

**方式 A：下载 tarball**

```bash
# 需要 Java 11
wget https://archive.apache.org/dist/flink/flink-1.18.1/flink-1.18.1-bin-scala_2.12.tgz
tar -xzf flink-1.18.1-bin-scala_2.12.tgz
cd flink-1.18.1

# 起一个本地集群（1 JobManager + 1 TaskManager）
./bin/start-cluster.sh

# 跑官方 WordCount 示例
./bin/flink run examples/streaming/WordCount.jar

# 停掉
./bin/stop-cluster.sh
```

浏览器打开 <http://localhost:8081> 就是 Flink Web UI，能看到作业、Checkpoint、背压情况。

**方式 B：Docker**

```bash
docker run --rm -p 8081:8081 flink:1.18.1 jobmanager
```

### 2.4 本项目的 Flink 作业清单

看 `docs/flink.md` 有完整代码，这里只说**每个作业干啥、读什么、写到哪**：

| 作业                        | 输入                             | 输出                                                        | 给谁用                    |
| --------------------------- | -------------------------------- | ----------------------------------------------------------- | ------------------------- |
| `RealtimeHotRankJob`        | MySQL CDC `user_action_log`      | Redis ZSet `yuequge:rt:hot:book` / `yuequge:rt:hot:item`    | 首页热榜 / 大屏热榜       |
| `EventStreamBroadcastJob`   | 同上                             | Kafka topic `yuequge.events` → WebSocket `/topic/events`    | 大屏右下角「实时事件流」  |

> 和 Spark 一样，**本地 Java 版（`UserActionService` + `HotRankService`）写同样的 Redis Key**。生产切到 Flink 后上层零感知。

### 2.5 怎么把本项目的 Flink 作业跑起来

**最小可行方案**：先别急着上 Flink，本项目已经有**本地等价实现**（`UserActionService` + `HotRankService`），启动后端就能跑。你可以先把业务链路跑通再迁移到 Flink。

**真上 Flink 的步骤**（参考 `docs/flink.md` §8）：

1. **开 MySQL binlog**：`my.cnf` 加 `log-bin=mysql-bin` + `binlog_format=ROW`，重启 MySQL
2. **部署 Flink 集群**：`./bin/start-cluster.sh` 本地跑；生产用 Flink Kubernetes Operator
3. **打包作业 jar**：把 `docs/flink.md` §3 的伪代码写成 Maven 项目，`mvn package` 产出 `yuequge-flink-job.jar`
4. **提交作业**：

   ```bash
   ./bin/flink run -c com.yuequge.flink.RealtimeHotRankJob yuequge-flink-job.jar \
     --mysql-host 127.0.0.1 --mysql-db yuequge \
     --redis-host 127.0.0.1
   ```
5. **后端切换**：把 `HotRankService#decay()` 的 `@Scheduled` 注释掉（Flink 已经做窗口覆写了）

作业跑起来后，登录后台 `/admin/bigscreen`，Top10 热榜柱状图就会秒级跳动。

### 2.6 Flink 排错速查

| 症状                            | 八成是                              | 解决                                                              |
| ------------------------------- | ----------------------------------- | ----------------------------------------------------------------- |
| 作业提交后状态一直是 `CREATED`  | TaskManager 没起来 / slot 不够      | 看 Web UI「Task Managers」页；调大 `taskmanager.numberOfTaskSlots` |
| Checkpoint 一直失败             | 状态后端配的 HDFS 路径没权限        | 先用 `state.backend: filesystem` + `file:///tmp/flink-checkpoints` 本地试 |
| 结果迟迟不出来                  | Watermark 没推进（没数据 / 数据乱序太大） | 临时改成 Processing Time 验证，或调大乱序容忍度                  |
| 数据重复 / 丢失                 | Sink 不是幂等的                     | Redis ZSet 用 `del + zadd` 覆写；Kafka 开 `EXACTLY_ONCE`         |
| 背压严重（Web UI 变红）         | 下游处理慢 / sink 写太慢             | 看 Web UI「Backpressure」定位算子；加并行度 / 批量写             |

---

## 3. 本项目「本地版 vs 大数据版」对照

| 链路         | 本地 Java 版（已实现）                                     | 大数据版（可选升级）                             |
| ------------ | ---------------------------------------------------------- | ------------------------------------------------ |
| 行为采集     | `UserActionService.track(...)` 写 MySQL + Redis            | 业务发 Kafka / MySQL CDC                         |
| 实时热榜     | `HotRankService` Redis ZSet + 5min 指数衰减                | **Flink** 滑动窗口覆写同一 ZSet                  |
| 个性化推荐   | `RecommendService` 每 6h 重建 ItemCF，写 `recommend_result` | **Spark** ItemCF / ALS 每天重建同一张表           |
| 大屏事件流   | HTTP 10s 轮询 Redis List                                   | Flink → Kafka → WebSocket `/topic/events` 秒级推送 |

**好处**：两个版本**读侧接口完全相同**（同一张表、同一组 Redis Key），要升级只替换「写侧」，前端一行代码不用改。

---

## 4. 学习路线建议

如果你想系统学一下，按这个顺序：

1. **先跑通本项目的 Java 本地版**（现在就能跑），理解业务链路
2. **装 PySpark + 跑 `docs/spark.md` 的 ItemCF**（半天搞定），体会「批处理 + DataFrame」
3. **装 Flink + 跑官方 WordCount**（1 小时），体会「流 + Window + Checkpoint」
4. **把 Flink WordCount 改成本项目的 `RealtimeHotRankJob`**（1-2 天）
5. 有余力再学 Kafka / MySQL CDC / Airflow，这些是「连接件」

### 官方资源

- Spark：<https://spark.apache.org/docs/latest/quick-start.html>
- PySpark：<https://spark.apache.org/docs/latest/api/python/getting_started/index.html>
- Flink：<https://nightlies.apache.org/flink/flink-docs-stable/docs/try-flink/local_installation/>
- Flink 中文：<https://nightlies.apache.org/flink/flink-docs-stable/zh/>

### 推荐书 / 视频

- 《Spark 快速大数据分析》—— 入门够用
- 《Flink 基础教程》（Fabian Hueske）—— 官方作者写的小册子，100 多页
- B 站搜「尚硅谷 Flink」/「尚硅谷 Spark」有完整视频课

---

## 5. FAQ

**Q：我可以不用这两个框架吗？**
A：可以。本项目 Java 本地版已经能完整跑业务。只有当数据量大到单机扛不住、或业务要求秒级延迟时，才值得引入。

**Q：两个都要部署吗？**
A：不一定。推荐优先级：**Spark（离线推荐）> Flink（实时热榜）**。Spark 作业一天只跑一次、资源好规划；Flink 需要 7×24 在线、运维成本更高。

**Q：本地开发机能跑动吗？**
A：能。PySpark `local[*]` 模式 4G 内存够；Flink 单机集群 2G 够。生产才需要集群。

**Q：数据量多大才值得上？**
A：经验值——
- MySQL 单表超过 1 亿行做 `group by` 开始明显变慢 → 考虑 Spark
- 业务要求「热榜 1 分钟内刷新」且事件量 > 100 QPS → 考虑 Flink

**Q：Flink 和 Kafka 是什么关系？**
A：Kafka 是**消息队列**（管道），Flink 是**计算引擎**（处理消息的人）。典型组合是 Kafka 当 Source，Flink 消费并计算，再写回 Kafka / Redis / MySQL。

---

读完这份文档，你应该知道：

- 两个框架各自擅长什么、本项目为什么用它们
- 本机怎么装、怎么跑 Hello World
- 本项目里有哪些作业、怎么提交、结果写到哪
- 出问题去哪排查

具体作业的算法细节和部署参数，请继续看 `docs/flink.md` 和 `docs/spark.md`。
