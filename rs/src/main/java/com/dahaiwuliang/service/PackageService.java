package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.dao.PackageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PackageService {

    @Autowired
    private PackageDao packageDao;

    public Page<com.dahaiwuliang.entity.Package> list(int pageNum, int pageSize, Integer status, String name) {
        Page<com.dahaiwuliang.entity.Package> page = new Page<>(pageNum, pageSize);
        QueryWrapper<com.dahaiwuliang.entity.Package> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        wrapper.orderByDesc("create_time");
        return packageDao.selectPage(page, wrapper);
    }

    public com.dahaiwuliang.entity.Package getById(Integer id) {
        return packageDao.selectById(id);
    }

    public int save(com.dahaiwuliang.entity.Package pkg) {
        return packageDao.insert(pkg);
    }

    public int updateById(com.dahaiwuliang.entity.Package pkg) {
        return packageDao.updateById(pkg);
    }

    public int removeById(Integer id) {
        return packageDao.deleteById(id);
    }
}
