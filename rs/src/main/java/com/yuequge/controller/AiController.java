package com.yuequge.controller;

import com.yuequge.common.Result;
import com.yuequge.entity.AiMessage;
import com.yuequge.exception.BizException;
import com.yuequge.service.AiChatService;
import com.yuequge.util.UserContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 对话 API（DeepSeek）。
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public Result<AiMessage> chat(@RequestBody ChatReq req) {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        return Result.ok(aiChatService.chat(u.userId().intValue(),
                req.getConversationId(), req.getContent()));
    }

    @GetMapping("/conversations")
    public Result<List<AiChatService.Conversation>> list() {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        return Result.ok(aiChatService.listConversations(u.userId().intValue()));
    }

    @GetMapping("/conversations/{id}/messages")
    public Result<List<AiMessage>> history(@PathVariable("id") String conversationId) {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        return Result.ok(aiChatService.history(u.userId().intValue(), conversationId, 200));
    }

    @PostMapping("/conversations/new")
    public Result<String> newConversation() {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        return Result.ok(aiChatService.newConversationId());
    }

    @Data
    public static class ChatReq {
        private String conversationId;
        private String content;
    }
}
