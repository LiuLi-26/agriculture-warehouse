package com.agriculture.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "storage_location")
public class StorageLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_code", nullable = false, unique = true, length = 20)
    private String locationCode;  // 货位编号，如 A-01-01

    @Column(length = 20)
    private String zone;  // 区域：A区/B区/冷藏区/冷冻区

    @Column(name = "is_occupied")
    private Boolean isOccupied = false;  // 是否被占用

    @Column(name = "current_product_id")
    private Long currentProductId;  // 当前存放的商品ID，为空表示空闲

    private Integer capacity;  // 容量/承重（kg）

    @Column(name = "create_time")
    private LocalDateTime createTime;
}