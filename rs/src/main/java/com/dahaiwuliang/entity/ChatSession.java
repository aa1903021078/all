package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 聊天会话
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_session")
public class ChatSession extends BaseEntity {

    private Long userId;
    private Long merchantId;
    private Long shopId;
    private String lastMessage;
    private LocalDateTime lastTime;
    private Integer userUnread;
    private Integer merchantUnread;

    // ---- 展示字段(不入库) ----
    @TableField(exist = false)
    private String peerName;
    @TableField(exist = false)
    private String peerAvatar;
    @TableField(exist = false)
    private String shopName;
    /** 当前视角未读数 */
    @TableField(exist = false)
    private Integer unread;
}
