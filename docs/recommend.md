# 阅趣阁 · 推荐系统设计

本文档描述阅趣阁推荐模块的整体架构、数据流、算法与落地方式。

## 1. 目标

- **主推荐位**：首页「为你推荐」，Top 10 图书
- **相关推荐**：图书详情「看过此书的人还看了」，Top 6
- **实时热榜**：首页「热门推荐」、数据大屏 Top 10（更新到分钟级）
- **冷启动**：无正反馈用户回退到实时热榜

## 2. 数据来源

| 数据          | 表 / Topic              | 作用                                 |
| ------------- | ----------------------- | ------------------------------------ |
| 正反馈        | `user_favorite`         | 收藏行为，ItemCF 的核心输入          |
| 隐式反馈      | `user_action_log`       | VIEW/READ/ORDER/PAY，打权重后入矩阵  |
| 订单成交      | `order_info` (status=1) | 强正反馈                             |
| 业务元数据    | `book`                  | 分类、热度、封面                     |
| 实时热度 Sink | Redis ZSet              | `yuequge:rt:hot:book` / `:hot:item`  |
| 实时事件流    | Redis List              | `yuequge:rt:events`                  |
| 离线结果      | `recommend_result`      | 线上查推荐时的主要来源               |

`user_action_log.action` 权重定义在 `UserActionService#weight`：
`VIEW=1 / READ=2 / FAVORITE=5 / COMMENT=4 / ORDER=6 / PAY=10`。

## 3. 架构总览

```
                ┌──────────────┐
                │ 前端埋点 / SDK│
                └──────┬───────┘
                       │  POST /api/actions + 业务接口内置埋点
                       ▼
┌───────────────────────────────────────┐
│  UserActionService（Spring Bean）     │
│  · 异步写 user_action_log             │
│  · 实时更新 Redis ZSet（本地等价）     │
│  · 推入最近事件 List                   │
└──────────┬──────────────────┬─────────┘
           │                  │
           ▼                  ▼
     user_action_log       Redis ZSet/List ── 大屏 & 首页实时热榜
     （MySQL / HDFS）        （HotRankService + 5min 衰减）
           │
           ├──► Flink 实时作业（见 docs/flink.md）
           │     · 滑动窗口聚合 → 覆写 Redis ZSet
           │     · 事件流写 Kafka Topic → 大屏 WebSocket
           │
           └──► Spark 离线作业（见 docs/spark.md）
                 · 每日 T+1 ItemCF / ALS
                 · 结果写回 recommend_result 表
```

## 4. 线上算法：Item-based CF

实现位置：`com.yuequge.service.RecommendService`

1. 读取 `user_favorite`，构造 `user -> Set<item>` 倒排表
2. 对每个用户的 item 集合两两共现，计数矩阵 `cooc[i][j]`
   - 用 `1 / log(1 + |N(u)|)` 惩罚活跃用户（IUF 变体）
3. 相似度归一：`sim(i,j) = cooc(i,j) / sqrt(|N(i)| * |N(j)|)` （余弦）
4. 为用户 `u` 推荐：遍历其已收藏 item 集合 `R(u)`，对每个 `i∈R(u)` 的邻居 `j∉R(u)` 累加 `sim(i,j)`，取 Top N
5. Top 20 写入 `recommend_result`，线上查询 `/api/recommend/my` 直接读表

### 调度

- 启动后 30s 首次构建
- 每 6h 重建一次（Spring `@Scheduled`）
- 管理员 `POST /api/admin/recommend/rebuild` 手动触发
- 线上真正用 Spark 时，该 `@Scheduled` 可关闭，由离线作业定时写入 `recommend_result`

## 5. 离线算法：Spark ALS

见 [spark.md](./spark.md)。作业产物写 `recommend_result`，算法列填 `ALS`。
Java 侧读取时按 `score desc` 排序，上层无需区分；可以通过 `algo` 字段做实验分流。

## 6. 实时：Flink 滑动窗口

见 [flink.md](./flink.md)。作业将 `user_action_log` 的事件按 `target_type + target_id` 分组，
5 分钟滑动、1 分钟步进聚合打分，覆写 Redis ZSet。

## 7. HTTP 接口

| 方法 | 路径                                            | 说明                       |
| ---- | ----------------------------------------------- | -------------------------- |
| GET  | `/api/recommend/my?n=10`                        | 登录用户个性化推荐         |
| GET  | `/api/public/recommend/similar/{bookId}?n=6`    | 相似书推荐                 |
| GET  | `/api/public/hot/realtime/books?n=8`            | 实时热榜 · 图书             |
| GET  | `/api/public/hot/realtime/items?n=6`            | 实时热榜 · 商品             |
| POST | `/api/admin/recommend/rebuild`                  | 管理员触发重建             |
| GET  | `/api/admin/recommend/stats`                    | 上一次重建的统计信息       |
| POST | `/api/actions`                                  | 通用埋点                   |

## 8. 冷启动策略

| 场景             | 策略                                                                |
| ---------------- | ------------------------------------------------------------------- |
| 未登录           | 接口层返回 401，前端展示实时热榜                                    |
| 登录但无收藏     | `RecommendService#recommendBooks` 回退 `HotRankService#hotBooks`    |
| 离线矩阵无此用户 | 实时算一次；若该用户 item 也无邻居，回退热榜                         |
| Redis 不可用     | `HotRankService` 自动 fallback 到 `book.heat desc` SQL               |

## 9. 评估与迭代（规划）

- 离线 A/B：ALS vs ItemCF，用 `precision@10 / recall@10 / nDCG@10` 在保留集评估
- 线上 A/B：`algo` 字段做分桶，观察点击率 / 转化率
- 多路召回：ItemCF + 内容标签（`book.type`）+ 实时热榜，排序侧用 LR/GBDT
