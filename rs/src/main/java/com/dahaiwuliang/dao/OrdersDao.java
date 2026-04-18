package com.dahaiwuliang.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dahaiwuliang.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersDao extends BaseMapper<Orders> {
}
