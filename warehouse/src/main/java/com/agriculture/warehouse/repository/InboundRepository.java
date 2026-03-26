package com.agriculture.warehouse.repository;

import com.agriculture.warehouse.entity.InboundRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InboundRepository extends JpaRepository<InboundRecord, Long> {

    // 根据商品ID查询入库记录（按过期时间排序，先进先出用）
    List<InboundRecord> findByProductIdOrderByExpiryDateAsc(Long productId);

    // 查询某个货位的所有入库记录
    List<InboundRecord> findByLocationId(Long locationId);
}