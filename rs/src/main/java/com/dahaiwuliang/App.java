package com.dahaiwuliang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 美食探店 & 菜谱分享平台 启动类
 */
@SpringBootApplication
@MapperScan("com.dahaiwuliang.mapper")
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}
