package com.example.mapper;

import com.example.entity.UserAddress;

import java.util.List;

public interface UserAddressMapper {


    void insert(UserAddress userAddress);

    void deleteById(Integer id);

    void updateById(UserAddress address);

    UserAddress selectById(Integer id);

    List<UserAddress> selectByUser(Integer userId);

    UserAddress selectDefault(Integer userId);

    void clearDefaultByUser(Integer userId);
}
