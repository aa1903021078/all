package com.dahaiwuliang.util;

import com.dahaiwuliang.common.LoginUser;
import com.dahaiwuliang.config.FoodieProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT 工具
 */
@Slf4j
@Component
public class JwtUtil {

    private final FoodieProperties properties;
    private final SecretKey key;

    public JwtUtil(FoodieProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(LoginUser user) {
        long now = System.currentTimeMillis();
        Date exp = new Date(now + properties.getJwt().getExpire() * 1000);
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .claim("nickname", user.getNickname())
                .claim("avatar", user.getAvatar())
                .claim("roles", user.getRoles())
                .claim("perms", user.getPerms())
                .issuedAt(new Date(now))
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    @SuppressWarnings("unchecked")
    public LoginUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
            LoginUser user = new LoginUser();
            Object userId = claims.get("userId");
            user.setId(userId == null ? null : Long.valueOf(userId.toString()));
            user.setUsername(claims.get("username", String.class));
            user.setNickname(claims.get("nickname", String.class));
            user.setAvatar(claims.get("avatar", String.class));
            user.setRoles((List<String>) claims.get("roles", List.class));
            user.setPerms((List<String>) claims.get("perms", List.class));
            return user;
        } catch (Exception e) {
            log.debug("解析 token 失败: {}", e.getMessage());
            return null;
        }
    }
}
