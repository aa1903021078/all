package com.yuequge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 阅趣阁后端启动类。
 */
@SpringBootApplication
@MapperScan("com.yuequge.mapper")
@org.springframework.scheduling.annotation.EnableScheduling
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
