package com.agriculture.warehouse.controller;

import com.agriculture.warehouse.annotation.Log;
import com.agriculture.warehouse.annotation.RequireRole;
import com.agriculture.warehouse.dto.EnvironmentAlertDTO;
import com.agriculture.warehouse.dto.EnvironmentDataDTO;
import com.agriculture.warehouse.dto.EnvironmentThresholdDTO;
import com.agriculture.warehouse.entity.EnvironmentThreshold;
import com.agriculture.warehouse.service.EnvironmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/environment")
@Tag(name = "环境监测", description = "仓库环境温湿度监测及预警")
public class EnvironmentController {

    @Autowired
    private EnvironmentService environmentService;

    @GetMapping("/latest")
    @Operation(summary = "获取最新环境数据")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "环境监测", operation = "查询环境数据")
    public List<EnvironmentDataDTO> getLatestData() {
        return environmentService.getLatestData();
    }

    @GetMapping("/alerts")
    @Operation(summary = "获取环境预警")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "环境监测", operation = "查询环境预警")
    public List<EnvironmentAlertDTO> getAlerts() {
        return environmentService.getAlerts();
    }

    @GetMapping("/history/{zone}")
    @Operation(summary = "获取历史数据")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "环境监测", operation = "查询历史数据")
    public List<EnvironmentDataDTO> getHistoryData(@PathVariable String zone,
                                                   @RequestParam(defaultValue = "24") int hours) {
        return environmentService.getHistoryData(zone, hours);
    }

    @GetMapping("/thresholds")
    @Operation(summary = "获取阈值配置")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "环境监测", operation = "查询阈值配置")
    public List<EnvironmentThreshold> getThresholds() {
        return environmentService.getAllThresholds();
    }

    @PutMapping("/thresholds/{zone}")
    @Operation(summary = "更新阈值配置")
    @RequireRole({"ADMIN"})
    @Log(module = "环境监测", operation = "更新环境阈值")
    public EnvironmentThreshold updateThreshold(@PathVariable String zone,
                                                @RequestBody EnvironmentThresholdDTO dto) {
        dto.setZone(zone);
        return environmentService.updateThreshold(dto);
    }

    @PostMapping("/simulate")
    @Operation(summary = "模拟传感器数据")
    @RequireRole({"ADMIN"})
    @Log(module = "环境监测", operation = "模拟传感器数据")
    public String simulate() {
        environmentService.simulateSensorData();
        return "模拟数据已生成";
    }
}