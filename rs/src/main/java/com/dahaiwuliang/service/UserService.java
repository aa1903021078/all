package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.dao.UserDao;
import com.dahaiwuliang.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    public Page<User> list(int pageNum, int pageSize, Integer role, String username) {
        Page<User> page = new Page<>(pageNum, pageSize);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (role != null) {
            wrapper.eq("role", role);
        }
        if (username != null && !username.isEmpty()) {
            wrapper.like("username", username);
        }
        wrapper.orderByDesc("create_time");
        return userDao.selectPage(page, wrapper);
    }

    public User getById(Integer id) {
        return userDao.selectById(id);
    }

    public int save(User user) {
        return userDao.insert(user);
    }

    public int updateById(User user) {
        return userDao.updateById(user);
    }

    public int removeById(Integer id) {
        return userDao.deleteById(id);
    }

    public User login(String username, String password) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        wrapper.eq("password", password);
        return userDao.selectOne(wrapper);
    }

    public List<User> findByRole(Integer role) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role", role);
        return userDao.selectList(wrapper);
    }

    public List<User> findStaff() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.in("role", Arrays.asList(1, 2, 3));
        return userDao.selectList(wrapper);
    }

    public User findByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return userDao.selectOne(wrapper);
    }
}
