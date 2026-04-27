package com.dahaiwuliang.security;

import com.alibaba.fastjson2.JSON;
import com.dahaiwuliang.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            String token = h.substring(7);
            try {
                JwtUtil.TokenInfo info = jwtUtil.parse(token);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        info, null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + info.getRole())));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                resp.setStatus(401);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(JSON.toJSONString(R.fail(401, "登录已过期或令牌无效")));
                return;
            }
        }
        chain.doFilter(req, resp);
    }
}
