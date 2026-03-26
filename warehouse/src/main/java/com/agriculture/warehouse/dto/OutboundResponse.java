package com.agriculture.warehouse.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OutboundResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Long locationId;
    private String locationCode;
    private LocalDateTime outboundTime;
    private String message;

    public OutboundResponse(Long id, Long productId, String productName,
                            Integer quantity, Long locationId, String locationCode,
                            LocalDateTime outboundTime, String message) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.locationId = locationId;
        this.locationCode = locationCode;
        this.outboundTime = outboundTime;
        this.message = message;
    }
}