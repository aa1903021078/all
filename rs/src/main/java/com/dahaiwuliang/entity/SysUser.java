package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String idCard;
    private String gender;
    private String role;          // PATIENT / DOCTOR / ADMIN
    private Long hospitalId;
    private Long deptId;
    private String title;
    private Integer enabled;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
}
