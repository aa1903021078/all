package com.yuequge.controller;

import com.yuequge.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付宝沙箱异步通知占位。完整签名校验 + 订单状态变更留到第 2 期实现。
 */
@Slf4j
@RestController
@RequestMapping("/api/payments/alipay")
public class AlipayNotifyController {

    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        log.info("[alipay notify] params={}", request.getParameterMap().keySet());
        // 第 2 期：验签 -> 查订单 -> 更新状态 -> 通知消息队列
        return "success";
    }

    /** 第 2 期前端发起支付的占位接口，返回待扩展提示。 */
    @PostMapping("/pay")
    public Result<String> pay() {
        return Result.ok("alipay-sandbox-not-enabled-yet", "支付宝沙箱完整集成将在第 2 期提供");
    }
}
