package com.yuequge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.yuequge.entity.Book;
import com.yuequge.entity.Item;
import com.yuequge.mapper.BookMapper;
import com.yuequge.mapper.ItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 实时热榜查询 + 衰减任务。
 *
 * <p>在生产环境，Redis Sorted Set 的写入由 Flink 作业完成（见 {@code docs/flink.md}）；
 * 在本地 all-in-one 模式下，由 {@link UserActionService} 写入，此任务负责定时指数衰减。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotRankService {

    private final StringRedisTemplate redis;
    private final BookMapper bookMapper;
    private final ItemMapper itemMapper;

    /** 每 5 分钟衰减一次 -> 7 天半衰期近似：factor = 0.5 ^ (5/ (7*24*60)) ≈ 0.99966 */
    private static final double DECAY = 0.99966;

    @Scheduled(fixedDelay = 5 * 60_000L, initialDelay = 5 * 60_000L)
    public void decay() {
        decayZset(UserActionService.ZSET_HOT_BOOK);
        decayZset(UserActionService.ZSET_HOT_ITEM);
    }

    private void decayZset(String key) {
        try {
            Set<String> members = redis.opsForZSet().range(key, 0, -1);
            if (members == null || members.isEmpty()) return;
            for (String m : members) {
                Double s = redis.opsForZSet().score(key, m);
                if (s == null) continue;
                double newScore = s * DECAY;
                if (newScore < 0.5) {
                    redis.opsForZSet().remove(key, m);
                } else {
                    redis.opsForZSet().add(key, m, newScore);
                }
            }
        } catch (Exception e) {
            log.debug("decay {} failed: {}", key, e.getMessage());
        }
    }

    /** 取实时图书热榜（Top N）。Redis 不可用时退回到 DB heat 字段。 */
    public List<Book> hotBooks(int limit) {
        try {
            Set<String> ids = redis.opsForZSet().reverseRange(UserActionService.ZSET_HOT_BOOK, 0, limit - 1);
            if (ids != null && !ids.isEmpty()) {
                List<Long> bookIds = ids.stream().map(Long::valueOf).toList();
                List<Book> list = bookMapper.selectBatchIds(bookIds);
                // 保持 redis 顺序
                list.sort((a, b) -> Integer.compare(bookIds.indexOf(a.getId()), bookIds.indexOf(b.getId())));
                return list;
            }
        } catch (Exception e) {
            log.debug("hotBooks redis failed: {}", e.getMessage());
        }
        return bookMapper.selectList(new LambdaQueryWrapper<Book>()
                .isNotNull(Book::getName)
                .orderByDesc(Book::getHeat)
                .last("LIMIT " + Math.max(1, Math.min(limit, 50))));
    }

    public List<Item> hotItems(int limit) {
        try {
            Set<String> ids = redis.opsForZSet().reverseRange(UserActionService.ZSET_HOT_ITEM, 0, limit - 1);
            if (ids != null && !ids.isEmpty()) {
                List<Long> itemIds = ids.stream().map(Long::valueOf).toList();
                List<Item> list = itemMapper.selectBatchIds(itemIds);
                list.sort((a, b) -> Integer.compare(itemIds.indexOf(a.getItemId()), itemIds.indexOf(b.getItemId())));
                return list;
            }
        } catch (Exception e) {
            log.debug("hotItems redis failed: {}", e.getMessage());
        }
        List<Item> all = itemMapper.selectList(null);
        return all.subList(0, Math.min(limit, all.size()));
    }

    /** 最近事件流（给大屏滚动展示）。 */
    public List<String> recentEvents(int limit) {
        try {
            List<String> list = redis.opsForList().range(UserActionService.LIST_RECENT_EVENTS, 0, limit - 1);
            return list == null ? Collections.emptyList() : list;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /** 将当前 Redis ZSet + 事件流全部清理（调试用）。 */
    public void reset() {
        try {
            redis.delete(UserActionService.ZSET_HOT_BOOK);
            redis.delete(UserActionService.ZSET_HOT_ITEM);
            redis.delete(UserActionService.LIST_RECENT_EVENTS);
        } catch (Exception ignore) {
        }
    }

    /** 返回实时 Top N 图书 id+score 的 Pair 列表（供大屏图表）。 */
    public List<ScoredId> topBooksWithScore(int limit) {
        List<ScoredId> out = new ArrayList<>();
        try {
            var set = redis.opsForZSet().reverseRangeWithScores(UserActionService.ZSET_HOT_BOOK, 0, limit - 1);
            if (set == null) return out;
            Set<Long> seen = new LinkedHashSet<>();
            for (var t : set) {
                if (t.getValue() == null) continue;
                seen.add(Long.valueOf(t.getValue()));
                out.add(new ScoredId(Long.valueOf(t.getValue()), t.getScore() == null ? 0 : t.getScore()));
            }
            if (!out.isEmpty()) {
                List<Book> books = bookMapper.selectBatchIds(new ArrayList<>(seen));
                for (ScoredId s : out) {
                    books.stream().filter(b -> b.getId().equals(s.id)).findFirst()
                            .ifPresent(b -> s.name = b.getName());
                }
            }
        } catch (Exception ignore) {
        }
        if (CollectionUtils.isEmpty(out)) {
            // fallback
            List<Book> list = hotBooks(limit);
            for (Book b : list) {
                ScoredId s = new ScoredId(b.getId(), b.getHeat() == null ? 0 : b.getHeat());
                s.name = b.getName();
                out.add(s);
            }
        }
        return out;
    }

    public static class ScoredId {
        public Long id;
        public double score;
        public String name;
        public ScoredId(Long id, double score) { this.id = id; this.score = score; }
    }
}
