package com.agriculture.warehouse.dto;

import lombok.Data;

@Data
public class ProductRequest {
    private String name;
    private String category;
    private String storageCondition;
    private Integer shelfLifeDays;
    private Integer alertThreshold;
    private String description;
    private String unit;
}