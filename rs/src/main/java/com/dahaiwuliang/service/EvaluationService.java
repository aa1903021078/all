package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.dao.EvaluationDao;
import com.dahaiwuliang.entity.Evaluation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EvaluationService {

    @Autowired
    private EvaluationDao evaluationDao;

    public Page<Evaluation> list(int pageNum, int pageSize, Integer orderId, Integer customerId) {
        Page<Evaluation> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Evaluation> wrapper = new QueryWrapper<>();
        if (orderId != null) {
            wrapper.eq("order_id", orderId);
        }
        if (customerId != null) {
            wrapper.eq("customer_id", customerId);
        }
        wrapper.orderByDesc("create_time");
        return evaluationDao.selectPage(page, wrapper);
    }

    public Evaluation getById(Integer id) {
        return evaluationDao.selectById(id);
    }

    public int save(Evaluation evaluation) {
        return evaluationDao.insert(evaluation);
    }

    public int removeById(Integer id) {
        return evaluationDao.deleteById(id);
    }
}
