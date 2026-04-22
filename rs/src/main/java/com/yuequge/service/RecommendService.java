package com.yuequge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuequge.entity.Book;
import com.yuequge.entity.RecommendResult;
import com.yuequge.entity.UserFavorite;
import com.yuequge.mapper.BookMapper;
import com.yuequge.mapper.RecommendResultMapper;
import com.yuequge.mapper.UserFavoriteMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基于物品（Item-based）协同过滤推荐。
 *
 * <p>在生产环境，该矩阵由 Spark 离线 ALS / ItemCF 作业每日计算，写入 {@code recommend_result} 表
 * 与 Redis 缓存（参见 {@code docs/spark.md}）。本类为 Java 内置的轻量实现，作为本地兜底，
 * 数据量小时足以支撑演示与开发。
 *
 * <p>输入：{@code user_favorite}（正反馈）。
 * <br>算法：
 * <ol>
 *   <li>构造 user -> items 倒排表；</li>
 *   <li>对每个用户的 item 两两共现，用 {@code 1 / log(1 + |N(u)|)} 惩罚活跃用户；</li>
 *   <li>相似度矩阵归一化：{@code sim(i,j) = cooc(i,j) / sqrt(|N(i)| * |N(j)|)}；</li>
 *   <li>为用户 u 推荐：对其已正反馈过的 item 集合，按相似度求和预估对 item j 的兴趣分。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final UserFavoriteMapper favoriteMapper;
    private final BookMapper bookMapper;
    private final RecommendResultMapper recommendMapper;
    private final HotRankService hotRankService;

    /** item -> (item -> similarity)，重建时替换。 */
    private volatile Map<Long, Map<Long, Double>> itemSim = new HashMap<>();
    private volatile LocalDateTime lastRebuildAt;
    private volatile int lastUserCount;
    private volatile int lastItemCount;

    /** 启动后 30 秒构建一次，之后每 6 小时重建（模拟 Spark 每日作业的在线等价物）。 */
    @Scheduled(initialDelay = 30_000L, fixedDelay = 6 * 3600_000L)
    public void scheduledRebuild() {
        try {
            rebuild();
        } catch (Exception e) {
            log.warn("recommend rebuild failed: {}", e.getMessage());
        }
    }

    /**
     * 从 {@code user_favorite} 读数据，构建 item-item 相似度矩阵，并把每个用户的 Top 推荐写入 DB。
     *
     * @return 统计信息
     */
    public Stats rebuild() {
        List<UserFavorite> favs = favoriteMapper.selectList(null);
        Map<Long, Set<Long>> userToItems = new HashMap<>();
        Map<Long, Integer> itemPop = new HashMap<>();
        for (UserFavorite f : favs) {
            userToItems.computeIfAbsent(f.getUserId(), k -> new HashSet<>()).add(f.getBookId());
            itemPop.merge(f.getBookId(), 1, Integer::sum);
        }

        // co-occurrence
        Map<Long, Map<Long, Double>> cooc = new HashMap<>();
        for (Set<Long> items : userToItems.values()) {
            double penalty = 1.0 / Math.log(1.0 + items.size());
            List<Long> arr = new ArrayList<>(items);
            for (int i = 0; i < arr.size(); i++) {
                Map<Long, Double> row = cooc.computeIfAbsent(arr.get(i), k -> new HashMap<>());
                for (int j = 0; j < arr.size(); j++) {
                    if (i == j) continue;
                    row.merge(arr.get(j), penalty, Double::sum);
                }
            }
        }

        // normalize -> cosine similarity
        Map<Long, Map<Long, Double>> sim = new HashMap<>(cooc.size());
        for (var entry : cooc.entrySet()) {
            Long i = entry.getKey();
            int popI = itemPop.getOrDefault(i, 1);
            Map<Long, Double> out = new HashMap<>(entry.getValue().size());
            for (var e2 : entry.getValue().entrySet()) {
                Long j = e2.getKey();
                int popJ = itemPop.getOrDefault(j, 1);
                double denom = Math.sqrt((double) popI * popJ);
                if (denom > 0) out.put(j, e2.getValue() / denom);
            }
            sim.put(i, out);
        }

        this.itemSim = sim;
        this.lastRebuildAt = LocalDateTime.now();
        this.lastUserCount = userToItems.size();
        this.lastItemCount = sim.size();
        log.info("[recommend] rebuilt: users={}, items={}", lastUserCount, lastItemCount);

        // 预计算每个用户的 top 20 并入库（便于跨进程 / 重启后读取）
        persistTopForAllUsers(userToItems, sim, 20);

        Stats s = new Stats();
        s.users = lastUserCount;
        s.items = lastItemCount;
        s.rebuildAt = lastRebuildAt;
        return s;
    }

    private void persistTopForAllUsers(Map<Long, Set<Long>> userToItems,
                                       Map<Long, Map<Long, Double>> sim,
                                       int topN) {
        for (var e : userToItems.entrySet()) {
            Long uid = e.getKey();
            List<Scored> list = computeForUser(uid, e.getValue(), sim, topN);
            try {
                recommendMapper.delete(new LambdaQueryWrapper<RecommendResult>()
                        .eq(RecommendResult::getUserId, uid));
                for (Scored sc : list) {
                    RecommendResult r = new RecommendResult();
                    r.setUserId(uid);
                    r.setBookId(sc.id);
                    r.setScore(sc.score);
                    r.setAlgo("ITEM_CF");
                    r.setUpdateTime(LocalDateTime.now());
                    recommendMapper.insert(r);
                }
            } catch (Exception ex) {
                log.debug("persist recommend for user {} failed: {}", uid, ex.getMessage());
            }
        }
    }

    private List<Scored> computeForUser(Long userId, Set<Long> userItems,
                                        Map<Long, Map<Long, Double>> sim,
                                        int topN) {
        Map<Long, Double> scores = new HashMap<>();
        for (Long i : userItems) {
            Map<Long, Double> neighbors = sim.get(i);
            if (neighbors == null) continue;
            for (var e : neighbors.entrySet()) {
                if (userItems.contains(e.getKey())) continue;
                scores.merge(e.getKey(), e.getValue(), Double::sum);
            }
        }
        return scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .map(en -> new Scored(en.getKey(), en.getValue()))
                .toList();
    }

    /** 为用户查推荐图书：先查缓存表 -> 再实时算 -> 冷启动回退热榜。 */
    public List<Book> recommendBooks(Long userId, int n) {
        if (userId == null) return hotRankService.hotBooks(n);
        try {
            List<RecommendResult> cached = recommendMapper.selectList(new LambdaQueryWrapper<RecommendResult>()
                    .eq(RecommendResult::getUserId, userId)
                    .orderByDesc(RecommendResult::getScore)
                    .last("LIMIT " + Math.max(1, Math.min(n, 50))));
            if (!cached.isEmpty()) {
                List<Long> ids = cached.stream().map(RecommendResult::getBookId).toList();
                List<Book> books = bookMapper.selectBatchIds(ids);
                books.sort((a, b) -> Integer.compare(ids.indexOf(a.getId()), ids.indexOf(b.getId())));
                return books;
            }
        } catch (Exception e) {
            log.debug("recommend cache read failed: {}", e.getMessage());
        }
        // 实时算
        Set<Long> userItems = new HashSet<>();
        for (UserFavorite f : favoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId))) {
            userItems.add(f.getBookId());
        }
        if (userItems.isEmpty()) return hotRankService.hotBooks(n);
        List<Scored> scored = computeForUser(userId, userItems, itemSim, n);
        if (scored.isEmpty()) return hotRankService.hotBooks(n);
        List<Long> ids = scored.stream().map(s -> s.id).toList();
        List<Book> books = bookMapper.selectBatchIds(ids);
        books.sort((a, b) -> Integer.compare(ids.indexOf(a.getId()), ids.indexOf(b.getId())));
        return books;
    }

    /** 相似图书（物品详情页「看过此书的人还看了」）。 */
    public List<Book> similarBooks(Long bookId, int n) {
        Map<Long, Double> neighbors = itemSim.get(bookId);
        if (neighbors == null || neighbors.isEmpty()) return List.of();
        List<Long> ids = neighbors.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(n)
                .map(Map.Entry::getKey)
                .toList();
        List<Book> books = bookMapper.selectBatchIds(ids);
        books.sort(Comparator.comparingInt(a -> ids.indexOf(a.getId())));
        return books;
    }

    public Stats currentStats() {
        Stats s = new Stats();
        s.users = lastUserCount;
        s.items = lastItemCount;
        s.rebuildAt = lastRebuildAt;
        return s;
    }

    @Data
    public static class Stats {
        private int users;
        private int items;
        private LocalDateTime rebuildAt;
    }

    public record Scored(Long id, double score) {}
}
