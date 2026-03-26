package com.agriculture.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "environment_threshold")
public class EnvironmentThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String zone;

    @Column(name = "temp_min")
    private Double tempMin;

    @Column(name = "temp_max")
    private Double tempMax;

    @Column(name = "humidity_min")
    private Double humidityMin;

    @Column(name = "humidity_max")
    private Double humidityMax;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}