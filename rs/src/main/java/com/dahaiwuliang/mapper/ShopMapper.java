package com.dahaiwuliang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dahaiwuliang.entity.Shop;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 店铺 Mapper
 */
public interface ShopMapper extends BaseMapper<Shop> {

    /** 菜系分布统计 (name=菜系, value=店铺数) */
    @Select("SELECT c.name AS name, COUNT(s.id) AS value FROM shop s " +
            "LEFT JOIN category c ON s.category_id = c.id " +
            "WHERE s.status = 1 GROUP BY c.id, c.name ORDER BY value DESC")
    List<Map<String, Object>> categoryDistribution();

    /** 人均消费区间分布 (name=区间, value=店铺数) */
    @Select("SELECT seg AS name, COUNT(*) AS value FROM (" +
            "  SELECT CASE " +
            "    WHEN avg_price < 50 THEN '0-50' " +
            "    WHEN avg_price < 100 THEN '50-100' " +
            "    WHEN avg_price < 150 THEN '100-150' " +
            "    WHEN avg_price < 200 THEN '150-200' " +
            "    ELSE '200+' END AS seg " +
            "  FROM shop WHERE status = 1) t " +
            "GROUP BY seg ORDER BY FIELD(seg,'0-50','50-100','100-150','150-200','200+')")
    List<Map<String, Object>> priceDistribution();
}
