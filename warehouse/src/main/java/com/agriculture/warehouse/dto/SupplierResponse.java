package com.agriculture.warehouse.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SupplierResponse {
    private Long id;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String category;
    private String remark;
    private LocalDateTime createTime;
}