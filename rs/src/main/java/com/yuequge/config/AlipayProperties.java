package com.yuequge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝沙箱配置。
 */
@Data
@Configuration
@ConfigurationProperties("payment.alipay")
public class AlipayProperties {
    private boolean enabled = true;
    private boolean sandbox = true;
    private String gatewayUrl;
    private String appId;
    private String privateKey;
    private String alipayPublicKey;
    private String notifyUrl;
    private String returnUrl;
    private String signType = "RSA2";
    private String charset = "UTF-8";
}
