package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 家电品类实体
 */
public class ApplianceType {
    private Integer id;  // 品类ID
    private String name;  // 品类名称
    private String icon;  // 图标
    private String unit;  // 计量单位（默认"台"）
    private BigDecimal priceMin;  // 最低预估价
    private BigDecimal priceMax;  // 最高预估价
    private BigDecimal weightEstimate;  // 预估重量(kg)
    private BigDecimal pointsPerKg;  // 每公斤积分
    private BigDecimal carbonFactor;  // 减碳系数
    private String description;  // 品类描述
    private Integer sortOrder;  // 排序
    private Integer status;  // 状态：0禁用 1启用
    private LocalDateTime createTime;  // 创建时间

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getPriceMin() {
        return priceMin;
    }

    public void setPriceMin(BigDecimal priceMin) {
        this.priceMin = priceMin;
    }

    public BigDecimal getPriceMax() {
        return priceMax;
    }

    public void setPriceMax(BigDecimal priceMax) {
        this.priceMax = priceMax;
    }

    public BigDecimal getWeightEstimate() {
        return weightEstimate;
    }

    public void setWeightEstimate(BigDecimal weightEstimate) {
        this.weightEstimate = weightEstimate;
    }

    public BigDecimal getPointsPerKg() {
        return pointsPerKg;
    }

    public void setPointsPerKg(BigDecimal pointsPerKg) {
        this.pointsPerKg = pointsPerKg;
    }

    public BigDecimal getCarbonFactor() {
        return carbonFactor;
    }

    public void setCarbonFactor(BigDecimal carbonFactor) {
        this.carbonFactor = carbonFactor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}