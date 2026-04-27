package com.dahaiwuliang.aop;

import com.alibaba.fastjson2.JSON;
import com.dahaiwuliang.entity.OperationLog;
import com.dahaiwuliang.mapper.OperationLogMapper;
import com.dahaiwuliang.security.CurrentUser;
import com.dahaiwuliang.security.JwtUtil.TokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper logMapper;

    @Around("@annotation(com.dahaiwuliang.aop.LogOp)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        LogOp ann = method.getAnnotation(LogOp.class);

        OperationLog l = new OperationLog();
        l.setAction(ann.action());
        l.setTarget(ann.target());
        TokenInfo t = CurrentUser.get();
        if (t != null) { l.setUserId(t.getUserId()); l.setUsername(t.getUsername()); }
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr != null) {
                HttpServletRequest req = attr.getRequest();
                String ip = req.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty()) ip = req.getRemoteAddr();
                l.setIp(ip);
            }
        } catch (Exception ignored) { }

        try {
            Object[] args = Arrays.stream(pjp.getArgs())
                    .filter(a -> a != null
                            && !(a instanceof MultipartFile)
                            && !(a instanceof javax.servlet.ServletRequest)
                            && !(a instanceof javax.servlet.ServletResponse))
                    .toArray();
            String params = JSON.toJSONString(args);
            if (params.length() > 1500) params = params.substring(0, 1500) + "...";
            l.setParams(params);
        } catch (Exception ignored) { l.setParams(""); }

        try {
            Object res = pjp.proceed();
            l.setSuccess(1);
            l.setDurationMs(System.currentTimeMillis() - start);
            l.setCreatedAt(LocalDateTime.now());
            safeSave(l);
            return res;
        } catch (Throwable ex) {
            l.setSuccess(0);
            l.setErrorMsg(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
            l.setDurationMs(System.currentTimeMillis() - start);
            l.setCreatedAt(LocalDateTime.now());
            safeSave(l);
            throw ex;
        }
    }

    private void safeSave(OperationLog l) {
        try { logMapper.insert(l); }
        catch (Exception e) { log.warn("save operation log failed: {}", e.getMessage()); }
    }
}
