package com.agriculture.warehouse.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
public class InboundResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Long locationId;
    private String locationCode;
    private String zone;
    private LocalDateTime inboundTime;
    private LocalDate expiryDate;
    private String message;

    public InboundResponse(Long id, Long productId, String productName,
                           Integer quantity, Long locationId, String locationCode,
                           String zone, LocalDateTime inboundTime,
                           LocalDate expiryDate, String message) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.locationId = locationId;
        this.locationCode = locationCode;
        this.zone = zone;
        this.inboundTime = inboundTime;
        this.expiryDate = expiryDate;
        this.message = message;
    }
}