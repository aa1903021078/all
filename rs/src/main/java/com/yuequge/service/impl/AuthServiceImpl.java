package com.yuequge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuequge.config.JwtProperties;
import com.yuequge.dto.LoginDTO;
import com.yuequge.dto.LoginVO;
import com.yuequge.dto.RegisterDTO;
import com.yuequge.entity.User;
import com.yuequge.exception.BizException;
import com.yuequge.interceptor.JwtInterceptor;
import com.yuequge.mapper.UserMapper;
import com.yuequge.service.AuthService;
import com.yuequge.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BizException(401, "用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(403, "账号已注销");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(401, "密码错误");
        }
        String token = jwtUtil.generate(user.getUserId(), user.getUsername(), user.getRole());
        return new LoginVO(token, user.getUserId(), user.getUsername(), user.getRole(), user.getAvatar());
    }

    @Override
    public User register(RegisterDTO dto) {
        Long cnt = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (cnt != null && cnt > 0) {
            throw new BizException("用户名已存在");
        }
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            Long phoneCnt = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, dto.getPhone()));
            if (phoneCnt != null && phoneCnt > 0) {
                throw new BizException("手机号已被注册");
            }
        }
        User u = new User();
        u.setUsername(dto.getUsername());
        u.setPassword(passwordEncoder.encode(dto.getPassword()));
        u.setPhone(dto.getPhone());
        u.setRole("USER");
        u.setStatus(1);
        u.setCreateTime(LocalDateTime.now());
        userMapper.insert(u);
        u.setPassword(null);
        return u;
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isBlank()) return;
        if (token.startsWith(jwtProperties.getPrefix())) {
            token = token.substring(jwtProperties.getPrefix().length());
        }
        try {
            Claims claims = jwtUtil.parse(token);
            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        JwtInterceptor.BLACKLIST_PREFIX + token, "1", Duration.ofMillis(ttl));
            }
        } catch (Exception e) {
            log.debug("logout token invalid, skip: {}", e.getMessage());
        }
    }
}
