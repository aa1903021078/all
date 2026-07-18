package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收藏夹
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("favorite_folder")
public class FavoriteFolder extends CreateEntity {

    private Long userId;
    private String name;
    /** SHOP / RECIPE */
    private String type;
}
