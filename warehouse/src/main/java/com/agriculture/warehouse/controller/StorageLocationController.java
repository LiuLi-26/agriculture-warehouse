package com.agriculture.warehouse.controller;

import com.agriculture.warehouse.annotation.Log;
import com.agriculture.warehouse.annotation.RequireRole;
import com.agriculture.warehouse.dto.StorageLocationRequest;
import com.agriculture.warehouse.dto.StorageLocationResponse;
import com.agriculture.warehouse.entity.StorageLocation;
import com.agriculture.warehouse.service.StorageLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@Tag(name = "货位管理", description = "仓库货位的增删改查接口")
public class StorageLocationController {

    @Autowired
    private StorageLocationService locationService;

    /**
     * 获取所有货位（带剩余容量）
     */
    @GetMapping
    @Operation(summary = "获取所有货位")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "货位管理", operation = "查询货位列表")
    public List<StorageLocationResponse> getAllLocations() {
        // 直接调用 Service 的方法，Service 已经返回带剩余容量的 Response
        return locationService.getAllLocationsWithRemaining();
    }

    /**
     * 根据ID获取货位
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取货位")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "货位管理", operation = "查询货位详情")
    public StorageLocationResponse getLocationById(@PathVariable Long id) {
        // 这里需要单独处理，因为要计算剩余容量
        return locationService.getLocationByIdWithRemaining(id);
    }

    /**
     * 获取所有空闲货位
     */
    @GetMapping("/free")
    @Operation(summary = "获取空闲货位")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "货位管理", operation = "查询空闲货位")
    public List<StorageLocationResponse> getFreeLocations() {
        return locationService.getFreeLocationsWithRemaining();
    }

    /**
     * 根据区域获取空闲货位
     */
    @GetMapping("/free/{zone}")
    @Operation(summary = "根据区域获取空闲货位")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "货位管理", operation = "按区域查询空闲货位")
    public List<StorageLocationResponse> getFreeLocationsByZone(@PathVariable String zone) {
        return locationService.getFreeLocationsByZoneWithRemaining(zone);
    }

    /**
     * 根据区域获取所有货位
     */
    @GetMapping("/zone/{zone}")
    @Operation(summary = "根据区域获取货位")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "货位管理", operation = "按区域查询货位")
    public List<StorageLocationResponse> getLocationsByZone(@PathVariable String zone) {
        return locationService.getLocationsByZoneWithRemaining(zone);
    }

    /**
     * 新增货位
     */
    @PostMapping
    @Operation(summary = "新增货位")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "货位管理", operation = "新增货位")
    public StorageLocationResponse createLocation(@RequestBody StorageLocationRequest request) {
        StorageLocation location = convertToEntity(request);
        StorageLocation saved = locationService.createLocation(location);
        return locationService.convertToResponse(saved);  // 使用 Service 的转换方法
    }

    /**
     * 更新货位信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新货位")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "货位管理", operation = "更新货位")
    public StorageLocationResponse updateLocation(@PathVariable Long id,
                                                  @RequestBody StorageLocationRequest request) {
        StorageLocation location = convertToEntity(request);
        StorageLocation updated = locationService.updateLocation(id, location);
        return locationService.convertToResponse(updated);
    }

    /**
     * 删除货位
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除货位")
    @RequireRole({"ADMIN"})
    @Log(module = "货位管理", operation = "删除货位")
    public String deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return "删除成功";
    }

    // 转换方法：Request -> Entity
    private StorageLocation convertToEntity(StorageLocationRequest request) {
        StorageLocation location = new StorageLocation();
        location.setLocationCode(request.getLocationCode());
        location.setZone(request.getZone());
        location.setCapacity(request.getCapacity());
        return location;
    }
}