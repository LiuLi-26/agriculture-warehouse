package com.agriculture.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "environment_monitor")
public class EnvironmentMonitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String zone;

    private Double temperature;

    private Double humidity;

    private String status;  // NORMAL, WARNING, CRITICAL

    @Column(name = "create_time")
    private LocalDateTime createTime;
}