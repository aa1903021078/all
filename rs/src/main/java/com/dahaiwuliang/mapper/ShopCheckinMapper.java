package com.dahaiwuliang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dahaiwuliang.entity.ShopCheckin;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 点亮店铺 Mapper
 */
public interface ShopCheckinMapper extends BaseMapper<ShopCheckin> {

    /** 点亮趋势 (date=日期, value=数量), shopId 为空则统计全平台 */
    @Select("<script>" +
            "SELECT DATE(checkin_time) AS date, COUNT(*) AS value FROM shop_checkin " +
            "<where> checkin_time IS NOT NULL " +
            "<if test='shopId != null'> AND shop_id = #{shopId} </if>" +
            "</where>" +
            "GROUP BY DATE(checkin_time) ORDER BY date" +
            "</script>")
    List<Map<String, Object>> checkinTrend(@Param("shopId") Long shopId);
}
