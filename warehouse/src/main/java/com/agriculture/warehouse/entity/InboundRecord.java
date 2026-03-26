package com.agriculture.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "inbound_record")
public class InboundRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    private Integer quantity;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "inbound_time")
    private LocalDateTime inboundTime;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;  // 该批次过期日期
}
