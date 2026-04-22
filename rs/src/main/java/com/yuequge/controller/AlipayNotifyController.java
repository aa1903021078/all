package com.yuequge.controller;

import com.yuequge.common.Result;
import com.yuequge.entity.OrderInfo;
import com.yuequge.mapper.OrderInfoMapper;
import com.yuequge.service.AlipayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝沙箱异步通知 + 同步回跳处理。
 */
@Slf4j
@RestController
@RequestMapping("/api/payments/alipay")
@RequiredArgsConstructor
public class AlipayNotifyController {

    private final AlipayService alipayService;
    private final OrderInfoMapper orderMapper;
    private final com.yuequge.service.UserActionService userActionService;

    /**
     * 异步通知：验签 -> 查订单 -> 幂等更新状态 -> 返回 success 文本。
     */
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        Map<String, String> params = flatten(request.getParameterMap());
        log.info("[alipay notify] keys={} outTradeNo={}", params.keySet(), params.get("out_trade_no"));

        if (!alipayService.verifyNotify(params)) {
            log.warn("[alipay notify] invalid signature");
            return "failure";
        }

        String tradeStatus = params.get("trade_status");
        String outTradeNo = params.get("out_trade_no");
        if (outTradeNo == null || outTradeNo.isBlank()) return "failure";

        OrderInfo o = orderMapper.selectById(outTradeNo);
        if (o == null) {
            log.warn("[alipay notify] order not found: {}", outTradeNo);
            return "success"; // 不重试
        }

        // 支付宝的终态：TRADE_SUCCESS / TRADE_FINISHED
        if (("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus))
                && !"1".equals(o.getStatus())) {
            o.setStatus("1");
            orderMapper.updateById(o);
            userActionService.track(o.getUserId(), "ORDER", o.getItemId(), "PAY", "orderId=" + outTradeNo);
            log.info("[alipay notify] order {} paid", outTradeNo);
        } else if ("TRADE_CLOSED".equals(tradeStatus) && "0".equals(o.getStatus())) {
            o.setStatus("2");
            orderMapper.updateById(o);
        }
        return "success";
    }

    /**
     * 调试 / 开发用：前端在无法联通支付宝沙箱时可直接把自己的订单标记为已支付。
     * 需登录，且仅允许操作自己订单；生产可通过配置关闭支付宝 sandbox 并移除此按钮。
     */
    @PostMapping("/mock-pay")
    public Result<Void> mockPay(@RequestParam String orderId) {
        var u = com.yuequge.util.UserContext.get();
        if (u == null) return Result.fail(401, "未登录");
        OrderInfo o = orderMapper.selectById(orderId);
        if (o == null) return Result.fail(404, "订单不存在");
        if (!u.isAdmin() && !o.getUserId().equals(u.userId())) return Result.fail(403, "无权访问");
        if ("1".equals(o.getStatus())) return Result.ok();
        o.setStatus("1");
        orderMapper.updateById(o);
        userActionService.track(o.getUserId(), "ORDER", o.getItemId(), "PAY", "orderId=" + orderId + "&mock=1");
        return Result.ok();
    }

    private static Map<String, String> flatten(Map<String, String[]> src) {
        Map<String, String> m = new HashMap<>(src.size());
        for (Map.Entry<String, String[]> e : src.entrySet()) {
            String[] v = e.getValue();
            if (v != null && v.length > 0) m.put(e.getKey(), v[0]);
        }
        return m;
    }
}
