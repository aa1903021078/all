package com.dahaiwuliang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dahaiwuliang.entity.Message;
import com.dahaiwuliang.mapper.MessageMapper;
import com.dahaiwuliang.service.MessageService;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
}
