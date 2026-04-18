package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.entity.ApplianceType;
import com.example.exception.CustomException;
import com.example.mapper.ApplianceTypeMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 家电品类业务处理
 */
@Service
public class ApplianceTypeService {

    @Resource
    private ApplianceTypeMapper applianceTypeMapper;

    /**
     * 新增品类
     */
    public void add(ApplianceType applianceType) {
        // 校验名称是否重复
        ApplianceType exist = applianceTypeMapper.selectByName(applianceType.getName());
        if (ObjectUtil.isNotNull(exist)) {
            throw new CustomException("品类名称已存在");
        }

        // 设置默认值
        if (ObjectUtil.isEmpty(applianceType.getUnit())) {
            applianceType.setUnit("台");
        }
        // 默认每公斤10积分（BigDecimal）
        if (applianceType.getPointsPerKg() == null) {
            applianceType.setPointsPerKg(new BigDecimal("10.00"));
        }
        // 默认减碳系数2.5
        if (applianceType.getCarbonFactor() == null) {
            applianceType.setCarbonFactor(new BigDecimal("2.5"));
        }
        if (applianceType.getStatus() == null) {
            applianceType.setStatus(1);
        }
        if (applianceType.getSortOrder() == null) {
            applianceType.setSortOrder(0);
        }

        applianceTypeMapper.insert(applianceType);
    }

    /**
     * 删除品类
     */
    public void deleteById(Integer id) {
        applianceTypeMapper.deleteById(id);
    }

    /**
     * 修改品类
     */
    public void updateById(ApplianceType applianceType) {
        // 如果修改了名称，校验是否与其他品类冲突
        if (ObjectUtil.isNotEmpty(applianceType.getName())) {
            ApplianceType exist = applianceTypeMapper.selectByName(applianceType.getName());
            if (ObjectUtil.isNotNull(exist) && !exist.getId().equals(applianceType.getId())) {
                throw new CustomException("品类名称已存在");
            }
        }
        applianceTypeMapper.updateById(applianceType);
    }

    /**
     * 根据ID查询
     */
    public ApplianceType selectById(Integer id) {
        return applianceTypeMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<ApplianceType> selectAll(ApplianceType applianceType) {
        return applianceTypeMapper.selectAll(applianceType);
    }

    /**
     * 分页查询
     */
    public PageInfo<ApplianceType> selectPage(ApplianceType applianceType, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ApplianceType> list = applianceTypeMapper.selectAll(applianceType);
        return PageInfo.of(list);
    }

    /**
     * 查询启用的品类（前端展示用）
     */
    public List<ApplianceType> selectEnabled() {
        return applianceTypeMapper.selectEnabled();
    }

    /**
     * 计算预估积分（新公式：重量 × 每公斤积分）
     *
     * @param applianceTypeId 品类ID
     * @param weight 重量(kg)，如果为null则使用品类预估重量
     * @return 积分（整数，向下取整）
     */
    public Integer calculatePoints(Integer applianceTypeId, BigDecimal weight) {
        ApplianceType type = applianceTypeMapper.selectById(applianceTypeId);
        if (ObjectUtil.isNull(type)) {
            throw new CustomException("品类不存在");
        }

        // 如果未传入重量，使用品类预估重量
        if (weight == null || weight.compareTo(BigDecimal.ZERO) == 0) {
            weight = type.getWeightEstimate();
        }
        if (weight == null) {
            weight = BigDecimal.ZERO;
        }

        // 公式：积分 = 重量(kg) × 每公斤积分
        BigDecimal points = weight.multiply(type.getPointsPerKg());

        // 转为整数（向下取整）
        return points.setScale(0, RoundingMode.DOWN).intValue();
    }

    /**
     * 计算预估碳减排（公式：重量 × 减碳系数）
     *
     * @param applianceTypeId 品类ID
     * @param weight 重量(kg)，如果为null则使用品类预估重量
     * @return 碳减排量（保留2位小数）
     */
    public BigDecimal calculateCarbonSaved(Integer applianceTypeId, BigDecimal weight) {
        ApplianceType type = applianceTypeMapper.selectById(applianceTypeId);
        if (ObjectUtil.isNull(type)) {
            throw new CustomException("品类不存在");
        }

        // 如果未传入重量，使用品类预估重量
        if (weight == null || weight.compareTo(BigDecimal.ZERO) == 0) {
            weight = type.getWeightEstimate();
        }
        if (weight == null) {
            weight = BigDecimal.ZERO;
        }

        // 公式：碳减排 = 重量(kg) × 减碳系数
        return weight.multiply(type.getCarbonFactor()).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 基于机况的预估价格计算（市场化定价）
     * 公式：品类基准价 × 机况综合系数
     * 综合系数 = 品牌权重 × 年限折旧率 × 功能系数 × 成色系数
     *
     * @param applianceTypeId 品类ID
     * @param brandLevel 品牌等级（一线品牌/二线品牌/杂牌）
     * @param usageYears 使用年限（1年内/2-3年/5年以上）
     * @param functionalStatus 功能状况（正常使用/零件损坏/无法开机）
     * @param appearanceLevel 外观成色（完好/轻微划痕/破损）
     * @return 预估价格（保留2位小数）
     */
    public BigDecimal calculatePriceByCondition(Integer applianceTypeId,
                                                String brandLevel,
                                                String usageYears,
                                                String functionalStatus,
                                                String appearanceLevel) {
        // 1. 查询品类信息
        ApplianceType type = applianceTypeMapper.selectById(applianceTypeId);
        if (ObjectUtil.isNull(type)) {
            throw new CustomException("品类不存在");
        }

        // 2. 计算品类基准价（取price_min和price_max的中位数）
        BigDecimal basePrice = calculateBasePrice(type);
        if (basePrice == null) {
            return null;
        }

        // 3. 计算机况综合系数
        BigDecimal conditionFactor = calculateConditionFactor(
                brandLevel, usageYears, functionalStatus, appearanceLevel
        );

        // 4. 计算预估价 = 基准价 × 系数
        BigDecimal estimatePrice = basePrice.multiply(conditionFactor);

        // 5. 限制在价格区间内（防止过高或过低）
        if (type.getPriceMin() != null && estimatePrice.compareTo(type.getPriceMin()) < 0) {
            estimatePrice = type.getPriceMin();
        }
        if (type.getPriceMax() != null && estimatePrice.compareTo(type.getPriceMax()) > 0) {
            estimatePrice = type.getPriceMax();
        }

        return estimatePrice.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 根据品类名称和机况计算价格（用于订单查询）
     */
    public BigDecimal calculatePriceByNameAndCondition(String applianceTypeName,
                                                       String brandLevel,
                                                       String usageYears,
                                                       String functionalStatus,
                                                       String appearanceLevel) {
        ApplianceType type = applianceTypeMapper.selectByName(applianceTypeName);
        if (ObjectUtil.isNull(type)) {
            return null;
        }
        return calculatePriceByCondition(type.getId(), brandLevel, usageYears,
                functionalStatus, appearanceLevel);
    }

    /**
     * 根据系数快速计算价格（用于订单列表查询）
     */
    public BigDecimal calculatePriceByFactor(Integer applianceTypeId, BigDecimal conditionFactor) {
        if (conditionFactor == null || conditionFactor.compareTo(BigDecimal.ZERO) <= 0) {
            conditionFactor = BigDecimal.ONE;
        }

        ApplianceType type = applianceTypeMapper.selectById(applianceTypeId);
        if (ObjectUtil.isNull(type)) {
            return null;
        }

        BigDecimal basePrice = calculateBasePrice(type);
        if (basePrice == null) {
            return null;
        }

        BigDecimal estimatePrice = basePrice.multiply(conditionFactor);

        // 限制在价格区间内
        if (type.getPriceMin() != null && estimatePrice.compareTo(type.getPriceMin()) < 0) {
            estimatePrice = type.getPriceMin();
        }
        if (type.getPriceMax() != null && estimatePrice.compareTo(type.getPriceMax()) > 0) {
            estimatePrice = type.getPriceMax();
        }

        return estimatePrice.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算品类基准价（标准新机价）
     * 公式：(price_min + price_max) / 2
     */
    private BigDecimal calculateBasePrice(ApplianceType type) {
        if (type.getPriceMin() == null && type.getPriceMax() == null) {
            return null;
        }

        BigDecimal min = type.getPriceMin() != null ? type.getPriceMin() : BigDecimal.ZERO;
        BigDecimal max = type.getPriceMax() != null ? type.getPriceMax() : min;

        return min.add(max).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
    }

    /**
     * 计算机况综合调整系数
     */
    private BigDecimal calculateConditionFactor(String brandLevel,
                                                String usageYears,
                                                String functionalStatus,
                                                String appearanceLevel) {
        // 品牌权重系数
        BigDecimal brandFactor;
        switch (brandLevel) {
            case "一线品牌" -> brandFactor = new BigDecimal("1.2");
            case "二线品牌" -> brandFactor = new BigDecimal("1.0");
            case "杂牌" -> brandFactor = new BigDecimal("0.8");
            default -> brandFactor = new BigDecimal("1.0");
        }

        // 年限折旧系数
        BigDecimal yearFactor;
        switch (usageYears) {
            case "1年内" -> yearFactor = new BigDecimal("1.0");
            case "2-3年" -> yearFactor = new BigDecimal("0.8");
            case "5年以上" -> yearFactor = new BigDecimal("0.6");
            default -> yearFactor = new BigDecimal("0.8");
        }

        // 功能状况系数
        BigDecimal functionFactor;
        switch (functionalStatus) {
            case "正常使用" -> functionFactor = new BigDecimal("1.0");
            case "零件损坏" -> functionFactor = new BigDecimal("0.7");
            case "无法开机" -> functionFactor = new BigDecimal("0.4");
            default -> functionFactor = new BigDecimal("0.7");
        }

        // 外观成色系数
        BigDecimal appearanceFactor;
        switch (appearanceLevel) {
            case "完好" -> appearanceFactor = new BigDecimal("1.0");
            case "轻微划痕" -> appearanceFactor = new BigDecimal("0.9");
            case "破损" -> appearanceFactor = new BigDecimal("0.7");
            default -> appearanceFactor = new BigDecimal("0.9");
        }

        // 综合系数 = 各项相乘
        return brandFactor.multiply(yearFactor)
                .multiply(functionFactor)
                .multiply(appearanceFactor)
                .setScale(2, RoundingMode.HALF_UP);
    }
}