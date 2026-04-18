package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.DailyRecycleStats;
import com.example.mapper.DailyRecycleStatsMapper;
import com.example.mapper.RecycleOrderMapper;
import com.example.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    @Resource
    private RecycleOrderMapper recycleOrderMapper;

    @Resource
    private DailyRecycleStatsMapper dailyRecycleStatsMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 获取首页概览数据
     */
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();

        // 订单统计
        overview.put("totalOrders", recycleOrderMapper.countTotal());
        overview.put("todayOrders", recycleOrderMapper.countToday());
        overview.put("pendingOrders", recycleOrderMapper.countByStatus(0));
        overview.put("processingOrders", recycleOrderMapper.countByStatus(1) +
                recycleOrderMapper.countByStatus(2) + recycleOrderMapper.countByStatus(3));
        overview.put("completedOrders", recycleOrderMapper.countByStatus(4));
        overview.put("cancelledOrders", recycleOrderMapper.countByStatus(5));

        // 重量和环保统计
        overview.put("totalWeight", recycleOrderMapper.sumTotalWeight());
        overview.put("totalCarbonSaved", recycleOrderMapper.sumTotalCarbonSaved());

        // 今日数据
        String today = DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd");
        DailyRecycleStats todayStats = dailyRecycleStatsMapper.selectByDate(today);
        if (todayStats != null) {
            overview.put("todayStats", todayStats);
        }

        return overview;
    }

    /**
     * 获取每日回收统计
     */
    public List<DailyRecycleStats> getDailyStats(String startDate, String endDate) {
        if (startDate == null) {
            startDate = DateUtil.format(LocalDateTime.now().minusDays(30), "yyyy-MM-dd");
        }
        if (endDate == null) {
            endDate = DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd");
        }
        return dailyRecycleStatsMapper.selectByDateRange(startDate, endDate);
    }

    /**
     * 获取品类统计
     */
    public List<Map<String, Object>> getApplianceTypeStats() {
        // 实际应从订单表按品类分组统计
        return recycleOrderMapper.selectApplianceTypeStats();
    }

    /**
     * 获取用户回收排行
     */
    public List<Map<String, Object>> getUserRanking(Integer limit) {
        return userMapper.selectPointsRanking(limit);
    }


    /**
     * 获取回收员绩效排行
     */
    public List<Map<String, Object>> getCollectorRanking(Integer limit) {
        // 实际应从回收员表查询
        return recycleOrderMapper.selectCollectorRanking(limit);
    }

    /**
     * 生成今日统计（定时任务调用）
     */
    public void generateTodayStats() {
        String today = DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd");
        DailyRecycleStats existing = dailyRecycleStatsMapper.selectByDate(today);

        if (existing == null) {
            DailyRecycleStats stats = new DailyRecycleStats();
            stats.setStatDate(LocalDate.now());
            stats.setTotalOrders(0);
            stats.setCompletedOrders(0);
            stats.setCancelledOrders(0);
            stats.setTotalWeight(BigDecimal.ZERO);
            stats.setTotalCarbonSaved(BigDecimal.ZERO);
            stats.setTotalPointsGiven(0);
            stats.setAvgResponseTime(0);
            stats.setActiveUsers(0);
            stats.setActiveCollectors(0);
            dailyRecycleStatsMapper.insert(stats);
        }
    }
}
