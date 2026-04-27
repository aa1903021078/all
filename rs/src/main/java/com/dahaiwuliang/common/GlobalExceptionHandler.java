package com.dahaiwuliang.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public R<?> biz(BizException e) { return R.fail(e.getCode(), e.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<?> valid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst().map(f -> f.getField() + ": " + f.getDefaultMessage()).orElse("参数错误");
        return R.fail(400, msg);
    }

    @ExceptionHandler(BindException.class)
    public R<?> bind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst().map(f -> f.getField() + ": " + f.getDefaultMessage()).orElse("参数错误");
        return R.fail(400, msg);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<R<?>> denied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(R.fail(403, "没有访问权限"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<R<?>> unauth(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(R.fail(401, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public R<?> all(Exception e) {
        e.printStackTrace();
        return R.fail(500, e.getMessage() == null ? "服务器内部错误" : e.getMessage());
    }
}
