package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 探店笔记
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("note")
public class Note extends BaseEntity {

    private Long shopId;
    private Long authorId;
    private String title;
    private String content;
    /** 图集 JSON 数组字符串 */
    private String images;
    /** 作者评分 1-5 */
    private Integer rating;
    /** 0待审核 1发布 2下架 3违规 */
    private Integer status;
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;
    private Integer recommend;

    // ---- 展示字段(不入库) ----
    @TableField(exist = false)
    private String authorName;
    @TableField(exist = false)
    private String authorAvatar;
    @TableField(exist = false)
    private String shopName;
    @TableField(exist = false)
    private String shopCover;
    @TableField(exist = false)
    private Boolean liked;
}
