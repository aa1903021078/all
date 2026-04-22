package com.yuequge.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 消息发送的轻量包装：broker 不可用 / rocketmq starter 未引入时自动降级为 no-op。
 *
 * <p>使用反射加载 RocketMQTemplate，避免在无依赖环境下的 NoClassDefFoundError。
 */
@Slf4j
@Component
public class MessageQueueService {

    @Autowired(required = false)
    private org.springframework.context.ApplicationContext ctx;

    /** 发送一条订单超时消息（RocketMQ 延迟级别 16 = 30min；用于 > 15min 关单场景）。 */
    public void sendOrderTimeout(String orderId, int delayMinutes) {
        if (ctx == null) return;
        try {
            Class<?> tmplClass = Class.forName("org.apache.rocketmq.spring.core.RocketMQTemplate");
            Object tmpl = ctx.getBeansOfType(tmplClass).values().stream().findFirst().orElse(null);
            if (tmpl == null) return;
            Class<?> msgBuilder = Class.forName("org.springframework.messaging.support.MessageBuilder");
            Object payload = msgBuilder.getMethod("withPayload", Object.class).invoke(null, orderId);
            Object msg = payload.getClass().getMethod("build").invoke(payload);
            // RocketMQTemplate#syncSendDelayTimer(topic, message, delayLevel) — 兼容 2.3.0 签名
            // 2.3.0 提供 syncSend(topic, msg, timeout, delayLevel)
            var m = tmpl.getClass().getMethod("syncSend",
                    String.class,
                    Class.forName("org.springframework.messaging.Message"),
                    long.class, int.class);
            int delayLevel = mapDelayLevel(delayMinutes);
            m.invoke(tmpl, "order-timeout", msg, 3000L, delayLevel);
            log.info("[mq] sent order-timeout orderId={} delay={}min", orderId, delayMinutes);
        } catch (ClassNotFoundException e) {
            // rocketmq 未引入，降级
        } catch (Exception e) {
            log.warn("[mq] send failed: {}", e.getMessage());
        }
    }

    /** 映射延迟分钟到 RocketMQ 延迟级别。RocketMQ 默认级别：1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h。 */
    private static int mapDelayLevel(int minutes) {
        if (minutes <= 1) return 5;
        if (minutes <= 2) return 6;
        if (minutes <= 5) return 9;
        if (minutes <= 10) return 14;
        if (minutes <= 15) return 15; // 20m（>=15）
        if (minutes <= 20) return 15;
        return 16; // 30m
    }
}
