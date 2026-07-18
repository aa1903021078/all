package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 点赞 (NOTE/RECIPE/COMMENT/REPOST)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_like")
public class UserLike extends CreateEntity {

    private Long userId;
    private String targetType;
    private Long targetId;
}
