package com.dahaiwuliang.tongue;

/**
 * 舌诊分析结构化结果。字段与前端展示一一对应。
 */
public class TongueReport {

    /** 舌质(颜色、胖瘦、齿痕、裂纹、点刺等)。 */
    private String tongueBody;

    /** 舌苔(颜色、厚薄、润燥、腐腻、剥落)。 */
    private String tongueCoating;

    /** 舌型(大小、老嫩、柔软度)。 */
    private String tongueShape;

    /** 可能的中医证型提示。 */
    private String syndrome;

    /** 生活调养建议。 */
    private String suggestion;

    /** 免责声明。 */
    private String disclaimer;

    /** 模型返回的原始文本(便于调试与兜底展示)。 */
    private String rawText;

    public String getTongueBody() { return tongueBody; }
    public void setTongueBody(String tongueBody) { this.tongueBody = tongueBody; }

    public String getTongueCoating() { return tongueCoating; }
    public void setTongueCoating(String tongueCoating) { this.tongueCoating = tongueCoating; }

    public String getTongueShape() { return tongueShape; }
    public void setTongueShape(String tongueShape) { this.tongueShape = tongueShape; }

    public String getSyndrome() { return syndrome; }
    public void setSyndrome(String syndrome) { this.syndrome = syndrome; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
}
