package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.dao.DietPlanDao;
import com.dahaiwuliang.entity.DietPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DietPlanService {

    @Autowired
    private DietPlanDao dietPlanDao;

    public Page<DietPlan> list(int pageNum, int pageSize, Integer orderId, Integer customerId, Integer staffId) {
        Page<DietPlan> page = new Page<>(pageNum, pageSize);
        QueryWrapper<DietPlan> wrapper = new QueryWrapper<>();
        if (orderId != null) {
            wrapper.eq("order_id", orderId);
        }
        if (customerId != null) {
            wrapper.eq("customer_id", customerId);
        }
        if (staffId != null) {
            wrapper.eq("staff_id", staffId);
        }
        wrapper.orderByDesc("create_time");
        return dietPlanDao.selectPage(page, wrapper);
    }

    public DietPlan getById(Integer id) {
        return dietPlanDao.selectById(id);
    }

    public int save(DietPlan plan) {
        return dietPlanDao.insert(plan);
    }

    public int updateById(DietPlan plan) {
        return dietPlanDao.updateById(plan);
    }

    public int removeById(Integer id) {
        return dietPlanDao.deleteById(id);
    }
}
