package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 店铺
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shop")
public class Shop extends BaseEntity {

    private String name;
    private Long categoryId;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private BigDecimal avgPrice;
    private String cover;
    /** 实拍图集 JSON 数组字符串 */
    private String images;
    private String businessHours;
    private String phone;
    private String description;
    private Long merchantId;
    /** 0待审核 1上架 2下架 3拒绝 */
    private Integer status;
    private BigDecimal rating;
    private Integer ratingCount;
    private Integer viewCount;
    private Integer checkinCount;
    /** 首页推荐 0否 1是 */
    private Integer recommend;

    // ---- 以下为展示字段(不入库) ----
    @TableField(exist = false)
    private String categoryName;
    @TableField(exist = false)
    private Boolean favorited;
    /** 当前用户是否已点亮 */
    @TableField(exist = false)
    private Boolean lit;
    @TableField(exist = false)
    private List<Dish> dishes;
}
