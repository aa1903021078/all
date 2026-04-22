package com.yuequge.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuequge.common.Result;
import com.yuequge.entity.ChatMessage;
import com.yuequge.entity.ChatSession;
import com.yuequge.entity.User;
import com.yuequge.exception.BizException;
import com.yuequge.mapper.UserMapper;
import com.yuequge.service.ChatService;
import com.yuequge.service.OnlineUserService;
import com.yuequge.util.UserContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天接口：HTTP 用于拉历史/会话列表；WebSocket STOMP 用于实时消息。
 *
 * <p>客户端订阅 {@code /user/queue/chat} 接收私聊，订阅 {@code /topic/online} 接收在线列表。
 * <p>客户端发送到 {@code /app/chat.send}，服务端转发到接收者 {@code /user/{username}/queue/chat} 并落库。
 */
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final OnlineUserService onlineUserService;
    private final UserMapper userMapper;
    private final SimpMessagingTemplate template;

    // ===== HTTP =====

    @GetMapping("/api/chat/online")
    public Result<List<OnlineUserService.OnlineUser>> online() {
        return Result.ok(onlineUserService.listOnline());
    }

    @GetMapping("/api/chat/sessions")
    public Result<List<Map<String, Object>>> mySessions() {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        List<ChatSession> list = chatService.sessionsOf(u.userId().intValue());
        return Result.ok(enrich(list, u.userId().intValue()));
    }

    @GetMapping("/api/chat/history")
    public Result<List<ChatMessage>> history(@RequestParam Integer peerId,
                                             @RequestParam(defaultValue = "100") int limit) {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        return Result.ok(chatService.historyBetween(u.userId().intValue(), peerId, limit));
    }

    /** HTTP 方式发送消息（WS 不可用时兜底）。 */
    @PostMapping("/api/chat/send")
    public Result<ChatMessage> send(@RequestBody ChatSendDTO dto) {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        return Result.ok(doSend(u.userId().intValue(), u.username(), dto));
    }

    /** 客服会话入口：自动找到管理员用户并建立 is_support=1 的会话。 */
    @PostMapping("/api/support/start")
    public Result<Map<String, Object>> startSupport() {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        User admin = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "ADMIN").last("LIMIT 1"));
        if (admin == null) throw new BizException(500, "客服暂不可用");
        ChatSession s = chatService.findOrCreate(u.userId().intValue(), admin.getUserId().intValue(), true);
        Map<String, Object> m = new HashMap<>();
        m.put("sessionId", s.getSessionId());
        m.put("peerId", admin.getUserId());
        m.put("peerName", admin.getUsername());
        return Result.ok(m);
    }

    // ===== 管理员客服后台 =====

    @GetMapping("/api/admin/support/sessions")
    public Result<List<Map<String, Object>>> supportList() {
        var u = UserContext.get();
        if (u == null || !u.isAdmin()) throw new BizException(403, "无权访问");
        return Result.ok(enrich(chatService.supportSessions(), u.userId().intValue()));
    }

    // ===== STOMP =====

    @MessageMapping("/chat.send")
    public void wsSend(@Payload ChatSendDTO dto, Principal principal) {
        if (principal == null) return;
        // principal.getName() == username；需要 userId，查一次 user 表
        User sender = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, principal.getName()).last("LIMIT 1"));
        if (sender == null) return;
        doSend(sender.getUserId().intValue(), sender.getUsername(), dto);
    }

    // ===== helpers =====

    private ChatMessage doSend(int senderId, String senderName, ChatSendDTO dto) {
        if (dto == null || dto.getReceiverId() == null) throw new BizException("参数错误");
        if (dto.getContent() == null || dto.getContent().isBlank()) throw new BizException("内容为空");
        User receiver = userMapper.selectById(dto.getReceiverId().longValue());
        if (receiver == null) throw new BizException(404, "接收用户不存在");

        boolean support = dto.isSupport()
                || "ADMIN".equalsIgnoreCase(receiver.getRole())
                || UserContext.get() != null && UserContext.get().isAdmin();
        ChatSession s = chatService.findOrCreate(senderId, dto.getReceiverId(), support);
        ChatMessage m = chatService.saveMessage(s.getSessionId(), senderId, dto.getReceiverId(),
                dto.getContent(), dto.getMsgType());

        // 实时推送给接收者
        Map<String, Object> payload = new HashMap<>();
        payload.put("messageId", m.getMessageId());
        payload.put("sessionId", m.getSessionId());
        payload.put("senderId", senderId);
        payload.put("senderName", senderName);
        payload.put("receiverId", dto.getReceiverId());
        payload.put("content", m.getContent());
        payload.put("msgType", m.getMsgType());
        payload.put("createTime", m.getCreateTime().toString());
        template.convertAndSendToUser(receiver.getUsername(), "/queue/chat", payload);
        // 回显给发送者
        template.convertAndSendToUser(senderName, "/queue/chat", payload);
        return m;
    }

    private List<Map<String, Object>> enrich(List<ChatSession> list, int currentUserId) {
        return list.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("sessionId", s.getSessionId());
            m.put("isSupport", s.getIsSupport());
            m.put("lastMessage", s.getLastMessage());
            m.put("lastTime", s.getLastTime() == null ? null : s.getLastTime().toString());
            int peerId = s.getUserA() == currentUserId ? s.getUserB() : s.getUserA();
            m.put("peerId", peerId);
            User peer = userMapper.selectById((long) peerId);
            if (peer != null) {
                m.put("peerName", peer.getUsername());
                m.put("peerAvatar", peer.getAvatar());
                m.put("peerRole", peer.getRole());
                m.put("peerOnline", onlineUserService.isOnline(peer.getUsername()));
            }
            return m;
        }).toList();
    }

    @Data
    public static class ChatSendDTO {
        private Integer receiverId;
        private String content;
        private Integer msgType;
        private boolean support;
    }
}
