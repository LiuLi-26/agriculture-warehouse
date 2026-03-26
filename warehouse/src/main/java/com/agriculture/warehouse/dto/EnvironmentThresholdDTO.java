package com.agriculture.warehouse.dto;

import lombok.Data;

@Data
public class EnvironmentThresholdDTO {
    private String zone;
    private Double tempMin;
    private Double tempMax;
    private Double humidityMin;
    private Double humidityMax;
}