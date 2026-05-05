package com.agriculture.warehouse.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
public class WasteResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private String wasteType;
    private String wasteTypeName;
    private LocalDate wasteDate;
    private String reason;
    private LocalDateTime createTime;
}