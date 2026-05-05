package com.agriculture.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "system_config")
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String configKey;

    @Column(nullable = false)
    private String configValue;

    private String description;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}