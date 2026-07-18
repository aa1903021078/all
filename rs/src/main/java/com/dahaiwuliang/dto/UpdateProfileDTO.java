package com.dahaiwuliang.dto;

import lombok.Data;

/**
 * 修改个人资料
 */
@Data
public class UpdateProfileDTO {
    private String nickname;
    private String avatar;
    private String bio;
    private Integer gender;
    private String phone;
}
