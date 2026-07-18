package com.dahaiwuliang.controller;

import com.dahaiwuliang.common.R;
import com.dahaiwuliang.common.annotation.RequireLogin;
import com.dahaiwuliang.util.WebpImageUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传(图片自动压缩转 WebP)
 * 注: 静态访问路径为 /upload/**, 这里的上传接口放在 /files 避免与静态资源冲突
 */
@RestController
@RequestMapping("/files")
@RequireLogin
public class UploadController {

    private final WebpImageUtil webpImageUtil;

    public UploadController(WebpImageUtil webpImageUtil) {
        this.webpImageUtil = webpImageUtil;
    }

    @PostMapping("/image")
    public R<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return R.ok(webpImageUtil.saveAsWebp(file));
    }

    @PostMapping("/images")
    public R<List<String>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(webpImageUtil.saveAsWebp(file));
        }
        return R.ok(urls);
    }
}
