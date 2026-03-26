package com.agriculture.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String category;

    @Column(name = "storage_condition", length = 50)
    private String storageCondition;

    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    private String unit;

    @Column(name = "alert_threshold")
    private Integer alertThreshold;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}