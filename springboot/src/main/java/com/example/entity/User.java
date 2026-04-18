package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class User extends Account{
    private Integer id;
    private String username;
    private String password;
    private String name;
    private String role;
    private String account;
    private String phone;
    private String avatar;
    private Integer points;
    private Integer totalRecycleCount;
    private java.math.BigDecimal totalRecycleWeight;
    private java.math.BigDecimal carbonSaved;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    //Alt+Insert后选择Getter和Setter,然后按Shift和鼠标点击全选，写出下面方法


    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
    @Override
    public String getRole() {
        return role;
    }

    @Override
    public void setRole(String role) {
        this.role = role;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    @Override
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getTotalRecycleCount() {
        return totalRecycleCount;
    }

    public void setTotalRecycleCount(Integer totalRecycleCount) {
        this.totalRecycleCount = totalRecycleCount;
    }

    public BigDecimal getTotalRecycleWeight() {
        return totalRecycleWeight;
    }

    public void setTotalRecycleWeight(BigDecimal totalRecycleWeight) {
        this.totalRecycleWeight = totalRecycleWeight;
    }

    public BigDecimal getCarbonSaved() {
        return carbonSaved;
    }

    public void setCarbonSaved(BigDecimal carbonSaved) {
        this.carbonSaved = carbonSaved;
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

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
