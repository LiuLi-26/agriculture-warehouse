package com.agriculture.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "outbound_record")
public class OutboundRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    private Integer quantity;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "outbound_time")
    private LocalDateTime outboundTime;

    @Column(name = "operator_id")
    private Long operatorId;
}