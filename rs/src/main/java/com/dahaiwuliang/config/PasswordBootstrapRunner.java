package com.dahaiwuliang.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dahaiwuliang.entity.SysUser;
import com.dahaiwuliang.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seed-data passwords are stored as plain text. On startup we re-hash any non-BCrypt password
 * with BCryptPasswordEncoder, so login (which always compares against BCrypt) works against
 * the bootstrap accounts.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class PasswordBootstrapRunner implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        List<SysUser> users = userMapper.selectList(Wrappers.emptyWrapper());
        for (SysUser u : users) {
            String p = u.getPassword();
            if (p == null) continue;
            if (!(p.startsWith("$2a$") || p.startsWith("$2b$") || p.startsWith("$2y$"))) {
                u.setPassword(encoder.encode(p));
                userMapper.updateById(u);
            }
        }
    }
}
