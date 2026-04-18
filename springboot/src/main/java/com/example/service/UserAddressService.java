package com.example.service;

import com.example.entity.UserAddress;
import com.example.mapper.UserAddressMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户地址业务处理
 */
@Service
public class UserAddressService {

    @Resource
    private UserAddressMapper userAddressMapper;

    /**
     * 新增地址
     */
    public void add(UserAddress userAddress) {
        // 如果是第一个地址，设为默认
        List<UserAddress> existing = userAddressMapper.selectByUser(userAddress.getUserId());
        if (existing.isEmpty()) {
            userAddress.setIsDefault(1);
        } else {
            userAddress.setIsDefault(0);
        }
        userAddressMapper.insert(userAddress);
    }

    /**
     * 删除地址
     */
    public void deleteById(Integer id) {
        userAddressMapper.deleteById(id);
    }

    /**
     * 修改地址
     */
    public void updateById(UserAddress address) {
        userAddressMapper.updateById(address);
    }

    /**
     * 设置默认地址
     */
    public void setDefault(Integer id, Integer userId) {
        // 先将该用户所有地址设为非默认
        userAddressMapper.clearDefaultByUser(userId);
        // 再设置指定地址为默认
        UserAddress userAddress = new UserAddress();
        userAddress.setId(id);
        userAddress.setIsDefault(1);
        userAddressMapper.updateById(userAddress);
    }

    /**
     * 根据ID查询地址
     */
    public UserAddress selectById(Integer id) {
        return userAddressMapper.selectById(id);
    }

    /**
     * 查询用户的所有地址
     */
    public List<UserAddress> selectByUser(Integer userId) {
        return userAddressMapper.selectByUser(userId);
    }

    /**
     * 查询用户的默认地址
     */
    public UserAddress selectDefault(Integer userId) {
        return userAddressMapper.selectDefault(userId);
    }
}