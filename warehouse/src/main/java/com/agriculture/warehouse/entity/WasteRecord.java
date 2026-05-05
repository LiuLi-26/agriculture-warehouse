package com.agriculture.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "waste_record")
public class WasteRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    private Integer quantity;

    @Column(name = "waste_type")
    private String wasteType;  // EXPIRED, DAMAGED, OTHER

    @Column(name = "waste_date")
    private LocalDate wasteDate;

    private String reason;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}