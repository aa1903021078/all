package com.dahaiwuliang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dahaiwuliang.entity.User;
import com.dahaiwuliang.mapper.UserMapper;
import com.dahaiwuliang.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.UUID;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User wxLogin(String code) {
        // 模拟微信登录，使用code作为openid
        String openid = "wx_" + code;
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        if (user == null) {
            // 新用户，自动注册
            user = new User();
            user.setOpenid(openid);
            user.setNickname("微信用户" + UUID.randomUUID().toString().substring(0, 6));
            user.setAvatar("/uploads/default_avatar.png");
            user.setCreditScore(100);
            user.setStatus(0);
            user.setRole(0);
            this.save(user);
        }
        return user;
    }

    @Override
    public User adminLogin(String username, String password) {
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        return this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getStudentId, username)
                .eq(User::getPassword, md5Password)
                .eq(User::getRole, 1));
    }
}
