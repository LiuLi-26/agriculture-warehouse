package com.agriculture.warehouse.controller;

import com.agriculture.warehouse.annotation.Log;
import com.agriculture.warehouse.annotation.RequireRole;
import com.agriculture.warehouse.dto.CustomerResponse;
import com.agriculture.warehouse.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "客户管理", description = "客户信息管理")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    @Operation(summary = "获取所有客户")
    @RequireRole({"ADMIN"})
    @Log(module = "客户管理", operation = "查询客户列表")
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取客户详情")
    @RequireRole({"ADMIN"})
    @Log(module = "客户管理", operation = "查询客户详情")
    public CustomerResponse getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索客户")
    @RequireRole({"ADMIN"})
    @Log(module = "客户管理", operation = "搜索客户")
    public List<CustomerResponse> searchCustomers(@RequestParam String keyword) {
        return customerService.searchCustomers(keyword);
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用客户")
    @RequireRole({"ADMIN"})
    @Log(module = "客户管理", operation = "禁用客户")
    public String disableCustomer(@PathVariable Long id) {
        customerService.disableCustomer(id);
        return "禁用成功";
    }

    @PutMapping("/{id}/enable")
    @Operation(summary = "启用客户")
    @RequireRole({"ADMIN"})
    @Log(module = "客户管理", operation = "启用客户")
    public String enableCustomer(@PathVariable Long id) {
        customerService.enableCustomer(id);
        return "启用成功";
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除客户")
    @RequireRole({"ADMIN"})
    @Log(module = "客户管理", operation = "删除客户")
    public String deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return "删除成功";
    }

    @GetMapping("/statistics")
    @Operation(summary = "客户统计")
    @RequireRole({"ADMIN"})
    @Log(module = "客户管理", operation = "查询客户统计")
    public CustomerService.CustomerStatistics getStatistics() {
        return customerService.getStatistics();
    }
}