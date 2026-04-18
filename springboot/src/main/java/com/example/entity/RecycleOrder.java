package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RecycleOrder {
    private Integer id;
    private String orderNo;
    private Integer userId;
    private Integer collectorId;
    private Integer addressId;
    private String addressDetail;
    private BigDecimal addressLat;
    private BigDecimal addressLng;
    private Integer buildingType;  /** 1电梯房 2楼梯房 */
    private Integer floor;
    private String contactName;
    private String contactPhone;
    private Integer applianceTypeId;
    private String applianceTypeName;
    private String applianceDesc;
    private String applianceImgs;
    /** 品牌等级  可选值：一线品牌、二线品牌、杂牌
     * 影响系数：一线品牌×1.2，二线品牌×1.0，杂牌×0.8 */
    private String brandLevel;
    /**使用年限   可选值：1年内、2-3年、5年以上
     * 影响系数：1年内×1.0，2-3年×0.8，5年以上×0.6  */
    private String usageYears;
    /** 功能状况  可选值：正常使用、零件损坏、无法开机
     * 影响系数：正常使用×1.0，零件损坏×0.7，无法开机×0.4  */
    private String functionalStatus;
    /** 外观成色  可选值：完好、轻微划痕、破损
     * 影响系数：完好×1.0，轻微划痕×0.9，破损×0.7  */
    private String appearanceLevel;
    /** 机况综合系数（缓存字段）
     * 计算公式：品牌系数 × 年限系数 × 功能系数 × 成色系数  示例：0.6（表示基准价的60%）
     * 用于后续快速计算价格，避免重复运算  */
    private BigDecimal conditionFactor;
    private BigDecimal estimatedWeight;
    private LocalDate expectDate;
    private LocalTime expectTimeStart;
    private LocalTime expectTimeEnd;
    private Integer urgencyLevel;  /** 1普通 2较急 3紧急 */
    private Integer assignType;//  /** 1智能派单 2手动派单 3回收员抢单 */
    private LocalDateTime assignTime;//
    private LocalDateTime acceptTime;//
    private LocalDateTime arriveTime;//
    private LocalDateTime completeTime;//
    /**  预估价格（基于机况计算）
     * 计算公式：(priceMin + priceMax) / 2 × conditionFactor  单位：元  */
    private BigDecimal priceEstimate;//
    private BigDecimal priceActual;//
    private Integer pointsEarned;
    private BigDecimal weightActual;//
    private BigDecimal carbonSaved;
    private Integer status; /** 0待分配 1待接单 2已接单 3已到达 4已完成 5已取消 */
    private String cancelReason;//
    private Integer userRating;//
    private String userComment;//
    private Integer collectorRating;//
    private LocalDateTime createTime;//
    private LocalDateTime updateTime;//

    //  乐观锁字段
    private Integer version;

    // 关联字段
    private String collectorName;
    private String collectorPhone;
    private BigDecimal collectorLat;
    private BigDecimal collectorLng;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getCollectorId() {
        return collectorId;
    }

    public void setCollectorId(Integer collectorId) {
        this.collectorId = collectorId;
    }

    public Integer getAddressId() {
        return addressId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    public String getAddressDetail() {
        return addressDetail;
    }

    public void setAddressDetail(String addressDetail) {
        this.addressDetail = addressDetail;
    }

    public BigDecimal getAddressLat() {
        return addressLat;
    }

    public void setAddressLat(BigDecimal addressLat) {
        this.addressLat = addressLat;
    }

    public BigDecimal getAddressLng() {
        return addressLng;
    }

    public void setAddressLng(BigDecimal addressLng) {
        this.addressLng = addressLng;
    }

    public Integer getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(Integer buildingType) {
        this.buildingType = buildingType;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Integer getApplianceTypeId() {
        return applianceTypeId;
    }

    public void setApplianceTypeId(Integer applianceTypeId) {
        this.applianceTypeId = applianceTypeId;
    }

    public String getApplianceTypeName() {
        return applianceTypeName;
    }

    public void setApplianceTypeName(String applianceTypeName) {
        this.applianceTypeName = applianceTypeName;
    }

    public String getApplianceDesc() {
        return applianceDesc;
    }

    public void setApplianceDesc(String applianceDesc) {
        this.applianceDesc = applianceDesc;
    }

    public String getApplianceImgs() {
        return applianceImgs;
    }

    public void setApplianceImgs(String applianceImgs) {
        this.applianceImgs = applianceImgs;
    }

    public String getBrandLevel() {
        return brandLevel;
    }

    public void setBrandLevel(String brandLevel) {
        this.brandLevel = brandLevel;
    }

    public String getUsageYears() {
        return usageYears;
    }

    public void setUsageYears(String usageYears) {
        this.usageYears = usageYears;
    }

    public String getFunctionalStatus() {
        return functionalStatus;
    }

    public void setFunctionalStatus(String functionalStatus) {
        this.functionalStatus = functionalStatus;
    }

    public String getAppearanceLevel() {
        return appearanceLevel;
    }

    public void setAppearanceLevel(String appearanceLevel) {
        this.appearanceLevel = appearanceLevel;
    }

    public BigDecimal getConditionFactor() {
        return conditionFactor;
    }

    public void setConditionFactor(BigDecimal conditionFactor) {
        this.conditionFactor = conditionFactor;
    }

    public BigDecimal getEstimatedWeight() {
        return estimatedWeight;
    }

    public void setEstimatedWeight(BigDecimal estimatedWeight) {
        this.estimatedWeight = estimatedWeight;
    }

    public LocalDate getExpectDate() {
        return expectDate;
    }

    public void setExpectDate(LocalDate expectDate) {
        this.expectDate = expectDate;
    }

    public LocalTime getExpectTimeStart() {
        return expectTimeStart;
    }

    public void setExpectTimeStart(LocalTime expectTimeStart) {
        this.expectTimeStart = expectTimeStart;
    }

    public LocalTime getExpectTimeEnd() {
        return expectTimeEnd;
    }

    public void setExpectTimeEnd(LocalTime expectTimeEnd) {
        this.expectTimeEnd = expectTimeEnd;
    }

    public Integer getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(Integer urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public Integer getAssignType() {
        return assignType;
    }

    public void setAssignType(Integer assignType) {
        this.assignType = assignType;
    }

    public LocalDateTime getAssignTime() {
        return assignTime;
    }

    public void setAssignTime(LocalDateTime assignTime) {
        this.assignTime = assignTime;
    }

    public LocalDateTime getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(LocalDateTime acceptTime) {
        this.acceptTime = acceptTime;
    }

    public LocalDateTime getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(LocalDateTime arriveTime) {
        this.arriveTime = arriveTime;
    }

    public LocalDateTime getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(LocalDateTime completeTime) {
        this.completeTime = completeTime;
    }

    public BigDecimal getPriceEstimate() {
        return priceEstimate;
    }

    public void setPriceEstimate(BigDecimal priceEstimate) {
        this.priceEstimate = priceEstimate;
    }

    public BigDecimal getPriceActual() {
        return priceActual;
    }

    public void setPriceActual(BigDecimal priceActual) {
        this.priceActual = priceActual;
    }

    public Integer getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(Integer pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public BigDecimal getWeightActual() {
        return weightActual;
    }

    public void setWeightActual(BigDecimal weightActual) {
        this.weightActual = weightActual;
    }

    public BigDecimal getCarbonSaved() {
        return carbonSaved;
    }

    public void setCarbonSaved(BigDecimal carbonSaved) {
        this.carbonSaved = carbonSaved;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Integer getUserRating() {
        return userRating;
    }

    public void setUserRating(Integer userRating) {
        this.userRating = userRating;
    }

    public String getUserComment() {
        return userComment;
    }

    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }

    public Integer getCollectorRating() {
        return collectorRating;
    }

    public void setCollectorRating(Integer collectorRating) {
        this.collectorRating = collectorRating;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getCollectorName() {
        return collectorName;
    }

    public void setCollectorName(String collectorName) {
        this.collectorName = collectorName;
    }

    public String getCollectorPhone() {
        return collectorPhone;
    }

    public void setCollectorPhone(String collectorPhone) {
        this.collectorPhone = collectorPhone;
    }

    public BigDecimal getCollectorLat() {
        return collectorLat;
    }

    public void setCollectorLat(BigDecimal collectorLat) {
        this.collectorLat = collectorLat;
    }

    public BigDecimal getCollectorLng() {
        return collectorLng;
    }

    public void setCollectorLng(BigDecimal collectorLng) {
        this.collectorLng = collectorLng;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}