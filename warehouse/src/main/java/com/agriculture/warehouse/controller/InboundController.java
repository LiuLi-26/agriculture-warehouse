package com.agriculture.warehouse.controller;

import com.agriculture.warehouse.annotation.Log;
import com.agriculture.warehouse.annotation.RequireRole;
import com.agriculture.warehouse.dto.InboundRequest;
import com.agriculture.warehouse.dto.InboundResponse;
import com.agriculture.warehouse.entity.InboundRecord;
import com.agriculture.warehouse.entity.Product;
import com.agriculture.warehouse.entity.StorageLocation;
import com.agriculture.warehouse.entity.Supplier;
import com.agriculture.warehouse.repository.ProductRepository;
import com.agriculture.warehouse.repository.StorageLocationRepository;
import com.agriculture.warehouse.repository.SupplierRepository;
import com.agriculture.warehouse.service.InboundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inbound")
@Tag(name = "入库管理", description = "农产品入库及智能货位分配")
public class InboundController {

    @Autowired
    private InboundService inboundService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StorageLocationRepository locationRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    /**
     * 执行入库
     */
    @PostMapping
    @Operation(summary = "执行入库")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "入库管理", operation = "执行入库")
    public InboundResponse inbound(@RequestBody InboundRequest request) {
        Long operatorId = 1L;
        return inboundService.executeInbound(request, operatorId);
    }

    /**
     * 获取所有入库记录
     */
    @GetMapping
    @Operation(summary = "获取所有入库记录")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "入库管理", operation = "查询入库记录")
    public List<InboundResponse> getAllInboundRecords() {
        List<InboundRecord> records = inboundService.getAllInboundRecords();

        return records.stream().map(record -> {
            Product product = productRepository.findById(record.getProductId()).orElse(null);
            StorageLocation location = locationRepository.findById(record.getLocationId()).orElse(null);
            Supplier supplier = record.getSupplierId() != null ?
                    supplierRepository.findById(record.getSupplierId()).orElse(null) : null;

            return new InboundResponse(
                    record.getId(),
                    record.getProductId(),
                    product != null ? product.getName() : "未知",
                    record.getQuantity(),
                    record.getLocationId(),
                    location != null ? location.getLocationCode() : "未知",
                    location != null ? location.getZone() : "未知",
                    record.getInboundTime(),
                    record.getExpiryDate(),
                    record.getSupplierId(),
                    supplier != null ? supplier.getName() : null,
                    null
            );
        }).collect(Collectors.toList());
    }

    /**
     * 根据商品ID查询入库记录
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "根据商品查询入库记录")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "入库管理", operation = "按商品查询入库记录")
    public List<InboundRecord> getInboundRecordsByProduct(@PathVariable Long productId) {
        return inboundService.getInboundRecordsByProduct(productId);
    }

    /**
     * 入库统计报表
     */
    @GetMapping("/statistics")
    @Operation(summary = "入库统计报表")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "入库管理", operation = "查询入库统计")
    public Map<String, Object> getInboundStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<InboundRecord> records = inboundService.getAllInboundRecords();

        // 日期过滤
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
            records = records.stream()
                    .filter(r -> r.getInboundTime().isAfter(start) && r.getInboundTime().isBefore(end))
                    .collect(Collectors.toList());
        }

        // 按日期统计入库数量
        Map<String, Integer> dailyStats = new LinkedHashMap<>();
        // 按商品统计入库数量
        Map<String, Integer> productStats = new LinkedHashMap<>();
        // 按供应商统计入库数量
        Map<String, Integer> supplierStats = new LinkedHashMap<>();

        for (InboundRecord record : records) {
            String date = record.getInboundTime().toLocalDate().toString();
            dailyStats.put(date, dailyStats.getOrDefault(date, 0) + record.getQuantity());

            Product product = productRepository.findById(record.getProductId()).orElse(null);
            String productName = product != null ? product.getName() : "未知";
            productStats.put(productName, productStats.getOrDefault(productName, 0) + record.getQuantity());

            if (record.getSupplierId() != null) {
                Supplier supplier = supplierRepository.findById(record.getSupplierId()).orElse(null);
                String supplierName = supplier != null ? supplier.getName() : "未知";
                supplierStats.put(supplierName, supplierStats.getOrDefault(supplierName, 0) + record.getQuantity());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dailyStats", dailyStats);
        result.put("productStats", productStats);
        result.put("supplierStats", supplierStats);
        result.put("totalQuantity", records.stream().mapToInt(InboundRecord::getQuantity).sum());
        result.put("totalCount", records.size());

        return result;
    }
}