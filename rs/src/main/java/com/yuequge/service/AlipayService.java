package com.yuequge.service;

import com.yuequge.config.AlipayProperties;
import com.yuequge.util.AlipaySignUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 支付宝沙箱 PC 扫码支付：构造 alipay.trade.page.pay 请求表单。
 *
 * <p>不依赖 Alipay SDK。默认沙箱禁用时返回 mock URL，不阻塞联调。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayService {

    private final AlipayProperties props;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 构造自动提交的 HTML 表单（前端可直接写入新窗口）。
     */
    public String buildPagePayForm(String outTradeNo, BigDecimal amount, String subject) {
        if (!props.isEnabled() || isBlank(props.getAppId()) || isBlank(props.getPrivateKey())) {
            // 沙箱未启用：返回一个简单的 mock 页面，走前端模拟支付按钮
            return "<html><body><h3>支付宝沙箱未启用 (mock)</h3>"
                    + "<p>订单号: " + escape(outTradeNo) + " 金额: ￥" + amount + "</p>"
                    + "<p>在生产环境请配置 payment.alipay.* 参数。</p></body></html>";
        }

        Map<String, String> biz = new LinkedHashMap<>();
        biz.put("out_trade_no", outTradeNo);
        biz.put("product_code", "FAST_INSTANT_TRADE_PAY");
        biz.put("total_amount", amount.toPlainString());
        biz.put("subject", subject);

        Map<String, String> params = new TreeMap<>();
        params.put("app_id", props.getAppId());
        params.put("method", "alipay.trade.page.pay");
        params.put("format", "JSON");
        params.put("charset", props.getCharset());
        params.put("sign_type", props.getSignType());
        params.put("timestamp", LocalDateTime.now().format(FMT));
        params.put("version", "1.0");
        params.put("notify_url", props.getNotifyUrl());
        params.put("return_url", props.getReturnUrl());
        params.put("biz_content", toJson(biz));

        String signContent = AlipaySignUtil.buildSignContent(params);
        String sign = AlipaySignUtil.sign(signContent, props.getPrivateKey());
        params.put("sign", sign);

        // 构造自动提交表单
        StringBuilder form = new StringBuilder();
        form.append("<html><head><meta charset='UTF-8'></head><body>")
                .append("<form id='alipaySubmit' name='alipaySubmit' action='")
                .append(escape(props.getGatewayUrl()))
                .append("?charset=").append(props.getCharset())
                .append("' method='POST'>");
        for (Map.Entry<String, String> e : params.entrySet()) {
            form.append("<input type='hidden' name='").append(escape(e.getKey()))
                    .append("' value=\"").append(escape(e.getValue())).append("\"/>");
        }
        form.append("<input type='submit' value='立即支付' style='display:none'/>")
                .append("</form><script>document.forms['alipaySubmit'].submit();</script>")
                .append("</body></html>");
        return form.toString();
    }

    /** 验签支付宝异步通知。*/
    public boolean verifyNotify(Map<String, String> params) {
        if (isBlank(props.getAlipayPublicKey())) {
            log.warn("alipay public key not configured, skip verify");
            return true; // 兜底放过，便于联调
        }
        String sign = params.get("sign");
        if (sign == null) return false;
        String content = AlipaySignUtil.buildSignContent(params);
        return AlipaySignUtil.verify(content, sign, props.getAlipayPublicKey());
    }

    /** biz_content 用极简 JSON 序列化，字段固定、无嵌套。 */
    private static String toJson(Map<String, String> biz) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> e : biz.entrySet()) {
            if (!first) sb.append(',');
            sb.append('"').append(e.getKey()).append('"').append(':')
                    .append('"').append(jsonEscape(e.getValue())).append('"');
            first = false;
        }
        return sb.append('}').toString();
    }

    private static String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
