package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.dao.ServiceItemDao;
import com.dahaiwuliang.entity.ServiceItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceItemService {

    @Autowired
    private ServiceItemDao serviceItemDao;

    public Page<ServiceItem> list(int pageNum, int pageSize, Integer status, String name) {
        Page<ServiceItem> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ServiceItem> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        wrapper.orderByDesc("create_time");
        return serviceItemDao.selectPage(page, wrapper);
    }

    public ServiceItem getById(Integer id) {
        return serviceItemDao.selectById(id);
    }

    public int save(ServiceItem serviceItem) {
        return serviceItemDao.insert(serviceItem);
    }

    public int updateById(ServiceItem serviceItem) {
        return serviceItemDao.updateById(serviceItem);
    }

    public int removeById(Integer id) {
        return serviceItemDao.deleteById(id);
    }
}
