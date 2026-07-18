package com.dahaiwuliang.controller;

import com.dahaiwuliang.common.LoginUser;
import com.dahaiwuliang.common.R;
import com.dahaiwuliang.common.annotation.RequireLogin;
import com.dahaiwuliang.dto.LoginDTO;
import com.dahaiwuliang.dto.RegisterDTO;
import com.dahaiwuliang.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody @Valid LoginDTO dto) {
        return R.ok("登录成功", authService.login(dto));
    }

    @PostMapping("/register")
    public R<Void> register(@RequestBody @Valid RegisterDTO dto) {
        authService.register(dto);
        return R.ok("注册成功", null);
    }

    @GetMapping("/me")
    @RequireLogin
    public R<LoginUser> me() {
        return R.ok(authService.currentUser());
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok();
    }
}
