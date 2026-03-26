package com.agriculture.warehouse.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StorageLocationResponse {
    private Long id;
    private String locationCode;
    private String zone;
    private Boolean isOccupied;
    private Long currentProductId;
    private Integer capacity;
    private Integer remainingCapacity;  // 剩余容量
    private LocalDateTime createTime;

    // 构造方法（包含所有字段）
    public StorageLocationResponse(Long id, String locationCode, String zone,
                                   Boolean isOccupied, Long currentProductId,
                                   Integer capacity, Integer remainingCapacity,
                                   LocalDateTime createTime) {
        this.id = id;
        this.locationCode = locationCode;
        this.zone = zone;
        this.isOccupied = isOccupied;
        this.currentProductId = currentProductId;
        this.capacity = capacity;
        this.remainingCapacity = remainingCapacity;
        this.createTime = createTime;
    }
}