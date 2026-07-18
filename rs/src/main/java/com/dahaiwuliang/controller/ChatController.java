package com.dahaiwuliang.controller;

import com.dahaiwuliang.common.R;
import com.dahaiwuliang.common.UserContext;
import com.dahaiwuliang.common.annotation.RequireLogin;
import com.dahaiwuliang.entity.ChatMessage;
import com.dahaiwuliang.entity.ChatSession;
import com.dahaiwuliang.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天接口(会话/历史消息, 实时收发走 WebSocket /api/ws/chat)
 */
@RestController
@RequestMapping("/chat")
@RequireLogin
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /** 顾客向店铺发起咨询, 获取/创建会话 */
    @PostMapping("/session")
    public R<ChatSession> session(@RequestParam Long shopId) {
        return R.ok(chatService.getOrCreateSession(UserContext.requireUserId(), shopId));
    }

    /** 我的会话列表 */
    @GetMapping("/sessions")
    public R<List<ChatSession>> sessions() {
        return R.ok(chatService.listSessions(UserContext.requireUserId()));
    }

    /** 会话历史消息 */
    @GetMapping("/sessions/{id}/messages")
    public R<List<ChatMessage>> messages(@PathVariable Long id) {
        return R.ok(chatService.history(id, UserContext.requireUserId()));
    }

    /** 标记已读 */
    @PutMapping("/sessions/{id}/read")
    public R<Void> read(@PathVariable Long id) {
        chatService.markRead(id, UserContext.requireUserId());
        return R.ok();
    }
}
