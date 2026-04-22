package com.yuequge.interceptor;

import com.yuequge.config.JwtProperties;
import com.yuequge.exception.BizException;
import com.yuequge.util.JwtUtil;
import com.yuequge.util.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtProperties props;
    private final StringRedisTemplate redisTemplate;

    public static final String BLACKLIST_PREFIX = "yuequge:jwt:blacklist:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader(props.getHeader());
        if (token == null || token.isBlank()) {
            throw new BizException(401, "未登录");
        }
        if (token.startsWith(props.getPrefix())) {
            token = token.substring(props.getPrefix().length());
        }
        // 黑名单校验
        try {
            Boolean inBlack = redisTemplate.hasKey(BLACKLIST_PREFIX + token);
            if (Boolean.TRUE.equals(inBlack)) {
                throw new BizException(401, "token 已失效，请重新登录");
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception ex) {
            // Redis 未启动时降级，不影响业务
            log.debug("redis blacklist check skipped: {}", ex.getMessage());
        }
        try {
            Claims claims = jwtUtil.parse(token);
            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);
            UserContext.set(new UserContext.CurrentUser(userId, username, role));
        } catch (Exception e) {
            throw new BizException(401, "token 解析失败：" + e.getMessage());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
