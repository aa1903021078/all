package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.Collector;
import com.example.entity.User;
import com.example.service.AdminService;
import com.example.service.CollectorService;
import com.example.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 通用Web接口（登录，注册，修改密码）
 */
@RestController
public class WebController {

    @Resource
    private AdminService adminService;
    @Resource
    private UserService userService;
    @Resource
    private CollectorService collectorService;


    /**
     * 默认请求接口
     */
    @GetMapping("/")
    public Result hello() {
        return Result.success();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        Account ac = null;
        if ("管理员".equals(account.getRole())) {
            ac = adminService.login(account);
        } else if ("普通用户".equals(account.getRole())) {
            User user = new User();
            user.setUsername(account.getUsername());
            user.setPassword(account.getPassword());
            ac = userService.login(user);
        } else if ("回收员".equals(account.getRole())) {
            Collector collector = new Collector();
            collector.setUsername(account.getUsername());
            collector.setPassword(account.getPassword());
            ac = collectorService.login(collector);
        }

        if (ac == null) {
            return Result.error("登录失败，用户不存在");
        }
        return Result.success(ac);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        // 校验两次密码是否一致
        if (!user.getPassword().equals(user.getConfirmPassword())){
            return Result.error("两次输入的密码不一致");
        }
        userService.register(user);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        if ("管理员".equals(account.getRole())) {
            adminService.updatePassword(account);
        } else if ("普通用户".equals(account.getRole())) {
            User user = new User();
            user.setUsername(account.getUsername());
            user.setPassword(account.getPassword());
            user.setNewPassword(account.getNewPassword());
            userService.updatePassword(user);
        } else if ("回收员".equals(account.getRole())) {
            Collector collector = new Collector();
            collector.setUsername(account.getUsername());
            collector.setPassword(account.getPassword());
            collector.setNewPassword(account.getNewPassword());
            collectorService.updatePassword(collector);
        }
        return Result.success();
    }



}
