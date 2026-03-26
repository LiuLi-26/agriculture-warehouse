package com.agriculture.warehouse.repository;

import com.agriculture.warehouse.entity.OutboundRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboundRepository extends JpaRepository<OutboundRecord, Long> {

    // 根据商品ID查询出库记录
    List<OutboundRecord> findByProductId(Long productId);

    // 根据货位ID查询出库记录
    List<OutboundRecord> findByLocationId(Long locationId);
}