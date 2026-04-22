package com.yuequge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_action_log")
public class UserActionLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** BOOK / CHAPTER / ITEM / SONG / ORDER */
    private String targetType;
    private Long targetId;
    /** VIEW / READ / FAVORITE / UNFAVORITE / ORDER / PAY / COMMENT */
    private String action;
    private String extra;
    private LocalDateTime createTime;
}
