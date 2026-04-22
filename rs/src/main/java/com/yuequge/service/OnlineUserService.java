package com.yuequge.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线用户注册表：基于 STOMP 连接/断开事件维护，广播到 /topic/online。
 */
@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private final SimpMessagingTemplate template;

    /** username -> count (同一用户可能多 tab)。 */
    private final ConcurrentHashMap<String, Integer> online = new ConcurrentHashMap<>();

    @EventListener
    public void onConnect(SessionConnectedEvent event) {
        Principal p = event.getUser();
        if (p == null) return;
        online.merge(p.getName(), 1, Integer::sum);
        broadcast();
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        Principal p = event.getUser();
        if (p == null) return;
        online.compute(p.getName(), (k, v) -> (v == null || v <= 1) ? null : v - 1);
        broadcast();
    }

    public List<OnlineUser> listOnline() {
        return online.keySet().stream()
                .map(name -> {
                    OnlineUser u = new OnlineUser();
                    u.setUsername(name);
                    return u;
                })
                .sorted(Comparator.comparing(OnlineUser::getUsername))
                .toList();
    }

    public boolean isOnline(String username) {
        return username != null && online.containsKey(username);
    }

    private void broadcast() {
        template.convertAndSend("/topic/online", listOnline());
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OnlineUser {
        private String username;
    }
}
