package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.common.PageResult;
import com.dahaiwuliang.common.R;
import com.dahaiwuliang.common.annotation.RequireLogin;
import com.dahaiwuliang.common.annotation.RequirePerm;
import com.dahaiwuliang.dto.UpdateProfileDTO;
import com.dahaiwuliang.entity.SysUser;
import com.dahaiwuliang.service.UserService;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息 & 后台用户管理接口
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 公开用户资料 */
    @GetMapping("/{id}")
    public R<SysUser> profile(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    /** 修改我的资料 */
    @PutMapping("/profile")
    @RequireLogin
    public R<SysUser> updateProfile(@RequestBody UpdateProfileDTO dto) {
        return R.ok("保存成功", userService.updateProfile(dto));
    }

    // ---------------- 后台 ----------------

    @GetMapping("/admin/page")
    @RequirePerm("user:manage")
    public R<PageResult<SysUser>> adminPage(@RequestParam(defaultValue = "1") long current,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Integer status) {
        Page<SysUser> page = userService.adminPage(current, size, keyword, status);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    /** 封禁/解封 */
    @PutMapping("/{id}/status")
    @RequirePerm("user:manage")
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.changeStatus(id, status);
        return R.ok();
    }
}
