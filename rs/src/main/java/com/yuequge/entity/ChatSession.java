package com.yuequge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_session")
public class ChatSession {
    @TableId(value = "session_id", type = IdType.AUTO)
    private Long sessionId;
    private Integer userA;
    private Integer userB;
    private String lastMessage;
    private LocalDateTime lastTime;
    /** 0=普通 1=客服会话 */
    private Integer isSupport;
}
