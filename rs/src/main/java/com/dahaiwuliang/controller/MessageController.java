package com.dahaiwuliang.controller;

import com.dahaiwuliang.common.R;
import com.dahaiwuliang.common.annotation.RequireLogin;
import com.dahaiwuliang.service.MessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 我的消息接口(收到的赞 / 评论回复)
 */
@RestController
@RequestMapping("/messages")
@RequireLogin
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public R<Map<String, Object>> myMessages() {
        return R.ok(messageService.myMessages());
    }
}
