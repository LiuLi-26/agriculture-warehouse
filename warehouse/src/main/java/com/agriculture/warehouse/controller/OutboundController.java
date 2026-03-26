package com.agriculture.warehouse.controller;

import com.agriculture.warehouse.annotation.Log;
import com.agriculture.warehouse.annotation.RequireRole;
import com.agriculture.warehouse.dto.OutboundRequest;
import com.agriculture.warehouse.dto.OutboundResponse;
import com.agriculture.warehouse.entity.OutboundRecord;
import com.agriculture.warehouse.service.OutboundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/outbound")
@Tag(name = "出库管理", description = "农产品出库及先进先出管理")
public class OutboundController {

    @Autowired
    private OutboundService outboundService;

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
}