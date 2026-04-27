package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String action;
    private String target;
    private String params;
    private Integer success;
    private String errorMsg;
    private String ip;
    private Long durationMs;
    private LocalDateTime createdAt;
}
