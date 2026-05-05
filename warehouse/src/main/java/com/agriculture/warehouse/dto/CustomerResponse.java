package com.agriculture.warehouse.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CustomerResponse {
    private Long id;
    private String username;
    private String role;
    private String status;
    private String phone;
    private String email;
    private String address;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;
    private Integer orderCount;
    private Double totalAmount;
}