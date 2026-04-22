package com.yuequge.service;

import com.yuequge.entity.UserActionLog;
import com.yuequge.mapper.UserActionLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 用户行为采集服务。
 *
 * <p>职责：
 * <ul>
 *   <li>把行为写入 {@code user_action_log}（供 Spark 离线 / Flink 实时消费）</li>
 *   <li>更新 Redis 实时热榜（Sorted Set，带最近 N 条事件 List 给大屏行为流）</li>
 * </ul>
 *
 * <p>设计：在生产环境，该服务只负责写 Kafka / log 文件，由 Flink 消费；此处为 all-in-one 简化实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionService {

    private final UserActionLogMapper mapper;
    private final StringRedisTemplate redis;

    public static final String ZSET_HOT_BOOK = "yuequge:rt:hot:book";
    public static final String ZSET_HOT_ITEM = "yuequge:rt:hot:item";
    public static final String LIST_RECENT_EVENTS = "yuequge:rt:events";
    public static final int MAX_RECENT_EVENTS = 100;

    /** 异步写日志 + 更新 Redis。Redis 不可用时只落库。 */
    @Async
    public void track(Long userId, String targetType, Long targetId, String action) {
        track(userId, targetType, targetId, action, null);
    }

    @Async
    public void track(Long userId, String targetType, Long targetId, String action, String extra) {
        try {
            UserActionLog log = new UserActionLog();
            log.setUserId(userId);
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            log.setAction(action);
            log.setExtra(extra);
            log.setCreateTime(LocalDateTime.now());
            mapper.insert(log);
        } catch (Exception e) {
            UserActionService.log.debug("track persist failed: {}", e.getMessage());
        }

        // 更新 Redis：热度累加 + 事件流
        try {
            double delta = weight(action);
            if (delta > 0 && targetId != null) {
                String zset = zsetFor(targetType);
                if (zset != null) {
                    redis.opsForZSet().incrementScore(zset, String.valueOf(targetId), delta);
                    redis.expire(zset, Duration.ofDays(7));
                }
            }
            String eventJson = String.format(
                    "{\"userId\":%s,\"type\":\"%s\",\"id\":%s,\"action\":\"%s\",\"t\":\"%s\"}",
                    userId, targetType, targetId, action, LocalDateTime.now());
            redis.opsForList().leftPush(LIST_RECENT_EVENTS, eventJson);
            redis.opsForList().trim(LIST_RECENT_EVENTS, 0, MAX_RECENT_EVENTS - 1);
        } catch (Exception e) {
            log.debug("track redis failed: {}", e.getMessage());
        }
    }

    /** 不同动作的权重（对应 Flink 打分逻辑）。 */
    private static double weight(String action) {
        if (action == null) return 0;
        return switch (action) {
            case "VIEW"     -> 1.0;
            case "READ"     -> 2.0;
            case "FAVORITE" -> 5.0;
            case "COMMENT"  -> 4.0;
            case "ORDER"    -> 6.0;
            case "PAY"      -> 10.0;
            default -> 0.0;
        };
    }

    private static String zsetFor(String targetType) {
        if (targetType == null) return null;
        return switch (targetType) {
            case "BOOK", "CHAPTER" -> ZSET_HOT_BOOK;
            case "ITEM" -> ZSET_HOT_ITEM;
            default -> null;
        };
    }
}
