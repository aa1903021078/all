package com.dahaiwuliang.service;

import com.dahaiwuliang.entity.Notification;
import com.dahaiwuliang.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper mapper;

    public void push(Long userId, String title, String content, String type) {
        if (userId == null) return;
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type);
        n.setReadFlag(0);
        n.setCreatedAt(LocalDateTime.now());
        mapper.insert(n);
    }
}
