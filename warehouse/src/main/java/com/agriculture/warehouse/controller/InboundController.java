package com.agriculture.warehouse.controller;

import com.agriculture.warehouse.annotation.Log;
import com.agriculture.warehouse.annotation.RequireRole;
import com.agriculture.warehouse.dto.InboundRequest;
import com.agriculture.warehouse.dto.InboundResponse;
import com.agriculture.warehouse.entity.InboundRecord;
import com.agriculture.warehouse.service.InboundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inbound")
@Tag(name = "入库管理", description = "农产品入库及智能货位分配")
public class InboundController {

    @Autowired
    private InboundService inboundService;

    @PostMapping
    @Operation(summary = "执行入库")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "入库管理", operation = "执行入库")
    public InboundResponse inbound(@RequestBody InboundRequest request) {
        Long operatorId = 1L;
        return inboundService.executeInbound(request, operatorId);
    }

    @GetMapping
    @Operation(summary = "获取所有入库记录")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "入库管理", operation = "查询入库记录")
    public List<InboundRecord> getAllInboundRecords() {
        return inboundService.getAllInboundRecords();
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "根据商品查询入库记录")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "入库管理", operation = "按商品查询入库记录")
    public List<InboundRecord> getInboundRecordsByProduct(@PathVariable Long productId) {
        return inboundService.getInboundRecordsByProduct(productId);
    }
}