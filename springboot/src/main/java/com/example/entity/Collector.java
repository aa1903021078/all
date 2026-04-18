package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Collector extends Account{
    private Integer id;
    private String username;
    private String password;
    private String name;
    private String phone;
    private String idCard;           // 身份证号
    private String idCardFront;      // 身份证正面照片
    private String idCardBack;       // 身份证反面照片
    private Integer workStatus;    // 0休息 1接单中 2忙碌
    private BigDecimal rating;
    private Integer orderCount;
    private Integer todayOrderCount;
    private BigDecimal locationLat;
    private BigDecimal locationLng;
    private LocalDateTime locationUpdateTime;
    private Integer maxDailyOrders;
    private String serviceArea;    // JSON格式
    private Integer status;     // 0禁用 1启用
    private Integer auditStatus;     // 0待审核 1已通过 2已拒绝  ← 新增
    private String rejectReason;     // 拒绝原因  ← 新增
    private LocalDateTime createTime;

    // 临时字段
    private String confirmPassword;  // 注册时确认密码


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getIdCardFront() {
        return idCardFront;
    }

    public void setIdCardFront(String idCardFront) {
        this.idCardFront = idCardFront;
    }

    public String getIdCardBack() {
        return idCardBack;
    }

    public void setIdCardBack(String idCardBack) {
        this.idCardBack = idCardBack;
    }

    public Integer getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(Integer workStatus) {
        this.workStatus = workStatus;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Integer getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }

    public Integer getTodayOrderCount() {
        return todayOrderCount;
    }

    public void setTodayOrderCount(Integer todayOrderCount) {
        this.todayOrderCount = todayOrderCount;
    }

    public BigDecimal getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(BigDecimal locationLat) {
        this.locationLat = locationLat;
    }

    public BigDecimal getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(BigDecimal locationLng) {
        this.locationLng = locationLng;
    }

    public LocalDateTime getLocationUpdateTime() {
        return locationUpdateTime;
    }

    public void setLocationUpdateTime(LocalDateTime locationUpdateTime) {
        this.locationUpdateTime = locationUpdateTime;
    }

    public Integer getMaxDailyOrders() {
        return maxDailyOrders;
    }

    public void setMaxDailyOrders(Integer maxDailyOrders) {
        this.maxDailyOrders = maxDailyOrders;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}