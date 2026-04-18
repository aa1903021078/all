package com.example.entity;

import java.time.LocalDateTime;

public class Banner {
    private Integer id;
    /** 轮播标题 */
    private String title;
    /** 副标题 */
    private String subtitle;
    /** 背景渐变色值（数据库：bg_color） */
    private String bgColor;
    /** 图标emoji */
    private String icon;
    /** 排序号（数据库：sort_order） */
    private Integer sortOrder;
    /** 状态：0-下架，1-上架 */
    private Integer status;
    /** 创建时间（数据库：create_time） */
    private LocalDateTime createTime;
    /** 更新时间（数据库：update_time） */
    private LocalDateTime updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}

