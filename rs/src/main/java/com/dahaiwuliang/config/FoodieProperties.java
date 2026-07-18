package com.dahaiwuliang.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 平台自定义配置 (对应 application.yml 中 foodie.*)
 */
@Data
@Component
@ConfigurationProperties(prefix = "foodie")
public class FoodieProperties {

    private Jwt jwt = new Jwt();
    private Upload upload = new Upload();
    private Amap amap = new Amap();

    @Data
    public static class Jwt {
        private String secret;
        /** 有效期(秒) */
        private long expire = 604800;
        private String header = "Authorization";
    }

    @Data
    public static class Upload {
        private String dir = "./data/upload";
        private String urlPrefix = "/upload";
        private int maxSide = 1600;
        private double webpQuality = 0.8;
    }

    @Data
    public static class Amap {
        private String key = "";
    }
}
