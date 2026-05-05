package com.agriculture.warehouse.controller;

import com.agriculture.warehouse.annotation.Log;
import com.agriculture.warehouse.annotation.RequireRole;
import com.agriculture.warehouse.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/system/config")
@Tag(name = "系统配置", description = "系统参数配置")
public class SystemConfigController {

    @Autowired
    private SystemConfigService configService;

    @GetMapping
    @Operation(summary = "获取所有系统配置")
    @RequireRole({"ADMIN"})
    @Log(module = "系统配置", operation = "查询系统配置")
    public Map<String, String> getAllConfigs() {
        return configService.getAllConfigs();
    }

    @GetMapping("/{key}")
    @Operation(summary = "获取单个配置")
    @RequireRole({"ADMIN"})
    @Log(module = "系统配置", operation = "查询单个配置")
    public String getConfig(@PathVariable String key) {
        return configService.getConfig(key);
    }

    @PutMapping("/{key}")
    @Operation(summary = "更新单个配置")
    @RequireRole({"ADMIN"})
    @Log(module = "系统配置", operation = "更新系统配置")
    public void updateConfig(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("value");
        configService.updateConfig(key, value, null);
    }

    @PutMapping("/batch")
    @Operation(summary = "批量更新配置")
    @RequireRole({"ADMIN"})
    @Log(module = "系统配置", operation = "批量更新配置")
    public void batchUpdateConfigs(@RequestBody Map<String, String> configs) {
        configService.batchUpdateConfigs(configs);
    }

    @PostMapping("/init")
    @Operation(summary = "初始化默认配置")
    @RequireRole({"ADMIN"})
    @Log(module = "系统配置", operation = "初始化系统配置")
    public String initConfigs() {
        configService.initDefaultConfigs();
        return "初始化成功";
    }
}