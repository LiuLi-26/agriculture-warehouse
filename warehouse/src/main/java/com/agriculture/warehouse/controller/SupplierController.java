package com.agriculture.warehouse.controller;

import com.agriculture.warehouse.annotation.Log;
import com.agriculture.warehouse.annotation.RequireRole;
import com.agriculture.warehouse.dto.SupplierRequest;
import com.agriculture.warehouse.dto.SupplierResponse;
import com.agriculture.warehouse.entity.Supplier;
import com.agriculture.warehouse.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/suppliers")
@Tag(name = "供应商管理", description = "供应商信息管理")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping
    @Operation(summary = "获取所有供应商")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "供应商管理", operation = "查询供应商列表")
    public List<SupplierResponse> getAllSuppliers() {
        return supplierService.getAllSuppliers().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取供应商")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "供应商管理", operation = "查询供应商详情")
    public SupplierResponse getSupplierById(@PathVariable Long id) {
        return convertToResponse(supplierService.getSupplierById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索供应商")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "供应商管理", operation = "搜索供应商")
    public List<SupplierResponse> searchSuppliers(@RequestParam(required = false) String name) {
        return supplierService.searchSuppliers(name).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @PostMapping
    @Operation(summary = "新增供应商")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "供应商管理", operation = "新增供应商")
    public SupplierResponse createSupplier(@RequestBody SupplierRequest request) {
        return convertToResponse(supplierService.createSupplier(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新供应商")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "供应商管理", operation = "更新供应商")
    public SupplierResponse updateSupplier(@PathVariable Long id, @RequestBody SupplierRequest request) {
        return convertToResponse(supplierService.updateSupplier(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除供应商")
    @RequireRole({"ADMIN"})
    @Log(module = "供应商管理", operation = "删除供应商")
    public String deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return "删除成功";
    }

    private SupplierResponse convertToResponse(Supplier supplier) {
        SupplierResponse response = new SupplierResponse();
        response.setId(supplier.getId());
        response.setName(supplier.getName());
        response.setContactPerson(supplier.getContactPerson());
        response.setPhone(supplier.getPhone());
        response.setEmail(supplier.getEmail());
        response.setAddress(supplier.getAddress());
        response.setCategory(supplier.getCategory());
        response.setRemark(supplier.getRemark());
        response.setCreateTime(supplier.getCreateTime());
        return response;
    }
}