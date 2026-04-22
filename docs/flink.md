# 阅趣阁 · Flink 实时作业设计

本文档描述阅趣阁的实时计算链路：如何把用户行为事件转换成「实时热榜」与「事件大屏流」。

## 1. 作业定位

| 作业                        | 输入                          | 输出                                             | 触发 |
| --------------------------- | ----------------------------- | ------------------------------------------------ | ---- |
| **RealtimeHotRankJob**      | `user_action_log` CDC 或 Kafka | Redis ZSet `yuequge:rt:hot:book/:item`           | 流式 |
| **EventStreamBroadcastJob** | 同上                          | Kafka `yuequge.events` → 后端 WebSocket 推送大屏 | 流式 |

> 当前仓库提供 Java 本地等价实现（`UserActionService` + `HotRankService`），便于无 Flink 环境下体验。
> 生产切换只需改 Sink 写入方，Redis 数据结构与 Key 不变。

## 2. 数据接入

### 方式 A：MySQL CDC（推荐）

```
MySQL binlog
  └─► Flink CDC connector (com.ververica:flink-connector-mysql-cdc)
        └─► DataStream<UserActionLog>
```

- 直接订阅 `yuequge.user_action_log` 的 insert 事件
- 优势：业务无改动，无丢失

### 方式 B：业务直发 Kafka

`UserActionService` 把事件写入 Kafka topic `yuequge.actions`：

```json
{
  "userId": 123, "targetType": "BOOK", "targetId": 7,
  "action": "READ", "ts": "2026-04-22T10:00:00Z"
}
```

Flink 用 `FlinkKafkaConsumer` 订阅。

## 3. 热榜聚合算法

- **窗口**：`SlidingEventTimeWindows.of(Time.minutes(30), Time.minutes(1))`
- **Watermark**：`forBoundedOutOfOrderness(Duration.ofSeconds(5))`
- **Key**：`targetType + "#" + targetId`
- **打分**：`weight(action)`（VIEW=1, READ=2, FAVORITE=5, COMMENT=4, ORDER=6, PAY=10）
- **Sink**：Redis ZSet，Key 为 `yuequge:rt:hot:book` / `yuequge:rt:hot:item`

### 伪代码

```java
DataStream<UserActionLog> src = ...;

src.assignTimestampsAndWatermarks(
        WatermarkStrategy.<UserActionLog>forBoundedOutOfOrderness(Duration.ofSeconds(5))
            .withTimestampAssigner((e, ts) -> e.getCreateTime().toEpochSecond(UTC) * 1000))
   .filter(e -> "BOOK".equals(e.getTargetType()) || "ITEM".equals(e.getTargetType()))
   .keyBy(e -> e.getTargetType() + "#" + e.getTargetId())
   .window(SlidingEventTimeWindows.of(Time.minutes(30), Time.minutes(1)))
   .aggregate(new WeightedSumAgg(), new TopNSink())
   .addSink(new RedisZSetSink("yuequge:rt:hot:book"));
```

其中：

- `WeightedSumAgg`：`add = acc + weight(action)`；`merge = a + b`
- `TopNSink` 按 `targetType` 聚合成 `(id -> score)` 并覆写 Redis ZSet：
  ```java
  jedis.del(key);
  for ((id, score) of top) jedis.zadd(key, score, id);
  jedis.expire(key, 7 * 24 * 3600);
  ```

### 为什么要窗口覆写而不是 incrementScore？

- 滑动窗口保证「最近 N 分钟热度」而不是历史累计
- 覆写一次即可让老数据自然淘汰，无需额外衰减任务
- 本地 Java 等价实现因无窗口机制，采用「incr + 每 5min 指数衰减」近似

## 4. 事件大屏流

同一条 `src` 流经 `map → toJson` 后写入 Kafka topic `yuequge.events`，
后端订阅该 topic 并通过 `/topic/events` STOMP 主题推送到大屏：

```
Flink → Kafka events → Spring listener → SimpMessagingTemplate → 大屏 ECharts
```

本地版本用 Redis List (`yuequge:rt:events`) 替代 Kafka，由 HTTP 轮询
`/api/admin/dashboard/recent-events` 获取。

## 5. 容错与 exactly-once

- Checkpoint：`env.enableCheckpointing(60_000)`，state backend `RocksDB`，`EXACTLY_ONCE` 对齐
- Redis Sink 实现为 upsert-only，天然幂等
- Kafka Sink 使用事务生产者 `FlinkKafkaProducer.Semantic.EXACTLY_ONCE`

## 6. 部署

```
Flink JobManager + 2 × TaskManager （Kubernetes Operator 部署）
  ├─ 读取 MySQL binlog
  ├─ 写 Redis（同机房）
  └─ 写 Kafka（同机房）
```

资源参考：100 QPS 事件流下 2 vCore / 4G 即可支撑。

## 7. 和本仓库代码的对应关系

| 仓库代码                                    | Flink 作业中的等价                     |
| ------------------------------------------- | -------------------------------------- |
| `UserActionService.track(...)`              | Kafka Producer / 直接 DB 插入          |
| `UserActionService#weight`                  | `WeightedSumAgg` 的权重函数            |
| `HotRankService#decay`（5min 指数衰减）     | Flink 滑动窗口（生产替换后可移除衰减） |
| `HotRankService#hotBooks / hotItems`        | Redis ZSet 读取（不变）                |
| `DashboardController#recent`                | 可改为订阅 `/topic/events` WebSocket   |

## 8. 切换清单

从本地模式升到 Flink 模式只需：

1. 部署 Flink 集群 + 开启 MySQL binlog
2. 发布 `yuequge-flink-job.jar`
3. `HotRankService` 把 `@Scheduled decay()` 注释掉（Flink 已经做窗口）
4. 可选：将 `UserActionService` 内部 Redis 写入改为只写日志 / Kafka
