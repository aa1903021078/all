package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 推荐菜品
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dish")
public class Dish extends CreateEntity {

    private Long shopId;
    private String name;
    private BigDecimal price;
    private String image;
    private String description;
    private Integer sort;
}
