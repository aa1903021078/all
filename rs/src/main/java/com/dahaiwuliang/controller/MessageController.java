package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dahaiwuliang.dto.Result;
import com.dahaiwuliang.entity.Message;
import com.dahaiwuliang.entity.SystemNotification;
import com.dahaiwuliang.mapper.SystemNotificationMapper;
import com.dahaiwuliang.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private SystemNotificationMapper notificationMapper;

    @GetMapping("/message/list")
    public Result<?> list(HttpServletRequest request, @RequestParam(required = false) Long contactId) {
        Long userId = (Long) request.getAttribute("userId");
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (contactId != null) {
            wrapper.and(w -> w
                    .and(w1 -> w1.eq(Message::getSenderId, userId).eq(Message::getReceiverId, contactId))
                    .or(w2 -> w2.eq(Message::getSenderId, contactId).eq(Message::getReceiverId, userId)));
        } else {
            wrapper.and(w -> w.eq(Message::getSenderId, userId).or().eq(Message::getReceiverId, userId));
        }
        wrapper.orderByAsc(Message::getCreateTime);
        List<Message> messages = messageService.list(wrapper);
        return Result.success(messages);
    }

    @PostMapping("/message/send")
    public Result<?> send(HttpServletRequest request, @RequestBody Message message) {
        Long userId = (Long) request.getAttribute("userId");
        message.setSenderId(userId);
        message.setIsRead(0);
        message.setType(0);
        messageService.save(message);
        return Result.success(message);
    }

    @PutMapping("/message/read/{id}")
    public Result<?> markRead(HttpServletRequest request, @PathVariable Long id) {
        Message message = messageService.getById(id);
        if (message != null) {
            message.setIsRead(1);
            messageService.updateById(message);
        }
        return Result.success();
    }

    @GetMapping("/message/unread-count")
    public Result<?> unreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        long count = messageService.count(new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverId, userId).eq(Message::getIsRead, 0));
        return Result.success(count);
    }

    @GetMapping("/notification/list")
    public Result<?> notifications(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<SystemNotification> list = notificationMapper.selectList(
                new LambdaQueryWrapper<SystemNotification>()
                        .eq(SystemNotification::getUserId, userId)
                        .orderByDesc(SystemNotification::getCreateTime));
        return Result.success(list);
    }
}
