package com.agriculture.warehouse.repository;

import com.agriculture.warehouse.entity.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {

    // 根据货位编号查询
    Optional<StorageLocation> findByLocationCode(String locationCode);

    // 查询所有空闲货位
    List<StorageLocation> findByIsOccupiedFalse();

    // 根据区域查询空闲货位
    List<StorageLocation> findByZoneAndIsOccupiedFalse(String zone);

    // 根据区域查询所有货位
    List<StorageLocation> findByZone(String zone);

    // 根据当前存放的商品ID查询
    List<StorageLocation> findByCurrentProductId(Long productId);
}
