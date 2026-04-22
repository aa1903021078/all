package com.yuequge.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuequge.common.PageResult;
import com.yuequge.common.Result;
import com.yuequge.entity.User;
import com.yuequge.exception.BizException;
import com.yuequge.mapper.UserMapper;
import com.yuequge.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 个人中心 + 管理员用户管理。
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public Result<User> me() {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        User user = userMapper.selectById(u.userId());
        if (user != null) user.setPassword(null);
        return Result.ok(user);
    }

    @PutMapping("/me")
    public Result<Void> updateProfile(@RequestBody Map<String, String> body) {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        User user = userMapper.selectById(u.userId());
        if (user == null) throw new BizException(404, "用户不存在");
        if (body.containsKey("avatar")) user.setAvatar(body.get("avatar"));
        if (body.containsKey("phone")) user.setPhone(body.get("phone"));
        userMapper.updateById(user);
        return Result.ok();
    }

    @PutMapping("/me/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        String oldPwd = body.get("oldPassword");
        String newPwd = body.get("newPassword");
        if (newPwd == null || newPwd.length() < 6) throw new BizException("新密码长度至少 6 位");
        User user = userMapper.selectById(u.userId());
        if (user == null) throw new BizException(404, "用户不存在");
        if (!passwordEncoder.matches(oldPwd, user.getPassword())) throw new BizException("旧密码错误");
        user.setPassword(passwordEncoder.encode(newPwd));
        userMapper.updateById(user);
        return Result.ok();
    }
}

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
class AdminUserController {

    private final UserMapper userMapper;

    @GetMapping
    public Result<PageResult<User>> page(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "10") long size,
                                          @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.like(User::getUsername, keyword).or().like(User::getPhone, keyword);
        }
        w.orderByDesc(User::getUserId);
        Page<User> p = userMapper.selectPage(Page.of(page, size), w);
        p.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(PageResult.of(p.getTotal(), p.getRecords()));
    }

    @PutMapping("/{id}/status")
    public Result<Void> setStatus(@PathVariable Long id, @RequestParam Integer status) {
        User u = userMapper.selectById(id);
        if (u == null) throw new BizException(404, "用户不存在");
        if ("ADMIN".equalsIgnoreCase(u.getRole())) {
            throw new BizException(403, "不能修改管理员账户状态");
        }
        u.setStatus(status);
        userMapper.updateById(u);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        User u = userMapper.selectById(id);
        if (u == null) return Result.ok();
        if ("ADMIN".equalsIgnoreCase(u.getRole())) {
            throw new BizException(403, "不能删除管理员账户");
        }
        userMapper.deleteById(id);
        return Result.ok();
    }
}
