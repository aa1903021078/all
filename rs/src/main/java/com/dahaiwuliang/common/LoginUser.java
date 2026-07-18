package com.dahaiwuliang.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 当前登录用户信息(存于 ThreadLocal / JWT 载荷)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    /** 角色编码 ADMIN/REVIEWER/MERCHANT/USER */
    private List<String> roles;
    /** 权限编码 如 content:review */
    private List<String> perms;

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /** 拥有 *:*:* 视为超级权限 */
    public boolean hasPerm(String perm) {
        if (perms == null) {
            return false;
        }
        return perms.contains("*:*:*") || perms.contains(perm);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN") || hasPerm("*:*:*");
    }
}
