package com.example.mapper;

import com.example.entity.User;

import java.util.List;
import java.util.Map;

/**
 * 操作user相关数据接口
 */
public interface UserMapper {

    /**
     * 新增
     */
    void insert(User user);

    /**
     * 删除
     */
    void deleteById(Integer id);

    /**
     * 修改
     */
    void updateById(User user);

    /**
     * 根据ID查询
     */
    User selectById(Integer id);

    /**
     * 查询所有
     */
    List<User> selectAll(User user);

    /**
     * 根据用户名查询
     */
    User selectByUsername(String username);

    /**
     * 根据手机号查询
     */
    User selectByPhone(String phone);

    /**
     * 根据账号查询（登录用）
     */
    User selectByAccount(String account);

    List<Map<String, Object>> selectPointsRanking(Integer limit);
}