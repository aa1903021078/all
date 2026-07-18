package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;

    @JsonIgnore
    private String password;

    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    /** 0未知 1男 2女 */
    private Integer gender;
    private String bio;
    /** 0正常 1封禁 */
    private Integer status;
}
