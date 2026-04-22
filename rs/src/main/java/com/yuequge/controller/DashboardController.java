package com.yuequge.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuequge.common.Result;
import com.yuequge.entity.Book;
import com.yuequge.entity.Item;
import com.yuequge.entity.OrderInfo;
import com.yuequge.entity.UserActionLog;
import com.yuequge.exception.BizException;
import com.yuequge.mapper.BookMapper;
import com.yuequge.mapper.OrderInfoMapper;
import com.yuequge.mapper.UserActionLogMapper;
import com.yuequge.mapper.UserMapper;
import com.yuequge.service.HotRankService;
import com.yuequge.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 数据大屏 + 实时热榜 HTTP 接口。
 */
@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final HotRankService hotRankService;
    private final UserMapper userMapper;
    private final BookMapper bookMapper;
    private final OrderInfoMapper orderMapper;
    private final UserActionLogMapper actionMapper;

    // ========== public 实时热榜 ==========

    @GetMapping("/api/public/hot/realtime/books")
    public Result<List<Book>> hotBooks(@RequestParam(defaultValue = "8") int n) {
        return Result.ok(hotRankService.hotBooks(Math.max(1, Math.min(n, 30))));
    }

    @GetMapping("/api/public/hot/realtime/items")
    public Result<List<Item>> hotItems(@RequestParam(defaultValue = "6") int n) {
        return Result.ok(hotRankService.hotItems(Math.max(1, Math.min(n, 30))));
    }

    // ========== admin 大屏 ==========

    @GetMapping("/api/admin/dashboard/overview")
    public Result<Map<String, Object>> overview() {
        requireAdmin();
        Map<String, Object> m = new HashMap<>();
        m.put("userTotal", userMapper.selectCount(null));
        m.put("bookTotal", bookMapper.selectCount(new LambdaQueryWrapper<Book>().isNotNull(Book::getName)));
        m.put("orderTotal", orderMapper.selectCount(null));
        m.put("orderPaid", orderMapper.selectCount(new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getStatus, "1")));
        // 今日活跃
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        m.put("actionsToday", actionMapper.selectCount(new LambdaQueryWrapper<UserActionLog>()
                .ge(UserActionLog::getCreateTime, todayStart)));
        // 付款总额
        List<OrderInfo> paid = orderMapper.selectList(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getStatus, "1"));
        BigDecimal sum = paid.stream()
                .map(o -> o.getAmount() == null ? BigDecimal.ZERO : o.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        m.put("amountPaid", sum);
        return Result.ok(m);
    }

    /** 热榜 + 分数（给柱状图）。 */
    @GetMapping("/api/admin/dashboard/hot-books")
    public Result<List<HotRankService.ScoredId>> hot(@RequestParam(defaultValue = "8") int n) {
        requireAdmin();
        return Result.ok(hotRankService.topBooksWithScore(n));
    }

    /** 最近 7 天订单趋势（日期 -> 订单数 / 金额）。 */
    @GetMapping("/api/admin/dashboard/order-trend")
    public Result<List<Map<String, Object>>> orderTrend() {
        requireAdmin();
        LocalDate start = LocalDate.now().minusDays(6);
        List<OrderInfo> list = orderMapper.selectList(new LambdaQueryWrapper<OrderInfo>()
                .ge(OrderInfo::getCreateTime, start.atStartOfDay()));
        Map<LocalDate, int[]> countMap = new LinkedHashMap<>();
        Map<LocalDate, BigDecimal> amountMap = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = start.plusDays(i);
            countMap.put(d, new int[1]);
            amountMap.put(d, BigDecimal.ZERO);
        }
        for (OrderInfo o : list) {
            if (o.getCreateTime() == null) continue;
            LocalDate d = o.getCreateTime().toLocalDate();
            int[] c = countMap.get(d);
            if (c == null) continue;
            c[0]++;
            if ("1".equals(o.getStatus()) && o.getAmount() != null) {
                amountMap.merge(d, o.getAmount(), BigDecimal::add);
            }
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (var e : countMap.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", e.getKey().toString());
            row.put("count", e.getValue()[0]);
            row.put("amount", amountMap.get(e.getKey()));
            out.add(row);
        }
        return Result.ok(out);
    }

    /** 图书分类分布。 */
    @GetMapping("/api/admin/dashboard/book-type-dist")
    public Result<List<Map<String, Object>>> bookTypeDist() {
        requireAdmin();
        List<Book> all = bookMapper.selectList(new LambdaQueryWrapper<Book>().isNotNull(Book::getName));
        Map<String, Integer> group = new LinkedHashMap<>();
        for (Book b : all) {
            String t = b.getType() == null || b.getType().isBlank() ? "未分类" : b.getType();
            group.merge(t, 1, Integer::sum);
        }
        return Result.ok(group.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", e.getKey());
                    m.put("value", e.getValue());
                    return m;
                })
                .toList());
    }

    /** 最近事件流（给大屏滚动列）。 */
    @GetMapping("/api/admin/dashboard/recent-events")
    public Result<List<String>> recent(@RequestParam(defaultValue = "50") int n) {
        requireAdmin();
        List<String> events = hotRankService.recentEvents(Math.max(1, Math.min(n, 200)));
        if (events.isEmpty()) {
            // 从 DB 拉最近几条
            List<UserActionLog> list = actionMapper.selectList(new LambdaQueryWrapper<UserActionLog>()
                    .orderByDesc(UserActionLog::getCreateTime)
                    .last("LIMIT " + Math.max(1, Math.min(n, 200))));
            List<String> out = new ArrayList<>();
            for (UserActionLog l : list) {
                out.add(String.format("{\"userId\":%s,\"type\":\"%s\",\"id\":%s,\"action\":\"%s\",\"t\":\"%s\"}",
                        l.getUserId(), l.getTargetType(), l.getTargetId(), l.getAction(),
                        l.getCreateTime() == null ? "" : l.getCreateTime().toString()));
            }
            return Result.ok(out);
        }
        return Result.ok(events);
    }

    /** 重置实时热榜（调试/演示用）。 */
    @PostMapping("/api/admin/dashboard/reset-hot")
    public Result<Void> resetHot() {
        requireAdmin();
        hotRankService.reset();
        return Result.ok();
    }

    private void requireAdmin() {
        var u = UserContext.get();
        if (u == null || !u.isAdmin()) throw new BizException(403, "无权访问");
    }
}
