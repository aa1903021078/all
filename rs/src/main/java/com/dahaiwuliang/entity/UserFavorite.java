package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收藏
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_favorite")
public class UserFavorite extends CreateEntity {

    private Long userId;
    /** SHOP / RECIPE */
    private String targetType;
    private Long targetId;
    private Long folderId;
}
