package com.agriculture.warehouse.dto;

import lombok.Data;

@Data
public class SupplierRequest {
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String category;
    private String remark;
}