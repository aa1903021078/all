package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.dao.CareRecordLifeDao;
import com.dahaiwuliang.entity.CareRecordLife;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CareRecordLifeService {

    @Autowired
    private CareRecordLifeDao careRecordLifeDao;

    public Page<CareRecordLife> list(int pageNum, int pageSize, Integer orderId, Integer customerId, Integer staffId, Integer infantId) {
        Page<CareRecordLife> page = new Page<>(pageNum, pageSize);
        QueryWrapper<CareRecordLife> wrapper = new QueryWrapper<>();
        if (orderId != null) {
            wrapper.eq("order_id", orderId);
        }
        if (customerId != null) {
            wrapper.eq("customer_id", customerId);
        }
        if (staffId != null) {
            wrapper.eq("staff_id", staffId);
        }
        if (infantId != null) {
            wrapper.eq("infant_id", infantId);
        }
        wrapper.orderByDesc("create_time");
        return careRecordLifeDao.selectPage(page, wrapper);
    }

    public CareRecordLife getById(Integer id) {
        return careRecordLifeDao.selectById(id);
    }

    public int save(CareRecordLife record) {
        return careRecordLifeDao.insert(record);
    }

    public int updateById(CareRecordLife record) {
        return careRecordLifeDao.updateById(record);
    }

    public int removeById(Integer id) {
        return careRecordLifeDao.deleteById(id);
    }
}
