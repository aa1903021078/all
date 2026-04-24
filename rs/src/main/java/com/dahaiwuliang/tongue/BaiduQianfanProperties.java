package com.dahaiwuliang.tongue;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 百度千帆(ModelBuilder / 文心一言)多模态大模型接入配置。
 */
@Component
@ConfigurationProperties(prefix = "baidu.qianfan")
public class BaiduQianfanProperties {

    /** 千帆应用 API Key(AK)。通过环境变量 BAIDU_AK 注入,不要写死在代码/配置文件中。 */
    private String apiKey;

    /** 千帆应用 Secret Key(SK)。通过环境变量 BAIDU_SK 注入。 */
    private String secretKey;

    /** 使用的模型 endpoint,例如 ernie-4.5-vl / ernie-4.0-turbo-vl。 */
    private String modelEndpoint = "ernie-4.5-vl";

    /** 单张图片原始字节上限,超过则拒绝处理。 */
    private int maxImageBytes = 4 * 1024 * 1024;

    private int connectTimeoutMs = 10_000;
    private int readTimeoutMs = 60_000;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public String getModelEndpoint() { return modelEndpoint; }
    public void setModelEndpoint(String modelEndpoint) { this.modelEndpoint = modelEndpoint; }

    public int getMaxImageBytes() { return maxImageBytes; }
    public void setMaxImageBytes(int maxImageBytes) { this.maxImageBytes = maxImageBytes; }

    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty()
                && secretKey != null && !secretKey.isEmpty();
    }
}
