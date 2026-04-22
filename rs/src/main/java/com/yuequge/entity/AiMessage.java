package com.yuequge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_message")
public class AiMessage {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Integer userId;
    private String conversationId;
    /** user / assistant / system */
    private String role;
    private String content;
    private LocalDateTime createTime;
}
