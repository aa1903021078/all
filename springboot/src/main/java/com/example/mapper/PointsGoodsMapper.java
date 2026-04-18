package com.example.mapper;

import com.example.entity.PointsGoods;

import java.util.List;

public interface PointsGoodsMapper {

    void insert(PointsGoods pointsGoods);

    void deleteById(Integer id);

    void updateById(PointsGoods pointsGoods);

    PointsGoods selectById(Integer id);

    List<PointsGoods> selectAll(PointsGoods pointsGoods);

    List<PointsGoods> selectAvailable();

    PointsGoods selectByIdWithVersion(Integer goodsId);

    int decreaseStockWithVersion(Integer goodsId, Integer oldVersion);
}
