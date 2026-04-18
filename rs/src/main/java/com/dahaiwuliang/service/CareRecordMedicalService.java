package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.dao.CareRecordMedicalDao;
import com.dahaiwuliang.entity.CareRecordMedical;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CareRecordMedicalService {

    @Autowired
    private CareRecordMedicalDao careRecordMedicalDao;

    public Page<CareRecordMedical> list(int pageNum, int pageSize, Integer orderId, Integer customerId, Integer staffId, Integer infantId) {
        Page<CareRecordMedical> page = new Page<>(pageNum, pageSize);
        QueryWrapper<CareRecordMedical> wrapper = new QueryWrapper<>();
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
        return careRecordMedicalDao.selectPage(page, wrapper);
    }

    public CareRecordMedical getById(Integer id) {
        return careRecordMedicalDao.selectById(id);
    }

    public int save(CareRecordMedical record) {
        return careRecordMedicalDao.insert(record);
    }

    public int updateById(CareRecordMedical record) {
        return careRecordMedicalDao.updateById(record);
    }

    public int removeById(Integer id) {
        return careRecordMedicalDao.deleteById(id);
    }
}
