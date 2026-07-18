package com.dahaiwuliang.config;

import cn.hutool.core.util.StrUtil;
import com.dahaiwuliang.common.LoginUser;
import com.dahaiwuliang.util.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手鉴权: 从 ?token= 解析登录用户并写入 session 属性
 */
@Component
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    public ChatHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = null;
        if (request instanceof ServletServerHttpRequest) {
            token = ((ServletServerHttpRequest) request).getServletRequest().getParameter("token");
        }
        if (StrUtil.isBlank(token)) {
            return false;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        LoginUser user = jwtUtil.parseToken(token);
        if (user == null || user.getId() == null) {
            return false;
        }
        attributes.put("userId", user.getId());
        attributes.put("nickname", user.getNickname());
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
