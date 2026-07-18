package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 评论(多级)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment")
public class Comment extends CreateEntity {

    /** NOTE / RECIPE */
    private String targetType;
    private Long targetId;
    private Long userId;
    /** 父评论 0为顶级 */
    private Long parentId;
    private Long replyUserId;
    private String content;
    private Integer likeCount;

    // ---- 展示字段(不入库) ----
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String userAvatar;
    @TableField(exist = false)
    private String replyUserName;
    @TableField(exist = false)
    private Boolean liked;
    @TableField(exist = false)
    private List<Comment> children;
}
