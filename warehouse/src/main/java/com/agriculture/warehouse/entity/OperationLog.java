package com.agriculture.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "operation_log")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = true)  // 改为允许 null
    private Long userId;

    @Column(length = 50)
    private String username;

    @Column(name = "operation_type", length = 50)
    private String operationType;

    @Column(length = 50)
    private String module;

    @Column(length = 500)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "request_url", length = 200)
    private String requestUrl;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(length = 20)
    private String status;

    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}