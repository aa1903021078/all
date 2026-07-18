package com.dahaiwuliang.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 我的消息(收到的赞 / 收到的评论回复) 展示对象
 */
@Data
public class MessageVO implements Serializable {

    /** 来源记录 id(点赞记录或评论 id) */
    private Long id;
    /** LIKE=收到点赞  COMMENT=收到评论  REPLY=收到回复 */
    private String kind;
    /** 触发者 */
    private Long fromUserId;
    private String fromUserName;
    private String fromUserAvatar;
    /** 关联内容 NOTE / RECIPE */
    private String targetType;
    private Long targetId;
    private String targetTitle;
    /** 评论/回复正文(点赞时为空) */
    private String content;
    private LocalDateTime createTime;
}
