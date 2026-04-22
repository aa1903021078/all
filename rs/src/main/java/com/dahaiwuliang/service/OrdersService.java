package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.dao.OrdersDao;
import com.dahaiwuliang.entity.Orders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdersService {

    @Autowired
    private OrdersDao ordersDao;

    public Page<Orders> list(int pageNum, int pageSize, Integer status, Integer customerId, String orderNo) {
        Page<Orders> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Orders> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (customerId != null) {
            wrapper.eq("customer_id", customerId);
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like("order_no", orderNo);
        }
        wrapper.orderByDesc("create_time");
        return ordersDao.selectPage(page, wrapper);
    }

    public Orders getById(Integer id) {
        return ordersDao.selectById(id);
    }

    public int save(Orders orders) {
        return ordersDao.insert(orders);
    }

    public int updateById(Orders orders) {
        return ordersDao.updateById(orders);
    }

    public int removeById(Integer id) {
        return ordersDao.deleteById(id);
    }

    public List<Orders> findByCustomerId(Integer customerId) {
        QueryWrapper<Orders> wrapper = new QueryWrapper<>();
        wrapper.eq("customer_id", customerId);
        wrapper.orderByDesc("create_time");
        return ordersDao.selectList(wrapper);
    }

    public String generateOrderNo() {
        return "ORD" + System.currentTimeMillis();
    }
}
