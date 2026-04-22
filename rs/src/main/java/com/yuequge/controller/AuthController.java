package com.yuequge.controller;

import com.yuequge.common.Result;
import com.yuequge.config.JwtProperties;
import com.yuequge.config.TestUsersProperties;
import com.yuequge.dto.LoginDTO;
import com.yuequge.dto.LoginVO;
import com.yuequge.dto.RegisterDTO;
import com.yuequge.entity.User;
import com.yuequge.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证相关接口（登录/注册/注销）。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TestUsersProperties testUsersProperties;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody @Valid RegisterDTO dto) {
        return Result.ok(authService.register(dto));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = request.getHeader(jwtProperties.getHeader());
        authService.logout(token);
        return Result.ok();
    }

    /** 登录页展示测试账号（供回填）。 */
    @GetMapping("/test-users")
    public Result<List<TestUsersProperties.TestUser>> testUsers() {
        return Result.ok(testUsersProperties.getTestUsers());
    }
}
