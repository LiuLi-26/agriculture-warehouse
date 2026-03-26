package com.agriculture.warehouse.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OperationLogDTO {
    private Long id;
    private Long userId;
    private String username;
    private String operationType;
    private String module;
    private String description;
    private String detail;
    private String ipAddress;
    private String requestUrl;
    private String requestMethod;
    private String status;
    private String errorMsg;
    private Long durationMs;
    private LocalDateTime createTime;
}