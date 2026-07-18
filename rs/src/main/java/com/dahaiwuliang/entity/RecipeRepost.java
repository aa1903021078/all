package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜谱复刻晒图
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recipe_repost")
public class RecipeRepost extends CreateEntity {

    private Long recipeId;
    private Long userId;
    /** 成品图 JSON 数组字符串 */
    private String images;
    private String content;
    private Integer likeCount;

    // ---- 展示字段(不入库) ----
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String userAvatar;
}
