package com.agriculture.warehouse.dto;

import lombok.Data;

@Data
public class StorageLocationRequest {
    private String locationCode;  // 货位编号
    private String zone;          // 区域
    private Integer capacity;     // 容量
}