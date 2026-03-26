package com.agriculture.warehouse.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("农产品仓储管理系统 API")
                        .version("1.0")
                        .description("智能农产品仓储管理系统接口文档")
                        .contact(new Contact()
                                .name("焦政赫")
                                .email("your-email@example.com")));
    }
}