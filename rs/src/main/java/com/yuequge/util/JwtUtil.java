package com.yuequge.util;

import com.yuequge.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类。
 */
@Component
public class JwtUtil {

    private final JwtProperties props;
    private final SecretKey key;

    @Autowired
    public JwtUtil(JwtProperties props) {
        this.props = props;
        if (props.getSecret() == null) {
            throw new IllegalStateException("jwt.secret must be configured");
        }
        byte[] bytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "jwt.secret must be at least 32 bytes (got " + bytes.length + "). " +
                    "Configure a strong secret via JWT_SECRET environment variable.");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String generate(Long userId, String username, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.getExpireHours() * 3600_000L);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
