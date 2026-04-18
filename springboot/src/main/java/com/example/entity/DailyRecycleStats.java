package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DailyRecycleStats {
    private Integer id;
    private LocalDate statDate;
    private Integer totalOrders;
    private Integer completedOrders;
    private Integer cancelledOrders;
    private BigDecimal totalWeight;
    private BigDecimal totalCarbonSaved;
    private Integer totalPointsGiven;
    private Integer avgResponseTime;
    private Integer activeUsers;
    private Integer activeCollectors;
    private String applianceTypeStats; // JSON格式
    private LocalDateTime createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getStatDate() {
        return statDate;
    }

    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Integer getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(Integer completedOrders) {
        this.completedOrders = completedOrders;
    }

    public Integer getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(Integer cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public BigDecimal getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }

    public BigDecimal getTotalCarbonSaved() {
        return totalCarbonSaved;
    }

    public void setTotalCarbonSaved(BigDecimal totalCarbonSaved) {
        this.totalCarbonSaved = totalCarbonSaved;
    }

    public Integer getTotalPointsGiven() {
        return totalPointsGiven;
    }

    public void setTotalPointsGiven(Integer totalPointsGiven) {
        this.totalPointsGiven = totalPointsGiven;
    }

    public Integer getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(Integer avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    public Integer getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Integer activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Integer getActiveCollectors() {
        return activeCollectors;
    }

    public void setActiveCollectors(Integer activeCollectors) {
        this.activeCollectors = activeCollectors;
    }

    public String getApplianceTypeStats() {
        return applianceTypeStats;
    }

    public void setApplianceTypeStats(String applianceTypeStats) {
        this.applianceTypeStats = applianceTypeStats;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
