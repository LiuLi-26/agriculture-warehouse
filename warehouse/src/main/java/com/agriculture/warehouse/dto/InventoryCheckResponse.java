package com.agriculture.warehouse.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
public class InventoryCheckResponse {
    private Long id;
    private String checkNo;
    private LocalDate checkDate;
    private Long productId;
    private String productName;
    private Long locationId;
    private String locationCode;
    private Integer systemQuantity;
    private Integer actualQuantity;
    private Integer difference;
    private String status;
    private String statusName;
    private String remark;
    private LocalDateTime createTime;
}