package com.dahaiwuliang.controller;

import com.dahaiwuliang.common.R;
import com.dahaiwuliang.common.annotation.RequirePerm;
import com.dahaiwuliang.common.annotation.RequireRole;
import com.dahaiwuliang.service.StatsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据统计接口
 */
@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /** 平台概览(公开) */
    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        return R.ok(statsService.overview());
    }

    /** 菜系分布(公开, 用于数据可视化) */
    @GetMapping("/category-distribution")
    public R<List<Map<String, Object>>> categoryDistribution() {
        return R.ok(statsService.categoryDistribution());
    }

    /** 人均消费区间分布(公开) */
    @GetMapping("/price-distribution")
    public R<List<Map<String, Object>>> priceDistribution() {
        return R.ok(statsService.priceDistribution());
    }

    /** 笔记发布趋势(公开) */
    @GetMapping("/publish-trend")
    public R<List<Map<String, Object>>> publishTrend() {
        return R.ok(statsService.publishTrend());
    }

    /** 数据大屏(需权限) */
    @GetMapping("/dashboard")
    @RequirePerm("dashboard:view")
    public R<Map<String, Object>> dashboard() {
        return R.ok(statsService.dashboard());
    }

    /** 商家看板 */
    @GetMapping("/merchant")
    @RequireRole("MERCHANT")
    public R<Map<String, Object>> merchant() {
        return R.ok(statsService.merchantOverview());
    }

    @GetMapping("/merchant/checkin-trend")
    @RequireRole("MERCHANT")
    public R<List<Map<String, Object>>> merchantCheckinTrend(@RequestParam Long shopId) {
        return R.ok(statsService.merchantCheckinTrend(shopId));
    }
}
