package com.yuequge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuequge.entity.ChatMessage;
import com.yuequge.entity.ChatSession;
import com.yuequge.mapper.ChatMessageMapper;
import com.yuequge.mapper.ChatSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话/消息持久化服务。
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;

    /** 查找或创建两人会话（user_a 固定为较小 id，保证唯一索引）。 */
    @Transactional
    public ChatSession findOrCreate(int userA, int userB, boolean support) {
        int a = Math.min(userA, userB);
        int b = Math.max(userA, userB);
        ChatSession s = sessionMapper.selectOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserA, a)
                .eq(ChatSession::getUserB, b)
                .last("LIMIT 1"));
        if (s != null) {
            if (support && (s.getIsSupport() == null || s.getIsSupport() == 0)) {
                s.setIsSupport(1);
                sessionMapper.updateById(s);
            }
            return s;
        }
        s = new ChatSession();
        s.setUserA(a);
        s.setUserB(b);
        s.setIsSupport(support ? 1 : 0);
        s.setLastTime(LocalDateTime.now());
        sessionMapper.insert(s);
        return s;
    }

    @Transactional
    public ChatMessage saveMessage(Long sessionId, int senderId, int receiverId, String content, Integer msgType) {
        ChatMessage m = new ChatMessage();
        m.setSessionId(sessionId);
        m.setSenderId(senderId);
        m.setReceiverId(receiverId);
        m.setContent(content);
        m.setMsgType(msgType == null ? 0 : msgType);
        m.setStatus(0);
        m.setCreateTime(LocalDateTime.now());
        messageMapper.insert(m);

        ChatSession s = sessionMapper.selectById(sessionId);
        if (s != null) {
            s.setLastMessage(content == null ? "" : (content.length() > 200 ? content.substring(0, 200) : content));
            s.setLastTime(m.getCreateTime());
            sessionMapper.updateById(s);
        }
        return m;
    }

    public List<ChatMessage> historyBetween(int userA, int userB, int limit) {
        ChatSession s = sessionMapper.selectOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserA, Math.min(userA, userB))
                .eq(ChatSession::getUserB, Math.max(userA, userB))
                .last("LIMIT 1"));
        if (s == null) return List.of();
        List<ChatMessage> list = messageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, s.getSessionId())
                .orderByDesc(ChatMessage::getCreateTime)
                .last("LIMIT " + Math.max(1, Math.min(limit, 500))));
        java.util.Collections.reverse(list);
        return list;
    }

    public List<ChatSession> sessionsOf(int userId) {
        return sessionMapper.selectList(new LambdaQueryWrapper<ChatSession>()
                .and(w -> w.eq(ChatSession::getUserA, userId).or().eq(ChatSession::getUserB, userId))
                .orderByDesc(ChatSession::getLastTime));
    }

    public List<ChatSession> supportSessions() {
        return sessionMapper.selectList(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getIsSupport, 1)
                .orderByDesc(ChatSession::getLastTime));
    }
}
