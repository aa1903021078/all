package com.dahaiwuliang.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.url-prefix:/uploads/}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File dir = Paths.get(uploadDir).toFile();
        if (!dir.exists()) dir.mkdirs();
        String location = "file:" + dir.getAbsolutePath() + File.separator;
        String pattern = urlPrefix.endsWith("/") ? urlPrefix + "**" : urlPrefix + "/**";
        registry.addResourceHandler(pattern).addResourceLocations(location);
        // also serve packaged static (if any) under /static/
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }
}
