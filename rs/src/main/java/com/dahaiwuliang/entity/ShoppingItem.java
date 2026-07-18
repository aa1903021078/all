package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 食材采购清单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shopping_item")
public class ShoppingItem extends BaseEntity {

    private Long userId;
    private String name;
    private String amount;
    private Long recipeId;
    /** 0待采购 1家中已有 2已核销 */
    private Integer status;
}
