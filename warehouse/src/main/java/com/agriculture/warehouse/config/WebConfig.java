package com.agriculture.warehouse.config;

import com.agriculture.warehouse.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",           // 登录接口放行
                        "/swagger-ui/**",             // Swagger 放行
                        "/v3/api-docs/**",            // API 文档放行
                        "/swagger-resources/**",      // Swagger 资源放行
                        "/webjars/**"                 // WebJars 放行
                );
    }
}