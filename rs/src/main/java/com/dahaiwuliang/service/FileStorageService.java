package com.dahaiwuliang.service;

import com.dahaiwuliang.common.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "pdf"));

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.url-prefix:/uploads/}")
    private String urlPrefix;

    @PostConstruct
    public void init() {
        File f = new File(uploadDir);
        if (!f.exists()) f.mkdirs();
        log.info("File storage dir: {}", f.getAbsolutePath());
    }

    public String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new BizException(400, "请选择文件");
        if (file.getSize() > 10L * 1024 * 1024) throw new BizException(400, "文件过大（最大 10MB）");
        String original = file.getOriginalFilename();
        if (original == null) original = "file";
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) ext = original.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXT.contains(ext)) throw new BizException(400, "不支持的文件类型");

        String today = LocalDate.now().toString();
        Path dir = Paths.get(uploadDir, today).toAbsolutePath().normalize();
        Path baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        if (!dir.startsWith(baseDir)) throw new BizException(400, "非法路径");

        String name = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path dest = dir.resolve(name).normalize();
        if (!dest.startsWith(baseDir)) throw new BizException(400, "非法路径");
        file.transferTo(dest.toFile());
        String prefix = urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
        return prefix + today + "/" + name;
    }
}
