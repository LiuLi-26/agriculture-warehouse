package com.agriculture.warehouse.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    String[] value();  // 允许访问的角色列表
}