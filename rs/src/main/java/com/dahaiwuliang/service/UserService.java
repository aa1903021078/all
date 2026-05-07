package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dahaiwuliang.entity.User;

public interface UserService extends IService<User> {
    User wxLogin(String code);
    User adminLogin(String username, String password);
}
