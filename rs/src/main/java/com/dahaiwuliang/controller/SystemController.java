package com.dahaiwuliang.controller;

import com.dahaiwuliang.common.R;
import com.dahaiwuliang.common.annotation.RequirePerm;
import com.dahaiwuliang.entity.SysConfig;
import com.dahaiwuliang.entity.SysPermission;
import com.dahaiwuliang.entity.SysRole;
import com.dahaiwuliang.service.ConfigService;
import com.dahaiwuliang.service.RbacService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置 & RBAC 管理接口(超级管理员)
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    private final RbacService rbacService;
    private final ConfigService configService;

    public SystemController(RbacService rbacService, ConfigService configService) {
        this.rbacService = rbacService;
        this.configService = configService;
    }

    // ---------------- RBAC ----------------

    @GetMapping("/roles")
    @RequirePerm("config:manage")
    public R<List<SysRole>> roles() {
        return R.ok(rbacService.listRoles());
    }

    @GetMapping("/permissions")
    @RequirePerm("config:manage")
    public R<List<SysPermission>> permissions() {
        return R.ok(rbacService.listPermissions());
    }

    @GetMapping("/users/{userId}/roles")
    @RequirePerm("config:manage")
    public R<List<Long>> userRoles(@PathVariable Long userId) {
        return R.ok(rbacService.userRoleIds(userId));
    }

    @PutMapping("/users/{userId}/roles")
    @RequirePerm("config:manage")
    public R<Void> assignUserRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        rbacService.assignUserRoles(userId, roleIds);
        return R.ok();
    }

    @GetMapping("/roles/{roleId}/permissions")
    @RequirePerm("config:manage")
    public R<List<Long>> rolePermissions(@PathVariable Long roleId) {
        return R.ok(rbacService.rolePermissionIds(roleId));
    }

    @PutMapping("/roles/{roleId}/permissions")
    @RequirePerm("config:manage")
    public R<Void> assignRolePermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        rbacService.assignRolePermissions(roleId, permissionIds);
        return R.ok();
    }

    // ---------------- 系统配置 ----------------

    /** 公开配置(站点名/地图等前端可读) */
    @GetMapping("/configs/public")
    public R<Map<String, String>> publicConfigs() {
        return R.ok(configService.getMap());
    }

    @GetMapping("/configs")
    @RequirePerm("config:manage")
    public R<List<SysConfig>> configs() {
        return R.ok(configService.listAll());
    }

    @PostMapping("/configs")
    @RequirePerm("config:manage")
    public R<SysConfig> saveConfig(@RequestBody SysConfig config) {
        return R.ok("保存成功", configService.save(config));
    }

    @PutMapping("/configs/{id}")
    @RequirePerm("config:manage")
    public R<Void> updateConfig(@PathVariable Long id, @RequestParam String value) {
        configService.update(id, value);
        return R.ok();
    }
}
