package com.agriculture.warehouse.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class InboundRequest {
    private Long productId;      // 商品ID
    private Integer quantity;    // 入库数量
    private LocalDate expiryDate; // 过期日期
}