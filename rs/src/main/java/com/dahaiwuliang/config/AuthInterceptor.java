package com.dahaiwuliang.config;

import com.alibaba.fastjson2.JSON;
import com.dahaiwuliang.common.LoginUser;
import com.dahaiwuliang.common.R;
import com.dahaiwuliang.common.UserContext;
import com.dahaiwuliang.common.annotation.RequireLogin;
import com.dahaiwuliang.common.annotation.RequirePerm;
import com.dahaiwuliang.common.annotation.RequireRole;
import com.dahaiwuliang.util.JwtUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 鉴权拦截器: 解析 token 写入上下文, 并按注解校验登录/角色/权限
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final FoodieProperties properties;

    public AuthInterceptor(JwtUtil jwtUtil, FoodieProperties properties) {
        this.jwtUtil = jwtUtil;
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 1. 解析 token 写入上下文(存在即写, 不强制)
        String token = request.getHeader(properties.getJwt().getHeader());
        if (StrUtil.isNotBlank(token)) {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            LoginUser user = jwtUtil.parseToken(token);
            if (user != null) {
                UserContext.set(user);
            }
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod hm = (HandlerMethod) handler;

        RequireLogin requireLogin = getAnnotation(hm, RequireLogin.class);
        RequireRole requireRole = getAnnotation(hm, RequireRole.class);
        RequirePerm requirePerm = getAnnotation(hm, RequirePerm.class);
        LoginUser current = UserContext.get();

        // 2. 登录校验
        if ((requireLogin != null || requireRole != null || requirePerm != null) && current == null) {
            writeJson(response, R.fail(401, "请先登录"));
            return false;
        }
        // 3. 角色校验(管理员放行)
        if (requireRole != null && current != null && !current.isAdmin()) {
            boolean ok = Arrays.stream(requireRole.value()).anyMatch(current::hasRole);
            if (!ok) {
                writeJson(response, R.fail(403, "无权限访问"));
                return false;
            }
        }
        // 4. 权限校验(管理员放行)
        if (requirePerm != null && current != null && !current.isAdmin()) {
            boolean ok = Arrays.stream(requirePerm.value()).anyMatch(current::hasPerm);
            if (!ok) {
                writeJson(response, R.fail(403, "无权限访问"));
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private <A extends Annotation> A getAnnotation(HandlerMethod hm, Class<A> clazz) {
        A anno = hm.getMethodAnnotation(clazz);
        if (anno == null) {
            anno = hm.getBeanType().getAnnotation(clazz);
        }
        return anno;
    }

    private void writeJson(HttpServletResponse response, R<?> r) throws IOException {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JSON.toJSONString(r));
    }
}
