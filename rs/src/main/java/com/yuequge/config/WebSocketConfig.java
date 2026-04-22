package com.yuequge.config;

import com.yuequge.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket + STOMP 配置。
 *
 * <p>握手时从 query 参数 {@code ?token=xxx} 读取 JWT，解析出 username 作为 Principal.name，
 * 后续 convertAndSendToUser 即可按用户名精确推送。
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // /topic 广播（在线用户列表）；/queue 点对点；应用前缀 /app
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        org.springframework.web.socket.server.support.DefaultHandshakeHandler handshake =
                new org.springframework.web.socket.server.support.DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request,
                                                      WebSocketHandler wsHandler,
                                                      Map<String, Object> attributes) {
                        Object p = attributes.get("principal");
                        if (p instanceof Principal pr) return pr;
                        return super.determineUser(request, wsHandler, attributes);
                    }
                };
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(handshake)
                .addInterceptors(new JwtHandshakeInterceptor(jwtUtil))
                .withSockJS();
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(handshake)
                .addInterceptors(new JwtHandshakeInterceptor(jwtUtil));
    }

    /** 从 query 读取 token 并注入 Principal；失败 -> 拒绝握手。 */
    static class JwtHandshakeInterceptor implements HandshakeInterceptor {
        private final JwtUtil jwtUtil;
        JwtHandshakeInterceptor(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }

        @Override
        public boolean beforeHandshake(ServerHttpRequest req, ServerHttpResponse resp,
                                       WebSocketHandler handler, Map<String, Object> attrs) {
            try {
                String token = null;
                if (req instanceof ServletServerHttpRequest s) {
                    token = s.getServletRequest().getParameter("token");
                }
                if (token == null || token.isBlank()) {
                    log.debug("ws handshake rejected: no token");
                    return false;
                }
                Claims c = jwtUtil.parse(token);
                String uid = c.getSubject();
                String username = c.get("username", String.class);
                String role = c.get("role", String.class);
                Principal principal = new StompPrincipal(uid, username, role);
                attrs.put("principal", principal);
                attrs.put("userId", Long.valueOf(uid));
                attrs.put("username", username);
                attrs.put("role", role);
                return true;
            } catch (Exception e) {
                log.debug("ws handshake rejected: {}", e.getMessage());
                return false;
            }
        }

        @Override
        public void afterHandshake(ServerHttpRequest req, ServerHttpResponse resp,
                                   WebSocketHandler handler, Exception ex) {}
    }

    public record StompPrincipal(String userId, String username, String role) implements Principal {
        @Override public String getName() { return username; }
    }
}
