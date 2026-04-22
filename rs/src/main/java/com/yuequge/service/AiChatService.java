package com.yuequge.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuequge.config.AiProperties;
import com.yuequge.entity.AiMessage;
import com.yuequge.exception.BizException;
import com.yuequge.mapper.AiMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DeepSeek AI 对话服务：上下文拼接 + 调用远端 + 落库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiProperties props;
    private final AiMessageMapper mapper;

    public String newConversationId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public List<AiMessage> history(int userId, String conversationId, int limit) {
        if (conversationId == null || conversationId.isBlank()) return List.of();
        List<AiMessage> list = mapper.selectList(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getUserId, userId)
                .eq(AiMessage::getConversationId, conversationId)
                .orderByAsc(AiMessage::getId)
                .last("LIMIT " + Math.max(1, Math.min(limit, 200))));
        return list;
    }

    /** 用户问一句 -> 返回 AI 回复 -> 同时落库 user + assistant 两条。 */
    public AiMessage chat(int userId, String conversationId, String userContent) {
        if (!props.isEnabled()) throw new BizException("AI 未启用");
        if (userContent == null || userContent.isBlank()) throw new BizException("内容为空");
        if (conversationId == null || conversationId.isBlank()) conversationId = newConversationId();

        // 组装消息：system + 历史 + 本次 user
        JSONArray messages = new JSONArray();
        JSONObject sys = new JSONObject();
        sys.put("role", "system");
        sys.put("content", props.getSystemPrompt());
        messages.add(sys);
        for (AiMessage h : history(userId, conversationId, 20)) {
            JSONObject o = new JSONObject();
            o.put("role", h.getRole());
            o.put("content", h.getContent());
            messages.add(o);
        }
        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content", userContent);
        messages.add(user);

        JSONObject body = new JSONObject();
        body.put("model", props.getModel());
        body.put("messages", messages);
        body.put("stream", false);

        // 先写入用户消息
        AiMessage userMsg = save(userId, conversationId, "user", userContent);

        String reply;
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(props.getApiUrl()))
                    .timeout(Duration.ofSeconds(Math.max(5, props.getTimeout())))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + props.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                log.warn("[ai] http {} body={}", resp.statusCode(), trim(resp.body()));
                reply = "AI 服务当前不可用（HTTP " + resp.statusCode() + "），请稍后再试。";
            } else {
                JSONObject json = JSON.parseObject(resp.body());
                JSONArray choices = json.getJSONArray("choices");
                reply = choices != null && !choices.isEmpty()
                        ? choices.getJSONObject(0).getJSONObject("message").getString("content")
                        : "（AI 未返回内容）";
            }
        } catch (Exception e) {
            log.warn("[ai] call failed: {}", e.getMessage());
            reply = "AI 服务暂不可达：" + e.getMessage();
        }
        return save(userId, conversationId, "assistant", reply);
    }

    private AiMessage save(int userId, String conversationId, String role, String content) {
        AiMessage m = new AiMessage();
        m.setUserId(userId);
        m.setConversationId(conversationId);
        m.setRole(role);
        m.setContent(content);
        m.setCreateTime(LocalDateTime.now());
        mapper.insert(m);
        return m;
    }

    /** 列出当前用户的会话 id + 首条用户消息（作为标题）。 */
    public List<Conversation> listConversations(int userId) {
        // 用 MyBatis-Plus 做简单聚合：按 conversation_id 取每组最早 id 的 content 做标题
        List<AiMessage> all = mapper.selectList(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getUserId, userId)
                .orderByAsc(AiMessage::getId));
        java.util.LinkedHashMap<String, Conversation> map = new java.util.LinkedHashMap<>();
        for (AiMessage m : all) {
            Conversation c = map.computeIfAbsent(m.getConversationId(), k -> {
                Conversation x = new Conversation();
                x.conversationId = k;
                x.title = m.getRole().equals("user")
                        ? (m.getContent().length() > 30 ? m.getContent().substring(0, 30) : m.getContent())
                        : "新对话";
                x.createTime = m.getCreateTime();
                return x;
            });
            c.lastTime = m.getCreateTime();
        }
        List<Conversation> list = new ArrayList<>(map.values());
        list.sort((a, b) -> b.lastTime.compareTo(a.lastTime));
        return list;
    }

    public static class Conversation {
        public String conversationId;
        public String title;
        public LocalDateTime createTime;
        public LocalDateTime lastTime;
    }

    private static String trim(String s) {
        if (s == null) return "";
        return s.length() > 500 ? s.substring(0, 500) : s;
    }
}
