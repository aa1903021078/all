package com.dahaiwuliang.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dahaiwuliang.common.BusinessException;
import com.dahaiwuliang.common.LoginUser;
import com.dahaiwuliang.common.UserContext;
import com.dahaiwuliang.dto.LoginDTO;
import com.dahaiwuliang.dto.RegisterDTO;
import com.dahaiwuliang.entity.SysRole;
import com.dahaiwuliang.entity.SysUser;
import com.dahaiwuliang.entity.SysUserRole;
import com.dahaiwuliang.mapper.SysPermissionMapper;
import com.dahaiwuliang.mapper.SysRoleMapper;
import com.dahaiwuliang.mapper.SysUserMapper;
import com.dahaiwuliang.mapper.SysUserRoleMapper;
import com.dahaiwuliang.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证 & RBAC
 */
@Service
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final JwtUtil jwtUtil;

    private static final String DEFAULT_AVATAR = "https://picsum.photos/seed/newuser/100/100";

    public AuthService(SysUserMapper userMapper, SysRoleMapper roleMapper,
                       SysPermissionMapper permissionMapper, SysUserRoleMapper userRoleMapper,
                       JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BusinessException("账号已被封禁, 请联系管理员");
        }
        LoginUser loginUser = buildLoginUser(user);
        String token = jwtUtil.generateToken(loginUser);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", loginUser);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        user.setNickname(StrUtil.isBlank(dto.getNickname()) ? dto.getUsername() : dto.getNickname());
        user.setAvatar(DEFAULT_AVATAR);
        user.setGender(0);
        user.setStatus(0);
        userMapper.insert(user);

        SysRole userRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCode, "USER"));
        if (userRole != null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(userRole.getId());
            userRoleMapper.insert(ur);
        }
    }

    /** 根据用户构建登录信息(含角色/权限) */
    public LoginUser buildLoginUser(SysUser user) {
        List<String> roles = roleMapper.listRoleCodesByUserId(user.getId());
        List<String> perms = permissionMapper.listPermCodesByUserId(user.getId());
        return new LoginUser(user.getId(), user.getUsername(), user.getNickname(),
                user.getAvatar(), roles, perms);
    }

    /** 当前登录用户(从库刷新最新信息) */
    public LoginUser currentUser() {
        Long userId = UserContext.requireUserId();
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "登录已失效");
        }
        return buildLoginUser(user);
    }
}
