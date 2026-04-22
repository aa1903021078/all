package com.yuequge.interceptor;

import com.yuequge.exception.BizException;
import com.yuequge.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员角色校验拦截器（需在 JwtInterceptor 之后）。
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        var u = UserContext.get();
        if (u == null || !u.isAdmin()) {
            throw new BizException(403, "需要管理员权限");
        }
        return true;
    }
}
