package com.example.mapper;

import com.example.entity.PointsExchange;

import java.util.List;

public interface PointsExchangeMapper {


    void insert(PointsExchange exchange);

    void updateById(PointsExchange exchange);

    PointsExchange selectById(Integer id);

    List<PointsExchange> selectByUser(Integer userId);

    List<PointsExchange> selectAll(Integer status);
}
