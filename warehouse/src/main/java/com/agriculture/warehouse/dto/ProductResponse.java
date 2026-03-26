package com.agriculture.warehouse.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String category;
    private String storageCondition;
    private Integer shelfLifeDays;
    private Integer alertThreshold;
    private String description;
    private String unit;  // ← 添加这行
    private LocalDateTime createTime;

    // 修改构造方法，添加 unit 参数
    public ProductResponse(Long id, String name, String category,
                           String storageCondition, Integer shelfLifeDays,
                           Integer alertThreshold, String description,
                           String unit, LocalDateTime createTime) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.storageCondition = storageCondition;
        this.shelfLifeDays = shelfLifeDays;
        this.alertThreshold = alertThreshold;
        this.description = description;
        this.unit = unit;
        this.createTime = createTime;
    }
}