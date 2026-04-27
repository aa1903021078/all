package com.dahaiwuliang.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogOp {
    String action();
    String target() default "";
}
