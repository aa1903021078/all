package com.example.controller;


import com.example.common.Result;
import com.example.entity.DailyRecycleStats;
import com.example.service.StatisticsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据统计前端操作接口
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    /**
     * 获取首页概览数据
     */
    @GetMapping("/overview")
    public Result getOverview() {
        Map<String, Object> data = statisticsService.getOverview();
        return Result.success(data);
    }

    /**
     * 获取每日回收统计
     */
    @GetMapping("/dailyStats")
    public Result getDailyStats(@RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate) {
        List<DailyRecycleStats> list = statisticsService.getDailyStats(startDate, endDate);
        return Result.success(list);
    }

    /**
     * 获取品类统计
     */
    @GetMapping("/applianceTypeStats")
    public Result getApplianceTypeStats() {
        return Result.success(statisticsService.getApplianceTypeStats());
    }

    /**
     * 获取用户回收排行
     */
    @GetMapping("/userRanking")
    public Result getUserRanking(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(statisticsService.getUserRanking(limit));
    }

    /**
     * 获取回收员绩效排行
     */
    @GetMapping("/collectorRanking")
    public Result getCollectorRanking(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(statisticsService.getCollectorRanking(limit));
    }

    /**
     * 生成今日统计（定时任务调用）
     */
    @PostMapping("/generateToday")
    public Result generateTodayStats() {
        statisticsService.generateTodayStats();
        return Result.success();
    }
}
