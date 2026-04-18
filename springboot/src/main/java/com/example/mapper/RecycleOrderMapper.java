package com.example.mapper;

import com.example.entity.RecycleOrder;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 操作recycle_order相关数据接口
 */
public interface RecycleOrderMapper {


    void insert(RecycleOrder recycleOrder);

    void deleteById(Integer id);


    void updateById(RecycleOrder recycleOrder);

    RecycleOrder selectById(Integer id);


    List<RecycleOrder> selectAll(RecycleOrder recycleOrder);

    List<RecycleOrder> selectByUser(Integer userId);

    List<RecycleOrder> selectByCollector(Integer collectorId);

    List<RecycleOrder> selectByStatus(Integer status);

    BigDecimal selectAvgRatingByCollector(Integer collectorId);

    int countTotal();

    int countToday();

    int countByStatus(@Param("status") Integer status);

    BigDecimal sumTotalWeight();

    BigDecimal sumTotalCarbonSaved();


    List<Map<String, Object>> selectApplianceTypeStats();

    List<Map<String, Object>> selectCollectorRanking(@Param("limit") Integer limit);

    RecycleOrder selectByIdWithVersion(Integer orderId);

    int grabOrderWithVersion(Integer orderId, Integer collectorId, Integer oldVersion);
}
