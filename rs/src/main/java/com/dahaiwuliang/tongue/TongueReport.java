package com.dahaiwuliang.tongue;

/**
 * 舌诊分析结构化结果。字段与前端展示一一对应。
 * 覆盖望舌的主要维度:舌质、舌苔、舌形、舌态、舌下脉络,以及齿痕、裂纹、点刺、
 * 津液等细节特征,并结合中医体质辨识与证型提示给出调养建议。
 */
public class TongueReport {

    /** 舌质:颜色(淡红/淡白/红/绛/青紫等)、荣枯。 */
    private String tongueBody;

    /** 舌苔:颜色(白/黄/灰/黑)、厚薄、润燥、腐腻、是否剥落。 */
    private String tongueCoating;

    /** 舌形:胖瘦、大小、老嫩。 */
    private String tongueShape;

    /** 舌态:舌体动态,如强硬、痿软、歪斜、颤动、吐弄、短缩等。 */
    private String tongueState;

    /** 舌下脉络:舌下静脉颜色、粗细、迂曲程度,反映气血瘀滞。 */
    private String sublingualVein;

    /** 齿痕:舌边齿痕的有无及明显程度。 */
    private String toothMarks;

    /** 裂纹:舌面裂纹位置、形态、深浅。 */
    private String cracks;

    /** 点刺:红点、芒刺的分布与色泽。 */
    private String spots;

    /** 津液:润、燥、滑、少津等状况。 */
    private String moisture;

    /** 中医体质辨识(平和/气虚/阳虚/阴虚/痰湿/湿热/血瘀/气郁/特禀)。 */
    private String constitution;

    /** 可能的中医证型提示。 */
    private String syndrome;

    /** 饮食建议。 */
    private String dietAdvice;

    /** 起居与运动建议。 */
    private String lifestyleAdvice;

    /** 综合调养建议(兼容旧字段)。 */
    private String suggestion;

    /** 图像质量评估:光线、对焦、完整度等。 */
    private String imageQuality;

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

    public String getTongueState() { return tongueState; }
    public void setTongueState(String tongueState) { this.tongueState = tongueState; }

    public String getSublingualVein() { return sublingualVein; }
    public void setSublingualVein(String sublingualVein) { this.sublingualVein = sublingualVein; }

    public String getToothMarks() { return toothMarks; }
    public void setToothMarks(String toothMarks) { this.toothMarks = toothMarks; }

    public String getCracks() { return cracks; }
    public void setCracks(String cracks) { this.cracks = cracks; }

    public String getSpots() { return spots; }
    public void setSpots(String spots) { this.spots = spots; }

    public String getMoisture() { return moisture; }
    public void setMoisture(String moisture) { this.moisture = moisture; }

    public String getConstitution() { return constitution; }
    public void setConstitution(String constitution) { this.constitution = constitution; }

    public String getSyndrome() { return syndrome; }
    public void setSyndrome(String syndrome) { this.syndrome = syndrome; }

    public String getDietAdvice() { return dietAdvice; }
    public void setDietAdvice(String dietAdvice) { this.dietAdvice = dietAdvice; }

    public String getLifestyleAdvice() { return lifestyleAdvice; }
    public void setLifestyleAdvice(String lifestyleAdvice) { this.lifestyleAdvice = lifestyleAdvice; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    public String getImageQuality() { return imageQuality; }
    public void setImageQuality(String imageQuality) { this.imageQuality = imageQuality; }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
}
