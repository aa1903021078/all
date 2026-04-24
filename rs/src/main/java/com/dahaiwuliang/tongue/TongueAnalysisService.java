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
 *
 * <p>调用路径由 {@link BaiduQianfanProperties} 自动选择:<br>
 * - 配置 {@code bearer-token} → 走千帆 v2 OpenAI 兼容接口(推荐,支持 ernie-4.5-vl 系多模态);<br>
 * - 只有 AK/SK → 走 v1 {@code wenxinworkshop/chat/{endpoint}}(需在控制台发布服务)。
 */
@Service
public class TongueAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(TongueAnalysisService.class);

    /** v1 老接口 base,拼接 endpoint 短名后使用。 */
    private static final String V1_CHAT_BASE_URL =
            "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/";

    /** v2 OpenAI 兼容接口(Bearer Token 鉴权)。 */
    private static final String V2_CHAT_URL =
            "https://qianfan.baidubce.com/v2/chat/completions";

    private static final String DEFAULT_DISCLAIMER =
            "本结果由 AI 根据图像生成,仅供健康参考,不构成医疗诊断,如有不适请及时就医。";

    /** 提示词:要求模型以固定 JSON 输出,方便后端解析。 */
    private static final String PROMPT_TEMPLATE = ""
            + "你是一名资深的中医舌诊与体质辨识专家。请严格根据用户上传的这张舌头图片进行细致的望舌分析,"
            + "并结合中医九种体质理论给出体质辨识与调养建议。请从以下维度分别评估:\n"
            + "1) 舌质:颜色(淡红/淡白/红/绛/青紫等)、荣枯、有无光泽;\n"
            + "2) 舌苔:颜色(白/黄/灰/黑)、厚薄、润燥、腐腻、是否剥落;\n"
            + "3) 舌形:胖瘦、大小、老嫩;\n"
            + "4) 舌态:强硬、痿软、歪斜、颤动、吐弄、短缩等动态特征(无则写\"自然伸展、活动自如\");\n"
            + "5) 舌下脉络:舌下静脉的颜色、粗细、迂曲程度,反映气血瘀滞情况(若图中不可见请说明\"未见舌下脉络图像\");\n"
            + "6) 齿痕:舌边是否有齿痕及其明显程度(如无则写\"未见明显齿痕\");\n"
            + "7) 裂纹:舌面裂纹的位置、形态、深浅(如无则写\"未见明显裂纹\");\n"
            + "8) 点刺:红点或芒刺的分布与色泽(如无则写\"未见明显点刺\");\n"
            + "9) 津液:润、燥、滑、少津等状况;\n"
            + "10) 体质辨识:从「平和质/气虚质/阳虚质/阴虚质/痰湿质/湿热质/血瘀质/气郁质/特禀质」中选出最可能的 1–2 种并简述依据;\n"
            + "11) 证型提示:可能的中医证型(如气虚、湿热、阴虚、血瘀等),并简要说明;\n"
            + "12) 图像质量:简评光线、对焦与舌体完整度。\n"
            + "请只返回 JSON,不要包含任何 Markdown 代码块标记或多余说明。字段固定如下(所有字段均必须存在,未观察到的请用\"未见明显异常\"或相应说明填充):\n"
            + "{\n"
            + "  \"tongueBody\": \"对舌质的描述\",\n"
            + "  \"tongueCoating\": \"对舌苔的描述\",\n"
            + "  \"tongueShape\": \"对舌形的描述\",\n"
            + "  \"tongueState\": \"对舌态的描述\",\n"
            + "  \"sublingualVein\": \"对舌下脉络的描述\",\n"
            + "  \"toothMarks\": \"对齿痕的描述\",\n"
            + "  \"cracks\": \"对裂纹的描述\",\n"
            + "  \"spots\": \"对点刺/红点的描述\",\n"
            + "  \"moisture\": \"对津液润燥的描述\",\n"
            + "  \"constitution\": \"中医体质辨识结果及依据\",\n"
            + "  \"syndrome\": \"可能的中医证型提示及简要说明\",\n"
            + "  \"dietAdvice\": \"针对性的饮食建议(宜食/忌食举例)\",\n"
            + "  \"lifestyleAdvice\": \"起居作息与运动建议\",\n"
            + "  \"suggestion\": \"综合调养建议摘要\",\n"
            + "  \"imageQuality\": \"图像质量简评\",\n"
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
        String modelRaw = properties.hasBearer()
                ? callQianfanV2(base64, mime)
                : callQianfanV1(base64, mime);
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

    /* ---------------- 构造 messages(两版共用) ---------------- */

    private JSONArray buildMessages(String base64Image, String mime) {
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
        return messages;
    }

    /* ---------------- 调用千帆 v2(Bearer Token,OpenAI 兼容) ---------------- */

    private String callQianfanV2(String base64Image, String mime) {
        JSONObject payload = new JSONObject();
        payload.put("model", properties.getModelEndpoint());
        payload.put("messages", buildMessages(base64Image, mime));
        payload.put("temperature", 0.2);

        RequestConfig cfg = RequestConfig.custom()
                .setConnectTimeout(properties.getConnectTimeoutMs())
                .setSocketTimeout(properties.getReadTimeoutMs())
                .build();

        try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(cfg).build()) {
            HttpPost post = new HttpPost(V2_CHAT_URL);
            post.setHeader("Authorization", "Bearer " + properties.getBearerToken());
            post.setEntity(new StringEntity(payload.toJSONString(),
                    ContentType.create("application/json", StandardCharsets.UTF_8)));
            try (CloseableHttpResponse resp = client.execute(post)) {
                int status = resp.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                if (status != 200) {
                    log.warn("调用千帆 v2 失败 status={} body={}", status, truncate(body));
                    throw new TongueAnalysisException(502,
                            "调用大模型失败,HTTP " + status + ":" + truncate(body));
                }
                JSONObject json = JSONObject.parseObject(body);
                if (json.containsKey("error")) {
                    JSONObject err = json.getJSONObject("error");
                    String msg = err == null ? json.getString("error") : err.getString("message");
                    throw new TongueAnalysisException(502, "大模型返回错误: " + msg);
                }
                JSONArray choices = json.getJSONArray("choices");
                if (choices == null || choices.isEmpty()) {
                    throw new TongueAnalysisException(502, "大模型返回为空: " + truncate(body));
                }
                JSONObject msg = choices.getJSONObject(0).getJSONObject("message");
                String result = msg == null ? null : msg.getString("content");
                if (result == null || result.isEmpty()) {
                    throw new TongueAnalysisException(502, "大模型返回为空");
                }
                return result;
            }
        } catch (TongueAnalysisException e) {
            throw e;
        } catch (Exception e) {
            throw new TongueAnalysisException(502, "调用大模型异常: " + e.getMessage(), e);
        }
    }

    /* ---------------- 调用千帆 v1(OAuth access_token) ---------------- */

    private String callQianfanV1(String base64Image, String mime) {
        String token = tokenService.getAccessToken();
        String url = V1_CHAT_BASE_URL + properties.getModelEndpoint() + "?access_token=" + token;

        JSONObject payload = new JSONObject();
        payload.put("messages", buildMessages(base64Image, mime));
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
                    log.warn("调用千帆 v1 失败 status={} body={}", status, truncate(body));
                    throw new TongueAnalysisException(502,
                            "调用大模型失败,HTTP " + status + ":" + truncate(body));
                }
                JSONObject json = JSONObject.parseObject(body);
                if (json.containsKey("error_code")) {
                    Object code = json.get("error_code");
                    Object err = json.get("error_msg");
                    throw new TongueAnalysisException(502,
                            "大模型返回错误[" + code + "]: " + (err == null ? "unknown" : err)
                                    + "。请确认 model-endpoint=\"" + properties.getModelEndpoint()
                                    + "\" 已在千帆控制台「在线服务」中发布,或改用 v2 Bearer Token 路径。");
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
            throw new TongueAnalysisException(502, "调用大模型异常: " + e.getMessage(), e);
        }
    }

    private static String truncate(String s) {
        if (s == null) return "";
        return s.length() > 500 ? s.substring(0, 500) + "..." : s;
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
        report.setTongueState(obj.getString("tongueState"));
        report.setSublingualVein(obj.getString("sublingualVein"));
        report.setToothMarks(obj.getString("toothMarks"));
        report.setCracks(obj.getString("cracks"));
        report.setSpots(obj.getString("spots"));
        report.setMoisture(obj.getString("moisture"));
        report.setConstitution(obj.getString("constitution"));
        report.setSyndrome(obj.getString("syndrome"));
        report.setDietAdvice(obj.getString("dietAdvice"));
        report.setLifestyleAdvice(obj.getString("lifestyleAdvice"));
        report.setSuggestion(obj.getString("suggestion"));
        report.setImageQuality(obj.getString("imageQuality"));
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
