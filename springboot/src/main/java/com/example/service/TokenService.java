package com.example.service;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 极简Token服务（不依赖JWT库）
 * 使用内存Map存储token-userId映射（生产环境应使用Redis）
 */
@Component
public class TokenService {

    // token -> userId 的映射
    private static final Map<String, Integer> tokenMap = new ConcurrentHashMap<>();

    /**
     * 创建token
     */
    public String createToken(Integer userId) {
        String token = "token-" + UUID.randomUUID().toString().replace("-", "");
        tokenMap.put(token, userId);
        return token;
    }

    /**
     * 验证并获取用户ID
     */
    public Integer getUserIdFromToken(String token) {
        return tokenMap.get(token);
    }

    /**
     * 删除token（退出登录）
     */
    public void removeToken(String token) {
        tokenMap.remove(token);
    }
}