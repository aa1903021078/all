package com.yuequge.util;

import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

/**
 * 支付宝 RSA2 签名 / 验签工具（基于 JDK 原生能力，无需 Alipay SDK）。
 */
@Slf4j
public final class AlipaySignUtil {

    private AlipaySignUtil() {}

    private static final String SIGN_ALGO = "SHA256withRSA";

    /** 构造待签名串：key=value& 按 key 升序拼接（跳过空值、sign、sign_type）。 */
    public static String buildSignContent(Map<String, String> params) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();
            if (v == null || v.isEmpty()) continue;
            if ("sign".equals(k) || "sign_type".equals(k)) continue;
            if (sb.length() > 0) sb.append('&');
            sb.append(k).append('=').append(v);
        }
        return sb.toString();
    }

    /** 使用 PKCS#8 私钥对字符串进行 RSA2 签名，返回 Base64。 */
    public static String sign(String content, String privateKeyPkcs8) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPkcs8);
            PrivateKey pk = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
            Signature sig = Signature.getInstance(SIGN_ALGO);
            sig.initSign(pk);
            sig.update(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(sig.sign());
        } catch (Exception e) {
            throw new RuntimeException("Alipay sign failed: " + e.getMessage(), e);
        }
    }

    /** 用支付宝公钥验签。 */
    public static boolean verify(String content, String signBase64, String alipayPublicKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(alipayPublicKey);
            PublicKey pk = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(keyBytes));
            Signature sig = Signature.getInstance(SIGN_ALGO);
            sig.initVerify(pk);
            sig.update(content.getBytes(StandardCharsets.UTF_8));
            return sig.verify(Base64.getDecoder().decode(signBase64));
        } catch (Exception e) {
            log.warn("alipay verify failed: {}", e.getMessage());
            return false;
        }
    }

    /** URL 编码工具。 */
    public static String urlEncode(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }
}
