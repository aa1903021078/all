package com.dahaiwuliang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dahaiwuliang.entity.ExchangeOrder;
import com.dahaiwuliang.mapper.ExchangeOrderMapper;
import com.dahaiwuliang.service.ExchangeOrderService;
import org.springframework.stereotype.Service;

@Service
public class ExchangeOrderServiceImpl extends ServiceImpl<ExchangeOrderMapper, ExchangeOrder> implements ExchangeOrderService {
}
