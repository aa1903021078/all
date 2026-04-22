package com.yuequge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 登录页测试用户配置（仅展示用）。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "yuequge")
public class TestUsersProperties {
    private List<TestUser> testUsers;

    @Data
    public static class TestUser {
        private String username;
        private String password;
        private String role;
        private String desc;
    }
}
