package com.dahaiwuliang.tongue;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 舌诊分析核心服务:校验图片 → 调用百度千帆多模态模型 → 解析结构化结果。
 */
@Service
public class TongueAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(TongueAnalysisService.class);

    private static final String CHAT_BASE_URL =
            "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/";

    private static final String DEFAULT_DISCLAIMER =
            "本结果由 AI 根据图像生成,仅供健康参考,不构成医疗诊断,如有不适请及时就医。";

    /** 提示词:要求模型以固定 JSON 输出,方便后端解析。 */
    private static final String PROMPT_TEMPLATE = ""
            + "你是一名资深的中医舌诊专家。请严格根据用户上传的这张舌头图片进行舌诊分析,"
            + "从以下三个维度给出判断,并结合常见中医证型给出生活调养建议:\n"
            + "1) 舌质:颜色(淡红/淡白/红/绛/青紫等)、胖瘦、齿痕、裂纹、点刺等;\n"
            + "2) 舌苔:颜色(白/黄/灰/黑)、厚薄、润燥、腐腻、是否剥落;\n"
            + "3) 舌型:大小、老嫩、柔软度。\n"
            + "请只返回 JSON,不要包含任何 Markdown 代码块标记或多余说明。字段固定如下:\n"
            + "{\n"
            + "  \"tongueBody\": \"对舌质的描述\",\n"
            + "  \"tongueCoating\": \"对舌苔的描述\",\n"
            + "  \"tongueShape\": \"对舌型的描述\",\n"
            + "  \"syndrome\": \"可能的中医证型提示(如气虚、湿热、阴虚等,并简要说明)\",\n"
            + "  \"suggestion\": \"饮食起居与调养建议\",\n"
            + "  \"disclaimer\": \"免责声明:仅供参考,不替代医生诊断\"\n"
            + "}\n"
            + "若图片不是人类舌头,或画面模糊、光线不足导致无法判断,请改为返回:\n"
            + "{\"error\": \"无法识别舌象的具体原因\"}";

    private final BaiduQianfanProperties properties;
    private final BaiduTokenService tokenService;

    @Autowired
    public TongueAnalysisService(BaiduQianfanProperties properties, BaiduTokenService tokenService) {
        this.properties = properties;
        this.tokenService = tokenService;
    }

    public TongueReport analyze(MultipartFile file) {
        byte[] bytes = validateAndRead(file);
        String mime = detectMime(bytes);
        String base64 = Base64.getEncoder().encodeToString(bytes);
        String modelRaw = callQianfan(base64, mime);
        return parseReport(modelRaw);
    }

    /* ---------------- 图片校验 ---------------- */

    private byte[] validateAndRead(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new TongueAnalysisException(400, "未接收到图片文件");
        }
        long size = file.getSize();
        if (size > properties.getMaxImageBytes()) {
            throw new TongueAnalysisException(400,
                    "图片过大,请上传不超过 " + (properties.getMaxImageBytes() / 1024 / 1024) + "MB 的图片");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new TongueAnalysisException(400, "读取图片失败", e);
        }
        if (!isSupportedImage(bytes)) {
            throw new TongueAnalysisException(400, "仅支持 JPEG / PNG / WEBP 格式的图片");
        }
        return bytes;
    }

    /** 通过文件头 magic number 判断图片类型,避免只信任后缀。 */
    private static boolean isSupportedImage(byte[] bytes) {
        return detectMimeOrNull(bytes) != null;
    }

    private static String detectMime(byte[] bytes) {
        String mime = detectMimeOrNull(bytes);
        return mime == null ? "image/jpeg" : mime;
    }

    private static String detectMimeOrNull(byte[] b) {
        if (b == null || b.length < 12) return null;
        // JPEG: FF D8 FF
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && (b[4] & 0xFF) == 0x0D && (b[5] & 0xFF) == 0x0A
                && (b[6] & 0xFF) == 0x1A && (b[7] & 0xFF) == 0x0A) {
            return "image/png";
        }
        // WEBP: "RIFF"...."WEBP"
        if (b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    /* ---------------- 调用千帆 ---------------- */

    private String callQianfan(String base64Image, String mime) {
        String token = tokenService.getAccessToken();
        String url = CHAT_BASE_URL + properties.getModelEndpoint() + "?access_token=" + token;

        // content 数组:一段文字 + 一张图片(base64)。
        // 千帆多模态 content 的 image_url 支持 "data:<mime>;base64,<data>" 或 http(s) URL。
        JSONArray content = new JSONArray();
        JSONObject textPart = new JSONObject();
        textPart.put("type", "text");
        textPart.put("text", PROMPT_TEMPLATE);
        content.add(textPart);

        JSONObject imagePart = new JSONObject();
        imagePart.put("type", "image_url");
        JSONObject imageUrl = new JSONObject();
        imageUrl.put("url", "data:" + mime + ";base64," + base64Image);
        imagePart.put("image_url", imageUrl);
        content.add(imagePart);

        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", content);

        JSONArray messages = new JSONArray();
        messages.add(message);

        JSONObject payload = new JSONObject();
        payload.put("messages", messages);
        payload.put("temperature", 0.2);

        RequestConfig cfg = RequestConfig.custom()
                .setConnectTimeout(properties.getConnectTimeoutMs())
                .setSocketTimeout(properties.getReadTimeoutMs())
                .build();

        try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(cfg).build()) {
            HttpPost post = new HttpPost(url);
            post.setEntity(new StringEntity(payload.toJSONString(),
                    ContentType.create("application/json", StandardCharsets.UTF_8)));
            try (CloseableHttpResponse resp = client.execute(post)) {
                int status = resp.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                if (status != 200) {
                    log.warn("调用千帆失败 status={} bodyLen={}", status, body == null ? 0 : body.length());
                    throw new TongueAnalysisException(502, "调用大模型失败,HTTP " + status);
                }
                JSONObject json = JSONObject.parseObject(body);
                if (json.containsKey("error_code")) {
                    Object err = json.get("error_msg");
                    throw new TongueAnalysisException(502,
                            "大模型返回错误: " + (err == null ? json.get("error_code") : err));
                }
                String result = json.getString("result");
                if (result == null || result.isEmpty()) {
                    throw new TongueAnalysisException(502, "大模型返回为空");
                }
                return result;
            }
        } catch (TongueAnalysisException e) {
            throw e;
        } catch (Exception e) {
            // 注意:异常信息不要带敏感信息(token/base64),只给出通用描述。
            throw new TongueAnalysisException(502, "调用大模型异常: " + e.getMessage(), e);
        }
    }

    /* ---------------- 解析模型输出 ---------------- */

    private TongueReport parseReport(String modelText) {
        String json = extractJson(modelText);
        TongueReport report = new TongueReport();
        report.setRawText(modelText);
        if (json == null) {
            report.setSuggestion(modelText);
            report.setDisclaimer(DEFAULT_DISCLAIMER);
            return report;
        }
        JSONObject obj;
        try {
            obj = JSONObject.parseObject(json);
        } catch (Exception e) {
            report.setSuggestion(modelText);
            report.setDisclaimer(DEFAULT_DISCLAIMER);
            return report;
        }
        if (obj.containsKey("error")) {
            throw new TongueAnalysisException(422,
                    "无法识别舌象:" + obj.getString("error") + "。请在自然光下张嘴伸舌,保持对焦清晰后重试。");
        }
        report.setTongueBody(obj.getString("tongueBody"));
        report.setTongueCoating(obj.getString("tongueCoating"));
        report.setTongueShape(obj.getString("tongueShape"));
        report.setSyndrome(obj.getString("syndrome"));
        report.setSuggestion(obj.getString("suggestion"));
        String disclaimer = obj.getString("disclaimer");
        report.setDisclaimer(disclaimer == null || disclaimer.isEmpty() ? DEFAULT_DISCLAIMER : disclaimer);
        return report;
    }

    /** 从模型回复里抓取第一段 {...} JSON,兼容 ```json ... ``` 代码块包裹的情况。 */
    private static String extractJson(String text) {
        if (text == null) return null;
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }
}
