package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.dao.StaffChangeRequestDao;
import com.dahaiwuliang.entity.StaffChangeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StaffChangeRequestService {

    @Autowired
    private StaffChangeRequestDao staffChangeRequestDao;

    public Page<StaffChangeRequest> list(int pageNum, int pageSize, Integer customerId, Integer status) {
        Page<StaffChangeRequest> page = new Page<>(pageNum, pageSize);
        QueryWrapper<StaffChangeRequest> wrapper = new QueryWrapper<>();
        if (customerId != null) {
            wrapper.eq("customer_id", customerId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        return staffChangeRequestDao.selectPage(page, wrapper);
    }

    public StaffChangeRequest getById(Integer id) {
        return staffChangeRequestDao.selectById(id);
    }

    public int save(StaffChangeRequest request) {
        return staffChangeRequestDao.insert(request);
    }

    public int updateById(StaffChangeRequest request) {
        return staffChangeRequestDao.updateById(request);
    }

    public int removeById(Integer id) {
        return staffChangeRequestDao.deleteById(id);
    }
}
