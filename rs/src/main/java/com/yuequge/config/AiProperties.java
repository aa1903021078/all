package com.yuequge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI（DeepSeek 等）配置。
 */
@Data
@Configuration
@ConfigurationProperties("ai")
public class AiProperties {
    private boolean enabled = true;
    private String apiKey;
    private String apiUrl;
    private String model = "deepseek-chat";
    private int timeout = 30;
    private String systemPrompt = "你是阅趣阁的阅读助手。";
}
