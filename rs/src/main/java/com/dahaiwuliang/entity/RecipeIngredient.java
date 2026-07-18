package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 菜谱食材
 */
@Data
@TableName("recipe_ingredient")
public class RecipeIngredient implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recipeId;
    private String name;
    private String amount;
    private Integer sort;
}
