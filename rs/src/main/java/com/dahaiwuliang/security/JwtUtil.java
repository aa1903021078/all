package com.dahaiwuliang.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expire-hours:24}")
    private long expireHours;

    private SecretKey key() {
        byte[] bytes;
        try { bytes = Base64.getDecoder().decode(secret); }
        catch (IllegalArgumentException e) { bytes = secret.getBytes(); }
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generate(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", userId);
        claims.put("role", role);
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .addClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expireHours * 3600_000L))
                .signWith(key())
                .compact();
    }

    public TokenInfo parse(String token) {
        Claims c = Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
        TokenInfo info = new TokenInfo();
        info.setUsername(c.getSubject());
        Object uid = c.get("uid");
        info.setUserId(uid == null ? null : ((Number) uid).longValue());
        info.setRole((String) c.get("role"));
        return info;
    }

    @Data
    public static class TokenInfo {
        private Long userId;
        private String username;
        private String role;
    }
}
