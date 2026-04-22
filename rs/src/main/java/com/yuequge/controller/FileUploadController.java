package com.yuequge.controller;

import com.yuequge.common.Result;
import com.yuequge.config.UploadProperties;
import com.yuequge.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传：保存到配置的静态目录，返回可访问 URL。
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final UploadProperties uploadProperties;

    /** 允许的上传类别白名单（防止路径注入）。 */
    private static final Set<String> ALLOWED_CATEGORIES =
            Set.of("avatar", "book", "song", "item", "announcement", "common");

    /** 允许的扩展名白名单（小写，带点）。 */
    private static final Set<String> ALLOWED_EXT =
            Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp",
                    ".mp3", ".wav", ".ogg", ".m4a",
                    ".pdf", ".txt");

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "category", defaultValue = "common") String category) {
        if (file == null || file.isEmpty()) {
            throw new BizException("文件为空");
        }
        // 严格校验类别：必须来自固定白名单，不允许拼接用户路径
        if (!ALLOWED_CATEGORIES.contains(category)) {
            category = "common";
        }

        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) ext = original.substring(dot).toLowerCase();
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BizException("不支持的文件类型：" + ext);
        }

        // 日期目录：服务器端生成，不含用户输入
        String datePath = LocalDate.now().toString(); // yyyy-MM-dd
        // 文件名：UUID + 白名单扩展，完全由服务器决定
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        // 基目录 + 白名单子目录，路径完全可控
        File baseDir = new File(uploadProperties.getPath()).getAbsoluteFile();
        File dir = new File(new File(baseDir, category), datePath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BizException("创建目录失败");
        }
        // 双保险：确保最终目录没有逃出 baseDir（防符号链接等）
        try {
            if (!dir.getCanonicalPath().startsWith(baseDir.getCanonicalPath() + File.separator)
                    && !dir.getCanonicalPath().equals(baseDir.getCanonicalPath())) {
                throw new BizException("非法上传路径");
            }
        } catch (IOException e) {
            throw new BizException("路径校验失败");
        }

        File dest = new File(dir, filename);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("upload failed", e);
            throw new BizException("上传失败: " + e.getMessage());
        }
        String url = uploadProperties.getUrlPrefix() + "/" + category + "/" + datePath + "/" + filename;
        Map<String, String> m = new HashMap<>();
        m.put("url", url);
        m.put("name", original);
        return Result.ok(m);
    }
}
