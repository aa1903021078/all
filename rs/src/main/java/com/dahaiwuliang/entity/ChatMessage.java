package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 聊天消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_message")
public class ChatMessage extends CreateEntity {

    private Long sessionId;
    private Long fromUserId;
    private Long toUserId;
    private String content;
    private String type;
    private Integer isRead;
}
