package com.yuequge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传配置。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {
    private String path;
    private String urlPrefix = "/static";
}
