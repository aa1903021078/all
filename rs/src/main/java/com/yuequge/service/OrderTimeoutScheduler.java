package com.yuequge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuequge.entity.OrderInfo;
import com.yuequge.mapper.OrderInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 订单超时关闭任务：每 30 秒扫描一次未支付且创建时间超过 N 分钟的订单，置为已关闭 ('2')。
 *
 * <p>这是对 RocketMQ 延迟消息的兜底：无 broker 时也能保证订单最终一致。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final OrderInfoMapper orderMapper;

    @Value("${payment.order-timeout-minutes:15}")
    private int timeoutMinutes;

    @Scheduled(fixedDelay = 30_000L, initialDelay = 30_000L)
    public void closeExpiredOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(timeoutMinutes);
        long pending = orderMapper.selectCount(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getStatus, "0")
                .lt(OrderInfo::getCreateTime, deadline));
        if (pending <= 0) return;
        int n = orderMapper.update(null, new LambdaUpdateWrapper<OrderInfo>()
                .eq(OrderInfo::getStatus, "0")
                .lt(OrderInfo::getCreateTime, deadline)
                .set(OrderInfo::getStatus, "2"));
        if (n > 0) log.info("[order-timeout] closed {} expired orders (> {} min)", n, timeoutMinutes);
    }
}
