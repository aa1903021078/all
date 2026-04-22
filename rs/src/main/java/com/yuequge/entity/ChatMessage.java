package com.yuequge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;
    private Long sessionId;
    private Integer senderId;
    private Integer receiverId;
    private String content;
    /** 0文本 1图片 2表情 3文件 4系统 */
    private Integer msgType;
    /** 0未读 1已读 */
    private Integer status;
    private LocalDateTime createTime;
}
