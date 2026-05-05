package com.agriculture.warehouse.controller;

import com.agriculture.warehouse.annotation.RequireRole;
import com.agriculture.warehouse.dto.LogQueryDTO;
import com.agriculture.warehouse.dto.OperationLogDTO;
import com.agriculture.warehouse.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "操作日志", description = "操作日志查询和管理")
public class LogController {

    @Autowired
    private OperationLogService logService;
    @PostMapping("/query")
    @Operation(summary = "查询操作日志")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    public Page<OperationLogDTO> queryLogs(@RequestBody LogQueryDTO queryDTO,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                           @RequestHeader(value = "X-Role", required = false) String role) {
        if (!"ADMIN".equals(role) && queryDTO.getUserId() == null) {
            queryDTO.setUserId(userId);
        }
        return logService.queryLogs(queryDTO);
    }

    /**
     * 获取日志统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取日志统计")
    @RequireRole({"ADMIN"})
    public OperationLogService.LogStatistics getStatistics() {
        return logService.getStatistics();
    }

    /**
     * 获取当前用户的操作日志
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的操作日志")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    public Page<OperationLogDTO> getMyLogs(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size,
                                           @RequestHeader("X-User-Id") Long userId) {
        return logService.getLogsByUserId(userId, page, size);
    }
}