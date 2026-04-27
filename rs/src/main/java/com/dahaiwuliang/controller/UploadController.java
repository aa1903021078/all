package com.dahaiwuliang.controller;

import com.dahaiwuliang.common.R;
import com.dahaiwuliang.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService storage;

    @PostMapping
    public R<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String url = storage.store(file);
        return R.ok(Collections.singletonMap("url", url));
    }
}
