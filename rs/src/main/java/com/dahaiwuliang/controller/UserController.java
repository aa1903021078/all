package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.common.Result;
import com.dahaiwuliang.entity.User;
import com.dahaiwuliang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        User loginUser = userService.login(user.getUsername(), user.getPassword());
        if (loginUser == null) {
            return Result.error("用户名或密码错误");
        }
        if (loginUser.getStatus() != null && loginUser.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }
        return Result.ok(loginUser);
    }

    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        User existUser = userService.findByUsername(user.getUsername());
        if (existUser != null) {
            return Result.error("用户名已存在");
        }
        user.setRole(4);
        user.setStatus(1);
        userService.save(user);
        return Result.ok();
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable Integer id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.ok(user);
    }

    @PutMapping("/update")
    public Result update(@RequestBody User user) {
        userService.updateById(user);
        return Result.ok();
    }

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") int pageNum,
                       @RequestParam(defaultValue = "10") int pageSize,
                       @RequestParam(required = false) Integer role,
                       @RequestParam(required = false) String username) {
        Page<User> page = userService.list(pageNum, pageSize, role, username);
        return Result.ok(page);
    }

    @PostMapping("/add")
    public Result add(@RequestBody User user) {
        User existUser = userService.findByUsername(user.getUsername());
        if (existUser != null) {
            return Result.error("用户名已存在");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        userService.save(user);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        userService.removeById(id);
        return Result.ok();
    }

    @GetMapping("/staff")
    public Result staff() {
        List<User> staffList = userService.findStaff();
        return Result.ok(staffList);
    }
}
