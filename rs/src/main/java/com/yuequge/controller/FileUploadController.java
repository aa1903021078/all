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

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "category", defaultValue = "common") String category) {
        if (file == null || file.isEmpty()) {
            throw new BizException("文件为空");
        }
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) ext = original.substring(dot);

        // 清理可能的非法字符
        category = category.replaceAll("[^a-zA-Z0-9_-]", "");
        if (category.isBlank()) category = "common";

        String datePath = LocalDate.now().toString(); // yyyy-MM-dd
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        File dir = new File(uploadProperties.getPath(), category + "/" + datePath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BizException("创建目录失败");
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
