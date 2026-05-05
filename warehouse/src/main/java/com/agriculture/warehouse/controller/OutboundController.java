package com.agriculture.warehouse.controller;

import com.agriculture.warehouse.annotation.Log;
import com.agriculture.warehouse.annotation.RequireRole;
import com.agriculture.warehouse.dto.OutboundRequest;
import com.agriculture.warehouse.dto.OutboundResponse;
import com.agriculture.warehouse.entity.OutboundRecord;
import com.agriculture.warehouse.entity.Product;
import com.agriculture.warehouse.repository.ProductRepository;
import com.agriculture.warehouse.service.OutboundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/outbound")
@Tag(name = "出库管理", description = "农产品出库及先进先出管理")
public class OutboundController {

    @Autowired
    private OutboundService outboundService;

    @Autowired
    private ProductRepository productRepository;

    @PostMapping
    @Operation(summary = "执行出库")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "出库管理", operation = "执行出库")
    public OutboundResponse outbound(@RequestBody OutboundRequest request) {
        Long operatorId = 1L;
        return outboundService.executeOutbound(request, operatorId);
    }

    @GetMapping
    @Operation(summary = "获取所有出库记录")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "出库管理", operation = "查询出库记录")
    public List<OutboundRecord> getAllOutboundRecords() {
        return outboundService.getAllOutboundRecords();
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "根据商品查询出库记录")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "出库管理", operation = "按商品查询出库记录")
    public List<OutboundRecord> getOutboundRecordsByProduct(@PathVariable Long productId) {
        return outboundService.getOutboundRecordsByProduct(productId);
    }

    /**
     * 出库统计报表
     */
    @GetMapping("/statistics")
    @Operation(summary = "出库统计报表")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "出库管理", operation = "查询出库统计")
    public Map<String, Object> getOutboundStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<OutboundRecord> records = outboundService.getAllOutboundRecords();

        // 日期过滤
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
            records = records.stream()
                    .filter(r -> r.getOutboundTime().isAfter(start) && r.getOutboundTime().isBefore(end))
                    .collect(Collectors.toList());
        }

        // 按日期统计出库数量
        Map<String, Integer> dailyStats = new LinkedHashMap<>();
        // 按商品统计出库数量
        Map<String, Integer> productStats = new LinkedHashMap<>();

        for (OutboundRecord record : records) {
            String date = record.getOutboundTime().toLocalDate().toString();
            dailyStats.put(date, dailyStats.getOrDefault(date, 0) + record.getQuantity());

            Product product = productRepository.findById(record.getProductId()).orElse(null);
            String productName = product != null ? product.getName() : "未知";
            productStats.put(productName, productStats.getOrDefault(productName, 0) + record.getQuantity());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dailyStats", dailyStats);
        result.put("productStats", productStats);
        result.put("totalQuantity", records.stream().mapToInt(OutboundRecord::getQuantity).sum());
        result.put("totalCount", records.size());

        return result;
    }
}