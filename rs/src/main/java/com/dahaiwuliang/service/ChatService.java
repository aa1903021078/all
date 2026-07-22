package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dahaiwuliang.common.BusinessException;
import com.dahaiwuliang.entity.ChatMessage;
import com.dahaiwuliang.entity.ChatSession;
import com.dahaiwuliang.entity.Shop;
import com.dahaiwuliang.entity.SysUser;
import com.dahaiwuliang.mapper.ChatMessageMapper;
import com.dahaiwuliang.mapper.ChatSessionMapper;
import com.dahaiwuliang.mapper.ShopMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 聊天会话 & 消息
 */
@Service
public class ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final ShopMapper shopMapper;
    private final UserService userService;

    public ChatService(ChatSessionMapper sessionMapper, ChatMessageMapper messageMapper,
                       ShopMapper shopMapper, UserService userService) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.shopMapper = shopMapper;
        this.userService = userService;
    }

    /** 顾客针对某店铺获取/创建会话 */
    @Transactional(rollbackFor = Exception.class)
    public ChatSession getOrCreateSession(Long userId, Long shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }
        Long merchantId = shop.getMerchantId();
        if (merchantId == null) {
            throw new BusinessException("该店铺暂未开通商家咨询");
        }
        ChatSession session = sessionMapper.selectOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getMerchantId, merchantId)
                .last("limit 1"));
        if (session == null) {
            session = new ChatSession();
            session.setUserId(userId);
            session.setMerchantId(merchantId);
            session.setShopId(shopId);
            session.setUserUnread(0);
            session.setMerchantUnread(0);
            session.setLastTime(LocalDateTime.now());
            sessionMapper.insert(session);
        }
        fillSessionView(java.util.Collections.singletonList(session), userId);
        return session;
    }

    /** 我的会话列表(顾客或商家视角) */
    public List<ChatSession> listSessions(Long currentUserId) {
        List<ChatSession> sessions = sessionMapper.selectList(new LambdaQueryWrapper<ChatSession>()
                .and(w -> w.eq(ChatSession::getUserId, currentUserId).or().eq(ChatSession::getMerchantId, currentUserId))
                .orderByDesc(ChatSession::getLastTime));
        fillSessionView(sessions, currentUserId);
        return sessions;
    }

    public ChatSession getSession(Long sessionId) {
        return sessionMapper.selectById(sessionId);
    }

    /** 会话消息记录, 并把发给我的标记已读 */
    @Transactional(rollbackFor = Exception.class)
    public List<ChatMessage> history(Long sessionId, Long currentUserId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        if (!currentUserId.equals(session.getUserId()) && !currentUserId.equals(session.getMerchantId())) {
            throw new BusinessException(403, "无权查看该会话");
        }
        List<ChatMessage> list = messageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId).orderByAsc(ChatMessage::getId));
        markRead(sessionId, currentUserId);
        return list;
    }

    /** 保存一条消息并更新会话 */
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage saveMessage(Long sessionId, Long fromUserId, Long toUserId, String content, String type) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setFromUserId(fromUserId);
        msg.setToUserId(toUserId);
        msg.setContent(content);
        msg.setType(type == null ? "TEXT" : type);
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(msg);

        session.setLastMessage(content);
        session.setLastTime(msg.getCreateTime());
        if (toUserId.equals(session.getUserId())) {
            session.setUserUnread((session.getUserUnread() == null ? 0 : session.getUserUnread()) + 1);
        } else {
            session.setMerchantUnread((session.getMerchantUnread() == null ? 0 : session.getMerchantUnread()) + 1);
        }
        sessionMapper.updateById(session);
        return msg;
    }

    /** 标记会话对当前用户已读 */
    public void markRead(Long sessionId, Long currentUserId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            return;
        }
        ChatMessage upd = new ChatMessage();
        upd.setIsRead(1);
        messageMapper.update(upd, new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getToUserId, currentUserId)
                .eq(ChatMessage::getIsRead, 0));
        if (currentUserId.equals(session.getUserId())) {
            session.setUserUnread(0);
        } else if (currentUserId.equals(session.getMerchantId())) {
            session.setMerchantUnread(0);
        }
        sessionMapper.updateById(session);
    }

    /** 回填对端信息 / 店铺名 / 当前视角未读数 */
    private void fillSessionView(List<ChatSession> sessions, Long currentUserId) {
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        Set<Long> peerIds = sessions.stream()
                .map(s -> currentUserId.equals(s.getUserId()) ? s.getMerchantId() : s.getUserId())
                .filter(id -> id != null).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = userService.mapByIds(peerIds);

        Set<Long> shopIds = sessions.stream().map(ChatSession::getShopId)
                .filter(id -> id != null).collect(Collectors.toSet());
        Map<Long, Shop> shopMap = shopIds.isEmpty() ? java.util.Collections.emptyMap()
                : shopMapper.selectBatchIds(shopIds).stream()
                .collect(Collectors.toMap(Shop::getId, s -> s, (a, b) -> a));

        for (ChatSession s : sessions) {
            boolean asCustomer = currentUserId.equals(s.getUserId());
            Long peerId = asCustomer ? s.getMerchantId() : s.getUserId();
            SysUser peer = userMap.get(peerId);
            if (peer != null) {
                s.setPeerName(peer.getNickname());
                s.setPeerAvatar(peer.getAvatar());
            }
            Shop shop = shopMap.get(s.getShopId());
            if (shop != null) {
                s.setShopName(shop.getName());
            }
            s.setUnread(asCustomer ? s.getUserUnread() : s.getMerchantUnread());
        }
    }
}
