package com.agriculture.warehouse.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnvironmentDataDTO {
    private Long id;
    private String zone;
    private Double temperature;
    private Double humidity;
    private String status;
    private LocalDateTime createTime;
}