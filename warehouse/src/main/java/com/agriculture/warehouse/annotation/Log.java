package com.agriculture.warehouse.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    String module() default "";      // 操作模块
    String operation() default "";   // 操作描述
    boolean saveDetail() default true; // 是否保存详情
}