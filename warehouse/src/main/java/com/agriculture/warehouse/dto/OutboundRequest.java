package com.agriculture.warehouse.dto;

import lombok.Data;

@Data
public class OutboundRequest {
    private Long productId;      // 商品ID
    private Integer quantity;    // 出库数量
}