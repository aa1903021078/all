package com.dahaiwuliang.ws;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.dahaiwuliang.entity.ChatMessage;
import com.dahaiwuliang.entity.ChatSession;
import com.dahaiwuliang.service.ChatService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天 WebSocket 处理器: 在线用户 -> 会话, 实时转发并落库
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    /** userId -> 连接 */
    private static final Map<Long, WebSocketSession> ONLINE = new ConcurrentHashMap<>();

    private final ChatService chatService;

    public ChatWebSocketHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            ONLINE.put(userId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long fromUserId = (Long) session.getAttributes().get("userId");
        if (fromUserId == null) {
            return;
        }
        JSONObject payload;
        try {
            payload = JSON.parseObject(message.getPayload());
        } catch (Exception e) {
            return;
        }
        if (payload == null || payload.getLong("sessionId") == null) {
            return;
        }
        Long sessionId = payload.getLong("sessionId");
        String content = payload.getString("content");
        String type = payload.getString("type");
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        ChatSession chatSession = chatService.getSession(sessionId);
        if (chatSession == null) {
            return;
        }
        if (!fromUserId.equals(chatSession.getUserId()) && !fromUserId.equals(chatSession.getMerchantId())) {
            return;
        }
        Long toUserId = fromUserId.equals(chatSession.getUserId())
                ? chatSession.getMerchantId() : chatSession.getUserId();

        ChatMessage saved = chatService.saveMessage(sessionId, fromUserId, toUserId, content, type);
        String out = JSON.toJSONString(saved);
        // 回显给发送者
        send(session, out);
        // 推送给接收者(在线才推)
        WebSocketSession target = ONLINE.get(toUserId);
        if (target != null && target.isOpen()) {
            send(target, out);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            WebSocketSession cur = ONLINE.get(userId);
            if (cur != null && cur.getId().equals(session.getId())) {
                ONLINE.remove(userId);
            }
        }
    }

    private void send(WebSocketSession session, String text) {
        try {
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(text));
                }
            }
        } catch (IOException ignored) {
        }
    }
}
