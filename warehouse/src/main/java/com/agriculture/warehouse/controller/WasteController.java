package com.agriculture.warehouse.controller;

import com.agriculture.warehouse.annotation.Log;
import com.agriculture.warehouse.annotation.RequireRole;
import com.agriculture.warehouse.dto.WasteRequest;
import com.agriculture.warehouse.dto.WasteResponse;
import com.agriculture.warehouse.entity.Product;
import com.agriculture.warehouse.entity.WasteRecord;
import com.agriculture.warehouse.repository.ProductRepository;
import com.agriculture.warehouse.service.WasteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/waste")
@Tag(name = "损耗管理", description = "农产品损耗记录和统计")
public class WasteController {

    @Autowired
    private WasteService wasteService;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    @Operation(summary = "获取所有损耗记录")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "损耗管理", operation = "查询损耗记录")
    public List<WasteResponse> getAllWasteRecords() {
        List<WasteRecord> records = wasteService.getAllWasteRecords();

        return records.stream().map(record -> {
            Product product = productRepository.findById(record.getProductId()).orElse(null);
            WasteResponse response = new WasteResponse();
            response.setId(record.getId());
            response.setProductId(record.getProductId());
            response.setProductName(product != null ? product.getName() : "未知");
            response.setQuantity(record.getQuantity());
            response.setWasteType(record.getWasteType());
            response.setWasteTypeName(getWasteTypeName(record.getWasteType()));
            response.setWasteDate(record.getWasteDate());
            response.setReason(record.getReason());
            response.setCreateTime(record.getCreateTime());
            return response;
        }).collect(Collectors.toList());
    }

    @PostMapping
    @Operation(summary = "新增损耗记录")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "损耗管理", operation = "新增损耗记录")
    public WasteResponse createWasteRecord(@RequestBody WasteRequest request) {
        Long operatorId = 1L;
        WasteRecord record = wasteService.createWasteRecord(request, operatorId);

        Product product = productRepository.findById(record.getProductId()).orElse(null);
        WasteResponse response = new WasteResponse();
        response.setId(record.getId());
        response.setProductId(record.getProductId());
        response.setProductName(product != null ? product.getName() : "未知");
        response.setQuantity(record.getQuantity());
        response.setWasteType(record.getWasteType());
        response.setWasteTypeName(getWasteTypeName(record.getWasteType()));
        response.setWasteDate(record.getWasteDate());
        response.setReason(record.getReason());
        response.setCreateTime(record.getCreateTime());
        return response;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除损耗记录")
    @RequireRole({"ADMIN"})
    @Log(module = "损耗管理", operation = "删除损耗记录")
    public String deleteWasteRecord(@PathVariable Long id) {
        wasteService.deleteWasteRecord(id);
        return "删除成功";
    }

    @GetMapping("/statistics")
    @Operation(summary = "损耗统计报表")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "损耗管理", operation = "查询损耗统计")
    public Map<String, Object> getWasteStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return wasteService.getWasteStatistics(startDate, endDate);
    }

    private String getWasteTypeName(String type) {
        switch (type) {
            case "EXPIRED": return "过期损耗";
            case "DAMAGED": return "损坏损耗";
            case "OTHER": return "其他损耗";
            default: return type;
        }
    }
}