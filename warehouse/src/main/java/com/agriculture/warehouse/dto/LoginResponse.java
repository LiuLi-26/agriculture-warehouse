package com.agriculture.warehouse.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private Long id;
    private String username;
    private String role;
    private String message;

    // 带参数的构造方法
    public LoginResponse(Long id, String username, String role, String message) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.message = message;
    }
}
