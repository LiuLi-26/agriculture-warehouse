package com.agriculture.warehouse.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String username;
    private String email;
    private String phone;
}