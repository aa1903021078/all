package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.*;
import com.example.exception.CustomException;
import com.example.mapper.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 回收订单业务处理
 */
@Service
public class RecycleOrderService {

    @Resource
    private RecycleOrderMapper recycleOrderMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private CollectorMapper collectorMapper;

    @Resource
    private UserService userService;

    @Resource
    private CollectorService collectorService;

    @Resource
    private ApplianceTypeService applianceTypeService;

    @Resource
    private PointsRecordMapper pointsRecordMapper;

    @Resource
    private DailyRecycleStatsMapper dailyRecycleStatsMapper;

    @Resource
    private DispatchRecordMapper dispatchRecordMapper;
    @Autowired
    private ApplianceTypeMapper applianceTypeMapper;

    /**
     *创建订单
     * @Transactional 是Spring的事务管理注解，表示该方法需要事务支持，出错自动撤销所有操作
     */
    /**
     * 创建订单（修改版，添加机况价格计算）
     */
    @Transactional
    public RecycleOrder create(RecycleOrder recycleOrder) {
        // 校验用户
        User user = userMapper.selectById(recycleOrder.getUserId());
        if (ObjectUtil.isNull(user)) {
            throw new CustomException("用户不存在");
        }

        // 查询品类信息
        if (recycleOrder.getApplianceTypeId() != null) {
            ApplianceType applianceType = applianceTypeMapper.selectById(recycleOrder.getApplianceTypeId());
            if (ObjectUtil.isNull(applianceType)) {
                throw new CustomException("回收品类不存在");
            }

            // 设置品类名称（冗余存储，方便查询）
            recycleOrder.setApplianceTypeName(applianceType.getName());

            // 获取预估重量（用户输入优先，否则用品类默认值）
            BigDecimal weight = recycleOrder.getEstimatedWeight();
            if (weight == null || weight.compareTo(BigDecimal.ZERO) == 0) {
                weight = applianceType.getWeightEstimate();
                recycleOrder.setEstimatedWeight(weight);
            }

            // 计算预估积分 = 重量(kg) × 每公斤积分
            BigDecimal pointsDecimal = weight.multiply(applianceType.getPointsPerKg());
            Integer points = pointsDecimal.setScale(0, RoundingMode.DOWN).intValue();
            recycleOrder.setPointsEarned(points);

            // 计算预估碳减排 = 重量(kg) × 减碳系数
            BigDecimal carbonSaved = weight.multiply(applianceType.getCarbonFactor())
                    .setScale(2, RoundingMode.HALF_UP);
            recycleOrder.setCarbonSaved(carbonSaved);

            // ===== 新增：基于机况计算预估价格 =====
            // 计算机况综合系数
            BigDecimal conditionFactor = calculateConditionFactor(
                    recycleOrder.getBrandLevel(),
                    recycleOrder.getUsageYears(),
                    recycleOrder.getFunctionalStatus(),
                    recycleOrder.getAppearanceLevel()
            );
            recycleOrder.setConditionFactor(conditionFactor);

            // 计算预估价格
            BigDecimal basePrice = calculateBasePrice(applianceType);
            if (basePrice != null) {
                BigDecimal priceEstimate = basePrice.multiply(conditionFactor);

                // 限制在价格区间内
                if (applianceType.getPriceMin() != null && priceEstimate.compareTo(applianceType.getPriceMin()) < 0) {
                    priceEstimate = applianceType.getPriceMin();
                }
                if (applianceType.getPriceMax() != null && priceEstimate.compareTo(applianceType.getPriceMax()) > 0) {
                    priceEstimate = applianceType.getPriceMax();
                }

                recycleOrder.setPriceEstimate(priceEstimate.setScale(2, RoundingMode.HALF_UP));
            }
        }

        // 生成订单号
        String orderNo = "REC" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") +
                UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        recycleOrder.setOrderNo(orderNo);

        // 设置默认值
        recycleOrder.setStatus(0); // 待分配

        recycleOrderMapper.insert(recycleOrder);
        return recycleOrder;
    }

    /**
     * 计算品类基准价（辅助方法）
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
     * 计算机况综合系数（辅助方法）
     */
    private BigDecimal calculateConditionFactor(String brandLevel, String usageYears,
                                                String functionalStatus, String appearanceLevel) {
        BigDecimal brandFactor = switch (brandLevel) {
            case "一线品牌" -> new BigDecimal("1.2");
            case "二线品牌" -> new BigDecimal("1.0");
            case "杂牌" -> new BigDecimal("0.8");
            default -> new BigDecimal("1.0");
        };

        BigDecimal yearFactor = switch (usageYears) {
            case "1年内" -> new BigDecimal("1.0");
            case "2-3年" -> new BigDecimal("0.8");
            case "5年以上" -> new BigDecimal("0.6");
            default -> new BigDecimal("0.8");
        };

        BigDecimal functionFactor = switch (functionalStatus) {
            case "正常使用" -> new BigDecimal("1.0");
            case "零件损坏" -> new BigDecimal("0.7");
            case "无法开机" -> new BigDecimal("0.4");
            default -> new BigDecimal("0.7");
        };

        BigDecimal appearanceFactor = switch (appearanceLevel) {
            case "完好" -> new BigDecimal("1.0");
            case "轻微划痕" -> new BigDecimal("0.9");
            case "破损" -> new BigDecimal("0.7");
            default -> new BigDecimal("0.9");
        };

        return brandFactor.multiply(yearFactor)
                .multiply(functionFactor)
                .multiply(appearanceFactor)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 完成订单（使用实际重量重新计算）
     */
    @Transactional
    public void complete(RecycleOrder orderParam) {
        RecycleOrder recycleOrder = recycleOrderMapper.selectById(orderParam.getId());
        if (ObjectUtil.isNull(recycleOrder)) {
            throw new CustomException("订单不存在");
        }
        if (recycleOrder.getStatus() != 2 && recycleOrder.getStatus() != 3) {
            throw new CustomException("订单状态错误");
        }

        // 更新实际重量、价格等信息
        recycleOrder.setWeightActual(orderParam.getWeightActual());
        recycleOrder.setPriceActual(orderParam.getPriceActual());
        recycleOrder.setStatus(4); // 已完成
        recycleOrder.setCompleteTime(LocalDateTime.now());

        //  重新计算实际积分和减碳量（根据实际重量 × 每公斤积分）
        if (recycleOrder.getApplianceTypeId() != null && orderParam.getWeightActual() != null) {
            ApplianceType applianceType = applianceTypeMapper.selectById(recycleOrder.getApplianceTypeId());
            if (ObjectUtil.isNotNull(applianceType)) {
                BigDecimal actualWeight = orderParam.getWeightActual();

                // 实际积分 = 实际重量(kg) × 每公斤积分
                BigDecimal actualPointsDecimal = actualWeight.multiply(applianceType.getPointsPerKg());
                Integer actualPoints = actualPointsDecimal.setScale(0, RoundingMode.DOWN).intValue();
                recycleOrder.setPointsEarned(actualPoints);

                // 实际碳减排 = 实际重量(kg) × 减碳系数
                BigDecimal actualCarbon = actualWeight.multiply(applianceType.getCarbonFactor())
                        .setScale(2, RoundingMode.HALF_UP);
                recycleOrder.setCarbonSaved(actualCarbon);
            }
        }
        recycleOrderMapper.updateById(recycleOrder);

        // 更新用户积分和统计
        User user = userMapper.selectById(recycleOrder.getUserId());
        if (ObjectUtil.isNotNull(user)) {
            user.setPoints(user.getPoints() + recycleOrder.getPointsEarned());
            user.setTotalRecycleCount(user.getTotalRecycleCount() + 1);
            user.setTotalRecycleWeight(user.getTotalRecycleWeight().add(
                    recycleOrder.getWeightActual() != null ? recycleOrder.getWeightActual() : BigDecimal.ZERO));
            user.setCarbonSaved(user.getCarbonSaved().add(recycleOrder.getCarbonSaved()));
            userMapper.updateById(user);

            // 添加积分记录
            PointsRecord pointsRecord = new PointsRecord();
            pointsRecord.setUserId(user.getId());
            pointsRecord.setOrderId(recycleOrder.getId());
            pointsRecord.setPoints(recycleOrder.getPointsEarned());
            pointsRecord.setType(1); // 回收获得
            pointsRecord.setDescription("完成回收订单：" + recycleOrder.getApplianceTypeName());
            pointsRecord.setBalance(user.getPoints());
            pointsRecordMapper.insert(pointsRecord);
        }

        // 更新每日统计
        updateDailyStats(recycleOrder);
    }


    /**
     * 智能派单
     */
    @Transactional
    public void dispatchOrder(Integer orderId) {
        RecycleOrder recycleOrder = recycleOrderMapper.selectById(orderId);
        if (ObjectUtil.isNull(recycleOrder)) {
            throw new CustomException("订单不存在");
        }
        if (recycleOrder.getStatus() != 0) {
            throw new CustomException("订单状态错误，无法派单");
        }
        // 查询附近可用回收员
        List<Collector> nearbyCollectors = collectorMapper.selectNearby(
                recycleOrder.getAddressLat().doubleValue(),
                recycleOrder.getAddressLng().doubleValue(),
                5000.0 // 5公里范围
        );

        if (nearbyCollectors.isEmpty()) {
            throw new CustomException("附近暂无可用回收员");
        }

        // 选择最合适的回收员（距离最近且接单数少）
        Collector selectedCollector = nearbyCollectors.get(0);

        // 更新订单
        recycleOrder.setCollectorId(selectedCollector.getId());
        recycleOrder.setStatus(1); // 待接单
        recycleOrder.setAssignType(1); // 智能派单
        recycleOrder.setAssignTime(LocalDateTime.now());
        recycleOrderMapper.updateById(recycleOrder);

        // 记录派单日志
        DispatchRecord dispatchRecord = new DispatchRecord();
        dispatchRecord.setOrderId(orderId);
        dispatchRecord.setCollectorId(selectedCollector.getId());
        dispatchRecord.setDispatchType("auto");
        // 计算距离（简化）
        dispatchRecord.setDistance(new BigDecimal("1.5")); // 实际应计算
        dispatchRecord.setEstimatedTime(15);
        dispatchRecordMapper.insert(dispatchRecord);
    }

    /**
     * 手动派单
     */
    @Transactional
    public void manualDispatch(Integer orderId, Integer collectorId) {
        RecycleOrder recycleOrder = recycleOrderMapper.selectById(orderId);
        if (ObjectUtil.isNull(recycleOrder)) {
            throw new CustomException("订单不存在");
        }

        Collector collector = collectorMapper.selectById(collectorId);
        if (ObjectUtil.isNull(collector)) {
            throw new CustomException("回收员不存在");
        }

        if (recycleOrder.getStatus() != 0) {
            throw new CustomException("订单状态错误");
        }

        recycleOrder.setCollectorId(collectorId);
        recycleOrder.setStatus(1);
        recycleOrder.setAssignType(2); // 手动派单
        recycleOrder.setAssignTime(LocalDateTime.now());
        recycleOrderMapper.updateById(recycleOrder);

        // 记录派单
        DispatchRecord record = new DispatchRecord();
        record.setOrderId(orderId);
        record.setCollectorId(collectorId);
        record.setDispatchType("manual");
        dispatchRecordMapper.insert(record);
    }

    /**
     * 回收员抢单（将待分配订单分配给自己）
     */
    @Transactional
    public void grabOrder(Integer orderId, Integer collectorId) {
        // 1. 查询订单（带版本号）
        RecycleOrder recycleOrder = recycleOrderMapper.selectByIdWithVersion(orderId);
        if (ObjectUtil.isNull(recycleOrder)) {
            throw new CustomException("订单不存在");
        }

        // 2. 业务校验（提前过滤）
        if (recycleOrder.getStatus() != 0) {
            throw new CustomException("手慢了，该订单已被抢走");
        }

        // 3. 校验回收员
        Collector collector = collectorMapper.selectById(collectorId);
        if (ObjectUtil.isNull(collector)) {
            throw new CustomException("回收员不存在");
        }

        Integer oldVersion = recycleOrder.getVersion();

        // 4. 乐观锁更新（CAS操作）
        int affected = recycleOrderMapper.grabOrderWithVersion(orderId, collectorId, oldVersion);

        // 5. 判断更新结果
        if (affected == 0) {
            throw new CustomException("手慢了，该订单已被其他回收员接单");
        }

        // 6. 记录派单日志（仅在更新成功后执行）
        DispatchRecord record = new DispatchRecord();
        record.setOrderId(orderId);
        record.setCollectorId(collectorId);
        record.setDispatchType("grab");
        dispatchRecordMapper.insert(record);
    }

    /**
     * 回收员接单
     */
    @Transactional
    public void acceptOrder(Integer orderId, Integer collectorId) {
        RecycleOrder recycleOrder = recycleOrderMapper.selectById(orderId);
        if (ObjectUtil.isNull(recycleOrder)) {
            throw new CustomException("订单不存在");
        }

        if (!recycleOrder.getCollectorId().equals(collectorId)) {
            throw new CustomException("无权操作此订单");
        }

        if (recycleOrder.getStatus() != 1) {
            throw new CustomException("订单状态错误");
        }

        // 检查回收员今日接单数
        Collector collector = collectorMapper.selectById(collectorId);
        if (collector.getTodayOrderCount() >= collector.getMaxDailyOrders()) {
            throw new CustomException("今日接单数已达上限");
        }

        recycleOrder.setStatus(2); // 已接单
        recycleOrder.setAcceptTime(LocalDateTime.now());
        recycleOrderMapper.updateById(recycleOrder);

        // 更新回收员接单数
        collectorService.incrementOrderCount(collectorId);

        // 更新派单记录
        dispatchRecordMapper.updateAcceptStatus(orderId, collectorId, 1);
    }

    /**
     * 回收员到达
     */
    public void arrive(Integer orderId) {
        RecycleOrder recycleOrder = recycleOrderMapper.selectById(orderId);
        if (ObjectUtil.isNull(recycleOrder)) {
            throw new CustomException("订单不存在");
        }
        if (recycleOrder.getStatus() != 2) {
            throw new CustomException("订单状态错误");
        }

        recycleOrder.setStatus(3); // 已到达
        recycleOrder.setArriveTime(LocalDateTime.now());
        recycleOrderMapper.updateById(recycleOrder);
    }

    /**
     * 取消订单
     */
    @Transactional
    public void cancel(Integer orderId, String reason) {
        RecycleOrder recycleOrder = recycleOrderMapper.selectById(orderId);
        if (ObjectUtil.isNull(recycleOrder)) {
            throw new CustomException("订单不存在");
        }
        if (recycleOrder.getStatus() == 4 || recycleOrder.getStatus() == 5) {
            throw new CustomException("订单已完成或已取消");
        }

        recycleOrder.setStatus(5); // 已取消
        recycleOrder.setCancelReason(reason);
        recycleOrderMapper.updateById(recycleOrder);

        // 如果已派单，更新派单记录
        if (recycleOrder.getCollectorId() != null) {
            dispatchRecordMapper.updateAcceptStatus(orderId, recycleOrder.getCollectorId(), 0);
        }
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        recycleOrderMapper.deleteById(id);
    }

    /**
     * 根据ID查询
     */
    public RecycleOrder selectById(Integer id) {
        return recycleOrderMapper.selectById(id);
    }

    /**
     * 查询用户的所有订单
     */
    public List<RecycleOrder> selectByUser(Integer userId) {
        return recycleOrderMapper.selectByUser(userId);
    }

    /**
     * 查询回收员的所有订单
     */
    public List<RecycleOrder> selectByCollector(Integer collectorId) {
        return recycleOrderMapper.selectByCollector(collectorId);
    }

    /**
     * 查询所有
     */
    public List<RecycleOrder> selectAll(RecycleOrder recycleOrder) {
        return recycleOrderMapper.selectAll(recycleOrder);
    }

    /**
     * 分页查询
     */
    public PageInfo<RecycleOrder> selectPage(RecycleOrder recycleOrder, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<RecycleOrder> list = recycleOrderMapper.selectAll(recycleOrder);
        return PageInfo.of(list);
    }

    /**
     * 查询待派单订单
     */
    public List<RecycleOrder> selectPendingDispatch() {
        return recycleOrderMapper.selectByStatus(0);
    }

    /**
     * 根据状态查询订单（用于抢单大厅）
     * 如果订单没有预估价格（旧数据），则根据系数补充计算
     */
    public List<RecycleOrder> selectByStatus(Integer status) {
        List<RecycleOrder> recycleOrders = recycleOrderMapper.selectByStatus(status);

        // 为没有预估价格的订单补充计算（兼容旧数据）
        for (RecycleOrder recycleOrder : recycleOrders) {
            if (recycleOrder.getPriceEstimate() == null && recycleOrder.getApplianceTypeId() != null) {
                // 如果有缓存的系数，直接使用
                if (recycleOrder.getConditionFactor() != null) {
                    BigDecimal price = applianceTypeService.calculatePriceByFactor(
                            recycleOrder.getApplianceTypeId(),
                            recycleOrder.getConditionFactor()
                    );
                    recycleOrder.setPriceEstimate(price);
                }
                // 如果有详细机况，重新计算
                else if (recycleOrder.getBrandLevel() != null) {
                    BigDecimal price = applianceTypeService.calculatePriceByCondition(
                            recycleOrder.getApplianceTypeId(),
                            recycleOrder.getBrandLevel(),
                            recycleOrder.getUsageYears(),
                            recycleOrder.getFunctionalStatus(),
                            recycleOrder.getAppearanceLevel()
                    );
                    recycleOrder.setPriceEstimate(price);
                }
            }
        }

        return recycleOrders;
    }

    /**
     * 用户评分
     */
    public void rateOrder(Integer orderId, Integer rating, String comment) {
        RecycleOrder recycleOrder = recycleOrderMapper.selectById(orderId);
        if (ObjectUtil.isNull(recycleOrder)) {
            throw new CustomException("订单不存在");
        }
        if (recycleOrder.getStatus() != 4) {
            throw new CustomException("订单未完成，无法评分");
        }

        recycleOrder.setUserRating(rating);
        recycleOrder.setUserComment(comment);
        recycleOrderMapper.updateById(recycleOrder);

        // 更新回收员评分
        updateCollectorRating(recycleOrder.getCollectorId());
    }

    /**
     * 更新回收员评分
     */
    private void updateCollectorRating(Integer collectorId) {
        BigDecimal avgRating = recycleOrderMapper.selectAvgRatingByCollector(collectorId);
        if (avgRating != null) {
            Collector collector = collectorMapper.selectById(collectorId);
            collector.setRating(avgRating.setScale(1, RoundingMode.HALF_UP));
            collectorMapper.updateById(collector);
        }
    }

    /**
     * 更新每日统计
     */
    private void updateDailyStats(RecycleOrder recycleOrder) {
        String today = DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd");
        DailyRecycleStats stats = dailyRecycleStatsMapper.selectByDate(today);

        if (stats == null) {
            stats = new DailyRecycleStats();
            stats.setStatDate(java.time.LocalDate.now());
            stats.setTotalOrders(1);
            stats.setCompletedOrders(1);
            stats.setTotalWeight(recycleOrder.getWeightActual() != null ? recycleOrder.getWeightActual() : BigDecimal.ZERO);
            stats.setTotalCarbonSaved(recycleOrder.getCarbonSaved());
            stats.setTotalPointsGiven(recycleOrder.getPointsEarned());
            stats.setActiveUsers(1);
            stats.setActiveCollectors(1);
            dailyRecycleStatsMapper.insert(stats);
        } else {
            stats.setTotalOrders(stats.getTotalOrders() + 1);
            stats.setCompletedOrders(stats.getCompletedOrders() + 1);
            stats.setTotalWeight(stats.getTotalWeight().add(
                    recycleOrder.getWeightActual() != null ? recycleOrder.getWeightActual() : BigDecimal.ZERO));
            stats.setTotalCarbonSaved(stats.getTotalCarbonSaved().add(recycleOrder.getCarbonSaved()));
            stats.setTotalPointsGiven(stats.getTotalPointsGiven() + recycleOrder.getPointsEarned());
            dailyRecycleStatsMapper.updateById(stats);
        }
    }

    /**
     * 获取统计数据
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", recycleOrderMapper.countTotal());
        stats.put("todayOrders", recycleOrderMapper.countToday());
        stats.put("pendingOrders", recycleOrderMapper.countByStatus(0));
        stats.put("completedOrders", recycleOrderMapper.countByStatus(4));
        stats.put("totalWeight", recycleOrderMapper.sumTotalWeight());
        stats.put("totalCarbonSaved", recycleOrderMapper.sumTotalCarbonSaved());
        return stats;
    }
}
