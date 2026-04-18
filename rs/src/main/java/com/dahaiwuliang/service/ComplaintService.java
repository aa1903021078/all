package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.dao.ComplaintDao;
import com.dahaiwuliang.entity.Complaint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintDao complaintDao;

    public Page<Complaint> list(int pageNum, int pageSize, Integer customerId, Integer status) {
        Page<Complaint> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Complaint> wrapper = new QueryWrapper<>();
        if (customerId != null) {
            wrapper.eq("customer_id", customerId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        return complaintDao.selectPage(page, wrapper);
    }

    public Complaint getById(Integer id) {
        return complaintDao.selectById(id);
    }

    public int save(Complaint complaint) {
        return complaintDao.insert(complaint);
    }

    public int updateById(Complaint complaint) {
        return complaintDao.updateById(complaint);
    }

    public int removeById(Integer id) {
        return complaintDao.deleteById(id);
    }
}
