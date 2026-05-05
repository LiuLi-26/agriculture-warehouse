package com.agriculture.warehouse.controller;

import com.agriculture.warehouse.annotation.Log;
import com.agriculture.warehouse.annotation.RequireRole;
import com.agriculture.warehouse.dto.InventoryCheckRequest;
import com.agriculture.warehouse.dto.InventoryCheckResponse;
import com.agriculture.warehouse.entity.InventoryCheck;
import com.agriculture.warehouse.repository.ProductRepository;
import com.agriculture.warehouse.repository.StorageLocationRepository;
import com.agriculture.warehouse.service.InventoryCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "库存盘点", description = "库存盘点管理")
public class InventoryCheckController {

    @Autowired
    private InventoryCheckService inventoryCheckService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StorageLocationRepository locationRepository;

    @GetMapping("/checks")
    @Operation(summary = "获取所有盘点记录")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "库存盘点", operation = "查询盘点记录")
    public List<InventoryCheckResponse> getAllChecks() {
        return inventoryCheckService.getAllChecksWithInfo();
    }

    @PostMapping("/checks")
    @Operation(summary = "创建盘点记录")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "库存盘点", operation = "创建盘点记录")
    public InventoryCheckResponse createCheck(@RequestBody InventoryCheckRequest request) {
        Long operatorId = 1L;
        InventoryCheck check = inventoryCheckService.createInventoryCheck(request, operatorId);

        InventoryCheckResponse response = new InventoryCheckResponse();
        response.setId(check.getId());
        response.setCheckNo(check.getCheckNo());
        response.setCheckDate(check.getCheckDate());
        response.setProductId(check.getProductId());

        var product = productRepository.findById(check.getProductId()).orElse(null);
        response.setProductName(product != null ? product.getName() : "未知");

        response.setLocationId(check.getLocationId());
        var location = locationRepository.findById(check.getLocationId()).orElse(null);
        response.setLocationCode(location != null ? location.getLocationCode() : "未知");

        response.setSystemQuantity(check.getSystemQuantity());
        response.setActualQuantity(check.getActualQuantity());
        response.setDifference(check.getDifference());
        response.setStatus(check.getStatus());
        response.setRemark(check.getRemark());
        response.setCreateTime(check.getCreateTime());

        return response;
    }

    @PutMapping("/checks/{id}/confirm")
    @Operation(summary = "确认盘点结果")
    @RequireRole({"ADMIN"})
    @Log(module = "库存盘点", operation = "确认盘点结果")
    public String confirmCheck(@PathVariable Long id) {
        inventoryCheckService.confirmCheck(id);
        return "确认成功";
    }

    @GetMapping("/checks/statistics")
    @Operation(summary = "盘点统计")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "库存盘点", operation = "查询盘点统计")
    public Map<String, Object> getCheckStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return inventoryCheckService.getCheckStatistics(startDate, endDate);
    }
}