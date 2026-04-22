package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dahaiwuliang.dao.OrderInfantDao;
import com.dahaiwuliang.entity.OrderInfant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderInfantService {

    @Autowired
    private OrderInfantDao orderInfantDao;

    public List<OrderInfant> findByOrderId(Integer orderId) {
        QueryWrapper<OrderInfant> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        wrapper.orderByAsc("id");
        return orderInfantDao.selectList(wrapper);
    }

    public OrderInfant getById(Integer id) {
        return orderInfantDao.selectById(id);
    }

    public int save(OrderInfant infant) {
        return orderInfantDao.insert(infant);
    }

    public int updateById(OrderInfant infant) {
        return orderInfantDao.updateById(infant);
    }

    public int removeById(Integer id) {
        return orderInfantDao.deleteById(id);
    }
}
