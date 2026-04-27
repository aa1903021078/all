package com.dahaiwuliang.controller;

import com.dahaiwuliang.aop.LogOp;
import com.dahaiwuliang.common.R;
import com.dahaiwuliang.entity.SysUser;
import com.dahaiwuliang.security.CurrentUser;
import com.dahaiwuliang.service.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @LogOp(action = "用户登录", target = "user")
    public R<Map<String, Object>> login(@RequestBody LoginReq req) {
        return R.ok(authService.login(req.getUsername(), req.getPassword()));
    }

    @PostMapping("/register")
    @LogOp(action = "患者注册", target = "user")
    public R<SysUser> register(@RequestBody RegisterReq req) {
        return R.ok(authService.register(req.getUsername(), req.getPassword(),
                req.getRealName(), req.getPhone(), req.getIdCard()));
    }

    @GetMapping("/me")
    public R<SysUser> me() {
        return R.ok(authService.me(CurrentUser.id()));
    }

    @Data
    public static class LoginReq {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @Data
    public static class RegisterReq {
        @NotBlank private String username;
        @NotBlank private String password;
        private String realName;
        private String phone;
        private String idCard;
    }
}
