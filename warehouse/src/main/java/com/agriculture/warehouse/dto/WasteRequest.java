package com.agriculture.warehouse.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class WasteRequest {
    private Long productId;
    private Integer quantity;
    private String wasteType;
    private LocalDate wasteDate;
    private String reason;
}