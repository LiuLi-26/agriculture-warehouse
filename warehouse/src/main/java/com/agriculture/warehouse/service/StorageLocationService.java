package com.agriculture.warehouse.service;

import com.agriculture.warehouse.dto.StorageLocationResponse;
import com.agriculture.warehouse.entity.InboundRecord;
import com.agriculture.warehouse.entity.StorageLocation;
import com.agriculture.warehouse.repository.InboundRepository;
import com.agriculture.warehouse.repository.StorageLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StorageLocationService {

    @Autowired
    private StorageLocationRepository storageLocationRepository;

    @Autowired
    private InboundRepository inboundRepository;

    // ========== 剩余容量计算方法 ==========

    /**
     * 计算货位当前占用容量
     */
    private Integer calculateCurrentQuantity(Long locationId) {
        // 从入库记录中统计该货位的总数量
        List<InboundRecord> records = inboundRepository.findByLocationId(locationId);
        return records.stream()
                .mapToInt(InboundRecord::getQuantity)
                .sum();
    }

    /**
     * 计算剩余容量
     */
    private Integer calculateRemainingCapacity(StorageLocation location) {
        if (location.getCapacity() == null) {
            return null;
        }
        Integer currentQuantity = calculateCurrentQuantity(location.getId());
        return Math.max(0, location.getCapacity() - currentQuantity);
    }

    /**
     * 转换实体到响应DTO（公开方法，供 Controller 调用）
     */
    public StorageLocationResponse convertToResponse(StorageLocation location) {
        Integer remainingCapacity = calculateRemainingCapacity(location);

        return new StorageLocationResponse(
                location.getId(),
                location.getLocationCode(),
                location.getZone(),
                location.getIsOccupied(),
                location.getCurrentProductId(),
                location.getCapacity(),
                remainingCapacity,
                location.getCreateTime()
        );
    }

    // ========== 带剩余容量的查询方法 ==========

    /**
     * 获取所有货位（带剩余容量）
     */
    public List<StorageLocationResponse> getAllLocationsWithRemaining() {
        return storageLocationRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取货位（带剩余容量）
     */
    public StorageLocationResponse getLocationByIdWithRemaining(Long id) {
        StorageLocation location = storageLocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("货位不存在，ID: " + id));
        return convertToResponse(location);
    }

    /**
     * 获取所有空闲货位（带剩余容量）
     */
    public List<StorageLocationResponse> getFreeLocationsWithRemaining() {
        return storageLocationRepository.findByIsOccupiedFalse().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据区域获取空闲货位（带剩余容量）
     */
    public List<StorageLocationResponse> getFreeLocationsByZoneWithRemaining(String zone) {
        return storageLocationRepository.findByZoneAndIsOccupiedFalse(zone).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据区域获取所有货位（带剩余容量）
     */
    public List<StorageLocationResponse> getLocationsByZoneWithRemaining(String zone) {
        return storageLocationRepository.findByZone(zone).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ========== 原有方法（返回实体） ==========

    /**
     * 获取所有货位
     */
    public List<StorageLocation> getAllLocations() {
        return storageLocationRepository.findAll();
    }

    /**
     * 根据ID获取货位
     */
    public Optional<StorageLocation> getLocationById(Long id) {
        return storageLocationRepository.findById(id);
    }

    /**
     * 根据货位编号获取货位
     */
    public Optional<StorageLocation> getLocationByCode(String locationCode) {
        return storageLocationRepository.findByLocationCode(locationCode);
    }

    /**
     * 获取所有空闲货位
     */
    public List<StorageLocation> getFreeLocations() {
        return storageLocationRepository.findByIsOccupiedFalse();
    }

    /**
     * 根据区域获取空闲货位
     */
    public List<StorageLocation> getFreeLocationsByZone(String zone) {
        return storageLocationRepository.findByZoneAndIsOccupiedFalse(zone);
    }

    /**
     * 根据区域获取所有货位
     */
    public List<StorageLocation> getLocationsByZone(String zone) {
        return storageLocationRepository.findByZone(zone);
    }

    // ========== 货位操作 ==========

    /**
     * 新增货位
     */
    public StorageLocation createLocation(StorageLocation location) {
        location.setCreateTime(LocalDateTime.now());
        location.setIsOccupied(false);
        location.setCurrentProductId(null);
        return storageLocationRepository.save(location);
    }

    /**
     * 更新货位信息
     */
    public StorageLocation updateLocation(Long id, StorageLocation locationDetails) {
        StorageLocation location = storageLocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("货位不存在，ID: " + id));

        location.setLocationCode(locationDetails.getLocationCode());
        location.setZone(locationDetails.getZone());
        location.setCapacity(locationDetails.getCapacity());
        // 注意：isOccupied 和 currentProductId 不应该通过更新接口修改，应该由入库/出库操作来管理

        return storageLocationRepository.save(location);
    }

    /**
     * 删除货位（只能删除空闲货位）
     */
    public void deleteLocation(Long id) {
        StorageLocation location = storageLocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("货位不存在，ID: " + id));

        if (location.getIsOccupied()) {
            throw new RuntimeException("货位已被占用，无法删除");
        }

        storageLocationRepository.deleteById(id);
    }

    /**
     * 占用货位（入库时调用）
     */
    public StorageLocation occupyLocation(Long locationId, Long productId) {
        StorageLocation location = storageLocationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("货位不存在，ID: " + locationId));

        if (location.getIsOccupied()) {
            throw new RuntimeException("货位已被占用");
        }

        location.setIsOccupied(true);
        location.setCurrentProductId(productId);
        return storageLocationRepository.save(location);
    }

    /**
     * 释放货位（出库时调用）
     */
    public StorageLocation releaseLocation(Long locationId) {
        StorageLocation location = storageLocationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("货位不存在，ID: " + locationId));

        location.setIsOccupied(false);
        location.setCurrentProductId(null);
        return storageLocationRepository.save(location);
    }
}