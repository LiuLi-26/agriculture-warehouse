package com.agriculture.warehouse.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class InventoryCheckRequest {
    private Long productId;
    private Long locationId;
    private Integer systemQuantity;
    private Integer actualQuantity;
    private String remark;
}