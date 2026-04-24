package com.dahaiwuliang.tongue;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 百度千帆(ModelBuilder / 文心一言)多模态大模型接入配置。
 *
 * <p>支持两种调用路径,优先级:<br>
 * 1) {@code bearerToken} 非空 → 走千帆 <b>v2 OpenAI 兼容接口</b>
 *    {@code https://qianfan.baidubce.com/v2/chat/completions},使用 Bearer Token。
 *    多模态模型(ernie-4.5-turbo-vl-32k、ernie-4.5-vl 等)建议走此路径。<br>
 * 2) 仅配置 {@code apiKey} + {@code secretKey} → 走老的 <b>v1 OAuth 接口</b>
 *    {@code https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/{modelEndpoint}?access_token=...},
 *    此时 {@code modelEndpoint} 必须是你在千帆控制台「在线服务」页面为该模型发布的短名。
 */
@Component
@ConfigurationProperties(prefix = "baidu.qianfan")
public class BaiduQianfanProperties {

    /** 千帆应用 API Key(AK)。用于 v1 OAuth 换 access_token。 */
    private String apiKey;

    /** 千帆应用 Secret Key(SK)。用于 v1 OAuth 换 access_token。 */
    private String secretKey;

    /**
     * 千帆 v2 Bearer Token(控制台「安全认证 → API Key」生成,通常形如
     * {@code bce-v3/ALTAK-xxx/xxx})。若配置了本字段,将优先走 v2 OpenAI 兼容接口。
     */
    private String bearerToken;

    /**
     * 模型名 / 服务 endpoint 短名。<br>
     * - v2 路径:直接填模型名,如 {@code ernie-4.5-turbo-vl-32k}、{@code ernie-4.5-vl}。<br>
     * - v1 路径:必须填你在控制台发布的服务短名(而不是模型展示名)。
     */
    private String modelEndpoint = "ernie-4.5-turbo-vl-32k";

    /** 单张图片原始字节上限,超过则拒绝处理。 */
    private int maxImageBytes = 4 * 1024 * 1024;

    private int connectTimeoutMs = 10_000;
    private int readTimeoutMs = 60_000;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public String getBearerToken() { return bearerToken; }
    public void setBearerToken(String bearerToken) { this.bearerToken = bearerToken; }

    public String getModelEndpoint() { return modelEndpoint; }
    public void setModelEndpoint(String modelEndpoint) { this.modelEndpoint = modelEndpoint; }

    public int getMaxImageBytes() { return maxImageBytes; }
    public void setMaxImageBytes(int maxImageBytes) { this.maxImageBytes = maxImageBytes; }

    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }

    public boolean hasBearer() {
        return bearerToken != null && !bearerToken.isEmpty();
    }

    public boolean hasAkSk() {
        return apiKey != null && !apiKey.isEmpty()
                && secretKey != null && !secretKey.isEmpty();
    }

    public boolean isConfigured() {
        return hasBearer() || hasAkSk();
    }
}
