package com.dahaiwuliang.common.annotation;

import java.lang.annotation.*;

/**
 * 需要指定角色之一 (ADMIN/REVIEWER/MERCHANT/USER)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    String[] value();
}
