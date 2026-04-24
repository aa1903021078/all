package com.dahaiwuliang.tongue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 舌诊接口:前端上传图片,后端调用百度千帆多模态大模型返回结构化舌诊结果。
 */
@RestController
@RequestMapping("/api/tongue")
public class TongueController {

    private static final Logger log = LoggerFactory.getLogger(TongueController.class);

    private final TongueAnalysisService service;

    public TongueController(TongueAnalysisService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public ResponseEntity<TongueReport> analyze(@RequestParam("file") MultipartFile file) {
        TongueReport report = service.analyze(file);
        return ResponseEntity.ok(report);
    }

    @ExceptionHandler(TongueAnalysisException.class)
    public ResponseEntity<Map<String, Object>> handleBiz(TongueAnalysisException e) {
        return ResponseEntity.status(e.getStatus()).body(error(e.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleSize(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error("上传图片过大"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException e) {
        log.warn("舌诊接口内部错误: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception e) {
        log.error("舌诊接口未知异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("服务内部错误"));
    }

    private static Map<String, Object> error(String message) {
        Map<String, Object> m = new HashMap<>();
        m.put("error", message == null ? "未知错误" : message);
        return m;
    }
}
