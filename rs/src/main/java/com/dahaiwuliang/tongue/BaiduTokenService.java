package com.dahaiwuliang.tongue;

import com.alibaba.fastjson2.JSONObject;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 维护百度千帆 access_token:首次调用时获取,过期前自动刷新,线程安全。
 *
 * <p>官方接口:
 * {@code POST https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id={AK}&client_secret={SK}}
 * 返回 {@code access_token} 与 {@code expires_in}(单位秒,通常 30 天)。
 */
@Service
public class BaiduTokenService {

    private static final Logger log = LoggerFactory.getLogger(BaiduTokenService.class);
    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    /** 提前 1 天刷新,避免边界失效。 */
    private static final long REFRESH_AHEAD_MS = 24L * 60 * 60 * 1000;

    private final BaiduQianfanProperties properties;
    private final AtomicReference<CachedToken> cache = new AtomicReference<>();
    private final Object refreshLock = new Object();

    @Autowired
    public BaiduTokenService(BaiduQianfanProperties properties) {
        this.properties = properties;
    }

    /** 获取有效 access_token,必要时自动刷新。 */
    public String getAccessToken() {
        if (!properties.isConfigured()) {
            throw new IllegalStateException(
                    "百度千帆 AK/SK 未配置,请设置环境变量 BAIDU_AK / BAIDU_SK");
        }
        CachedToken current = cache.get();
        long now = System.currentTimeMillis();
        if (current != null && current.expireAt - REFRESH_AHEAD_MS > now) {
            return current.token;
        }
        synchronized (refreshLock) {
            current = cache.get();
            now = System.currentTimeMillis();
            if (current != null && current.expireAt - REFRESH_AHEAD_MS > now) {
                return current.token;
            }
            CachedToken fresh = fetchToken();
            cache.set(fresh);
            return fresh.token;
        }
    }

    private CachedToken fetchToken() {
        RequestConfig cfg = RequestConfig.custom()
                .setConnectTimeout(properties.getConnectTimeoutMs())
                .setSocketTimeout(properties.getReadTimeoutMs())
                .build();
        try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(cfg).build()) {
            URI uri = new URIBuilder(TOKEN_URL)
                    .addParameter("grant_type", "client_credentials")
                    .addParameter("client_id", properties.getApiKey())
                    .addParameter("client_secret", properties.getSecretKey())
                    .build();
            HttpPost post = new HttpPost(uri);
            try (CloseableHttpResponse resp = client.execute(post)) {
                int status = resp.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                if (status != 200) {
                    // 不输出 body 明文中的敏感信息,仅记录状态与错误描述
                    throw new IllegalStateException("获取百度 access_token 失败,HTTP " + status);
                }
                JSONObject json = JSONObject.parseObject(body);
                String token = json.getString("access_token");
                Long expiresIn = json.getLong("expires_in"); // 秒
                if (token == null || token.isEmpty() || expiresIn == null) {
                    String err = json.getString("error_description");
                    throw new IllegalStateException("获取百度 access_token 响应异常: "
                            + (err == null ? "无 access_token 字段" : err));
                }
                long expireAt = System.currentTimeMillis() + expiresIn * 1000L;
                log.info("百度千帆 access_token 刷新成功,有效期 {} 秒", expiresIn);
                return new CachedToken(token, expireAt);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("获取百度 access_token 异常: " + e.getMessage(), e);
        }
    }

    private static final class CachedToken {
        final String token;
        final long expireAt;
        CachedToken(String token, long expireAt) {
            this.token = token;
            this.expireAt = expireAt;
        }
    }
}
