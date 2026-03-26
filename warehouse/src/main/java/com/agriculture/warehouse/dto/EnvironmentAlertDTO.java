package com.agriculture.warehouse.dto;

import lombok.Data;

@Data
public class EnvironmentAlertDTO {
    private String zone;
    private String type;  // TEMPERATURE, HUMIDITY
    private Double currentValue;
    private Double minValue;
    private Double maxValue;
    private String message;
    private String level;  // WARNING, CRITICAL
}