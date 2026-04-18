package com.dahaiwuliang.controller;

import com.dahaiwuliang.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${upload.path}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(dir.getAbsolutePath() + File.separator + newFileName));
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }

        String url = "/upload/" + newFileName;
        return Result.ok(url);
    }
}
