package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dahaiwuliang.dao.OrderMaternalDao;
import com.dahaiwuliang.entity.OrderMaternal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderMaternalService {

    @Autowired
    private OrderMaternalDao orderMaternalDao;

    public OrderMaternal findByOrderId(Integer orderId) {
        QueryWrapper<OrderMaternal> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        return orderMaternalDao.selectOne(wrapper);
    }

    public OrderMaternal getById(Integer id) {
        return orderMaternalDao.selectById(id);
    }

    public int save(OrderMaternal maternal) {
        return orderMaternalDao.insert(maternal);
    }

    public int updateById(OrderMaternal maternal) {
        return orderMaternalDao.updateById(maternal);
    }

    public int removeById(Integer id) {
        return orderMaternalDao.deleteById(id);
    }
}
