package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 菜谱步骤
 */
@Data
@TableName("recipe_step")
public class RecipeStep implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recipeId;
    private Integer stepNo;
    private String image;
    private String content;
    private Integer sort;
}
