package com.agriculture.warehouse.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LogQueryDTO {
    private Long userId;
    private String operationType;
    private String module;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer page = 1;
    private Integer size = 20;
}