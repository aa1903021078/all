package com.example.entity;

import java.time.LocalDateTime;

public class UserBadge {
    private Integer id;
    private Integer userId;
    private String badgeCode;
    private String badgeName;
    private String badgeIcon;
    private String description;
    private LocalDateTime obtainTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getBadgeCode() {
        return badgeCode;
    }

    public void setBadgeCode(String badgeCode) {
        this.badgeCode = badgeCode;
    }

    public String getBadgeName() {
        return badgeName;
    }

    public void setBadgeName(String badgeName) {
        this.badgeName = badgeName;
    }

    public String getBadgeIcon() {
        return badgeIcon;
    }

    public void setBadgeIcon(String badgeIcon) {
        this.badgeIcon = badgeIcon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getObtainTime() {
        return obtainTime;
    }

    public void setObtainTime(LocalDateTime obtainTime) {
        this.obtainTime = obtainTime;
    }
}
