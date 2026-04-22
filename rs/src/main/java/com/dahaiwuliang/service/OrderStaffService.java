package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.dao.OrderStaffDao;
import com.dahaiwuliang.entity.OrderStaff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderStaffService {

    @Autowired
    private OrderStaffDao orderStaffDao;

    public Page<OrderStaff> list(int pageNum, int pageSize, Integer orderId) {
        Page<OrderStaff> page = new Page<>(pageNum, pageSize);
        QueryWrapper<OrderStaff> wrapper = new QueryWrapper<>();
        if (orderId != null) {
            wrapper.eq("order_id", orderId);
        }
        wrapper.orderByDesc("create_time");
        return orderStaffDao.selectPage(page, wrapper);
    }

    public int save(OrderStaff orderStaff) {
        return orderStaffDao.insert(orderStaff);
    }

    public int removeById(Integer id) {
        return orderStaffDao.deleteById(id);
    }

    public List<OrderStaff> findByOrderId(Integer orderId) {
        QueryWrapper<OrderStaff> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        return orderStaffDao.selectList(wrapper);
    }

    public OrderStaff getById(Integer id) {
        return orderStaffDao.selectById(id);
    }
}
