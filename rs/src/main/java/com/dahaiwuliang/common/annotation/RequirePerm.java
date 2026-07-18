package com.dahaiwuliang.common.annotation;

import java.lang.annotation.*;

/**
 * 需要指定权限之一 (如 content:review)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePerm {
    String[] value();
}
