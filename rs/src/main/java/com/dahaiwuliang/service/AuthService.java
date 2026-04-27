package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dahaiwuliang.common.BizException;
import com.dahaiwuliang.entity.SysUser;
import com.dahaiwuliang.mapper.SysUserMapper;
import com.dahaiwuliang.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public Map<String, Object> login(String username, String password) {
        SysUser u = userMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username));
        if (u == null) throw new BizException(401, "用户不存在");
        if (u.getEnabled() != null && u.getEnabled() == 0) throw new BizException(401, "账号已禁用");
        if (!encoder.matches(password, u.getPassword())) throw new BizException(401, "密码错误");
        String token = jwt.generate(u.getId(), u.getUsername(), u.getRole());
        Map<String, Object> r = new HashMap<>();
        r.put("token", token);
        r.put("user", safe(u));
        return r;
    }

    public SysUser register(String username, String password, String realName,
                            String phone, String idCard) {
        if (username == null || username.isEmpty()) throw new BizException(400, "用户名必填");
        if (password == null || password.length() < 6) throw new BizException(400, "密码至少 6 位");
        Long exists = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username));
        if (exists != null && exists > 0) throw new BizException(400, "用户名已存在");
        SysUser u = new SysUser();
        u.setUsername(username);
        u.setPassword(encoder.encode(password));
        u.setRealName(realName);
        u.setPhone(phone);
        u.setIdCard(idCard);
        u.setRole("PATIENT");
        u.setEnabled(1);
        userMapper.insert(u);
        return safe(u);
    }

    public SysUser me(Long id) {
        SysUser u = userMapper.selectById(id);
        if (u == null) throw new BizException(404, "用户不存在");
        return safe(u);
    }

    public SysUser safe(SysUser u) { u.setPassword(null); return u; }
}
