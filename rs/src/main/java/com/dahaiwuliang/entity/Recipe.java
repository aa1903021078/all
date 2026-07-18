package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 菜谱
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recipe")
public class Recipe extends BaseEntity {

    private String title;
    private String cover;
    private Long authorId;
    private Long categoryId;
    /** 烹饪时长(分钟) */
    private Integer cookTime;
    /** 1简单 2中等 3困难 */
    private Integer difficulty;
    private String description;
    /** 0待审核 1发布 2下架 3违规 */
    private Integer status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer recommend;

    // ---- 展示字段(不入库) ----
    @TableField(exist = false)
    private String categoryName;
    @TableField(exist = false)
    private String authorName;
    @TableField(exist = false)
    private String authorAvatar;
    @TableField(exist = false)
    private Boolean liked;
    @TableField(exist = false)
    private Boolean favorited;
    @TableField(exist = false)
    private List<RecipeIngredient> ingredients;
    @TableField(exist = false)
    private List<RecipeStep> steps;
}
