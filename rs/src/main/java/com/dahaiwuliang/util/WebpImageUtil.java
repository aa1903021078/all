package com.dahaiwuliang.util;

import com.dahaiwuliang.common.BusinessException;
import com.dahaiwuliang.config.FoodieProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 图片上传工具: 将图片保存到后端本地项目目录, 返回可访问的相对 URL.
 *
 * 说明:
 *  1. 为保证跨平台 & 打包后在其他电脑均能正常显示, 这里不做 WebP/压缩转换,
 *     直接保存原图(避免依赖平台相关的 WebP 本地库), 前端仅保留"压缩"提示文案。
 *  2. 存储目录为相对路径 ./data/upload(位于后端项目目录内), 便于随项目一起拷贝、部署。
 *  3. 返回的 URL 会带上 context-path(如 /api), 从而在开发代理与生产环境下均可直接访问。
 */
@Slf4j
@Component
public class WebpImageUtil {

    private final FoodieProperties properties;

    /** 后端 context-path(如 /api), 用于拼出前端可直接访问的图片地址 */
    @Value("${server.servlet.context-path:}")
    private String contextPath;

    public WebpImageUtil(FoodieProperties properties) {
        this.properties = properties;
    }

    /**
     * 保存图片到本地上传目录, 返回可访问的相对 URL (如 /api/upload/20240718/xxx.jpg)
     */
    public String saveAsWebp(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件为空");
        }
        String ext = resolveExt(file);

        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        File dir = new File(properties.getUpload().getDir(), dateDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("创建上传目录失败");
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        File dest = new File(dir, fileName);
        try {
            Files.copy(file.getInputStream(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("图片保存失败", e);
            throw new BusinessException("图片保存失败");
        }
        return buildUrl(dateDir, fileName);
    }

    /** 从原始文件名/内容类型推断扩展名, 默认 jpg */
    private String resolveExt(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name != null && name.contains(".")) {
            String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
            if (ext.matches("jpg|jpeg|png|gif|webp|bmp")) {
                return "jpeg".equals(ext) ? "jpg" : ext;
            }
        }
        String type = file.getContentType();
        if (type != null && type.startsWith("image/")) {
            String ext = type.substring("image/".length()).toLowerCase();
            if ("jpeg".equals(ext)) {
                return "jpg";
            }
            if (ext.matches("png|gif|webp|bmp")) {
                return ext;
            }
        }
        return "jpg";
    }

    /** 拼接可访问 URL: context-path + urlPrefix + /日期/文件名 */
    private String buildUrl(String dateDir, String fileName) {
        String prefix = contextPath == null ? "" : contextPath.trim();
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix + properties.getUpload().getUrlPrefix() + "/" + dateDir + "/" + fileName;
    }
}
