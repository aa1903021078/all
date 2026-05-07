package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long senderId;
    private Long receiverId;
    private Long orderId;
    private String content;
    private Integer type;
    private Integer isRead;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
