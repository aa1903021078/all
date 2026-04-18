package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户业务处理
 */
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;


    /**
     * 用户注册
     * @param user
     */
    public void register(User user) {
        String username = user.getUsername();
        //校验账户是否已经存在
        User dbUser = userMapper.selectByUsername(username);
        if (dbUser != null) {
            throw new CustomException("用户已经存在，请重新注册！");
        }
        // 校验手机号是否已存在
        String phone = user.getPhone();
        User dbUserByPhone = userMapper.selectByPhone(phone);
        if (dbUserByPhone != null) {
            throw new CustomException("手机号已被注册");
        }
        //设置默认值
        if(StrUtil.isBlank(user.getPassword())){
            //默认密码
            user.setPassword("123");
        }
        if(StrUtil.isBlank(user.getName())){
            //默认姓名
            user.setName(user.getUsername());
        }
        //默认用户角色
        user.setRole("普通用户");
        //默认环保积分为0
        user.setPoints(0);
        //默认总回收次数为0
        user.setTotalRecycleCount(0);
        //默认状态为使用
        user.setStatus(1);
        //默认账户余额为0
        user.setAccount(user.getUsername());
        //用userMapper接口去插入数据
        userMapper.insert(user);
    }

    /**
     * 新增（管理员添加）
     */
    public void add(User user) {
        String username = user.getUsername();
        //校验账户是否已经存在
        User dbUser = userMapper.selectByUsername(username);
        if (dbUser != null) {
            throw new CustomException("用户已经存在");
        }
        //设置默认值
        if(StrUtil.isBlank(user.getPassword())){
            //默认密码
            user.setPassword("123");
        }
        if(StrUtil.isBlank(user.getName())){
            //默认姓名
            user.setName(user.getUsername());
        }
        //默认用户角色
        user.setRole("普通用户");
        //默认环保积分为0
        user.setPoints(0);
        //默认总回收次数为0
        user.setTotalRecycleCount(0);
        //默认状态为使用
        user.setStatus(1);
        userMapper.insert(user);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }

    /**
     * 更新用户数据
     */
    public void updateById(User user) {
        // 如果修改了用户名，需要校验是否与其他用户冲突
        if (StrUtil.isNotBlank(user.getUsername())) {
            User existUser = userMapper.selectByUsername(user.getUsername());
            if (existUser != null && !existUser.getId().equals(user.getId())) {
                throw new CustomException("用户名已存在");
            }
        }
        //user对象里面必须包含ID，否则无法更新数据
        userMapper.updateById(user);
    }

    /**
     * 通过Id查询信息
     */
    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<User> selectAll(User user) {
        return userMapper.selectAll(user);
    }

    /**
     * 分页查询的方法
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<User> selectPage(User user,Integer pageNum, Integer pageSize,String name){
        PageHelper.startPage(pageNum,pageSize);
        List<User> list = userMapper.selectAll(user);
        return PageInfo.of(list);
    }

    /**
     * 根据用户名查询
     */
    public User selectByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    /**
     * 用户登录
     */
    public Account login(Account account) {
        /**  查询账号判断账号密码是否正确  */
        User  dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        if (dbUser.getStatus() != 1) {
            throw new CustomException("账号已被禁用");
        }
        return dbUser;
    }

    /**
     * 修改密码
     * @param account
     */
    public void updatePassword(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException("原密码错误");
        }
        dbUser.setPassword(account.getNewPassword());
        userMapper.updateById(dbUser);
    }

    /**
     * 获取用户个人信息
     */
    public User getUserProfile(Integer id) {
        User user = userMapper.selectById(id);
        if (ObjectUtil.isNull(user)) {
            throw new CustomException("用户不存在");
        }
        return user;
    }

    /**
     * 更新用户积分
     */
    public void updatePoints(Integer userId, Integer points) {
        User user = userMapper.selectById(userId);
        if (ObjectUtil.isNull(user)) {
            throw new CustomException("用户不存在");
        }
        user.setPoints(user.getPoints() + points);
        userMapper.updateById(user);
    }
}
