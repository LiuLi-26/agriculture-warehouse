package com.agriculture.warehouse.service;

import com.agriculture.warehouse.dto.OutboundRequest;
import com.agriculture.warehouse.dto.OutboundResponse;
import com.agriculture.warehouse.entity.*;
import com.agriculture.warehouse.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import com.agriculture.warehouse.exception.BusinessException;

@Service
public class OutboundService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StorageLocationRepository locationRepository;

    @Autowired
    private InboundRepository inboundRepository;

    @Autowired
    private OutboundRepository outboundRepository;

    /**
     * 出库库存信息（用于跟踪批次扣减）
     */
    private static class StockBatch {
        InboundRecord record;
        StorageLocation location;
        int remainingQuantity;  // 该批次剩余可出库数量
    }

    /**
     * 执行出库（先进先出）
     * @param request 出库请求
     * @param operatorId 操作员ID
     * @return 出库响应
     */
    @Transactional
    public OutboundResponse executeOutbound(OutboundRequest request, Long operatorId) {
        // 1. 验证商品是否存在
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(404, "商品不存在，ID: " + request.getProductId()));

        // 2. 查询该商品的所有入库记录（按过期时间升序，先进先出）
        List<InboundRecord> inboundRecords = inboundRepository
                .findByProductIdOrderByExpiryDateAsc(request.getProductId());

        if (inboundRecords.isEmpty()) {
            throw new BusinessException(400, "商品库存不足，无入库记录");
        }

        // 3. 计算总库存
        int totalStock = inboundRecords.stream()
                .mapToInt(InboundRecord::getQuantity)
                .sum();

        if (request.getQuantity() > totalStock) {
            throw new BusinessException(400, "库存不足，当前库存: " + totalStock +
                    "，出库数量: " + request.getQuantity());
        }

        // 4. 准备批次扣减
        List<StockBatch> batches = new ArrayList<>();
        for (InboundRecord record : inboundRecords) {
            // 获取货位信息
            StorageLocation location = locationRepository.findById(record.getLocationId())
                    .orElseThrow(() -> new BusinessException(500, "货位不存在，ID: " + record.getLocationId()));

            StockBatch batch = new StockBatch();
            batch.record = record;
            batch.location = location;
            batch.remainingQuantity = record.getQuantity();
            batches.add(batch);
        }

        // 5. 执行扣减，记录出库详情
        int remainingOutbound = request.getQuantity();
        List<OutboundDetail> outboundDetails = new ArrayList<>();

        for (StockBatch batch : batches) {
            if (remainingOutbound <= 0) break;

            int takeQuantity = Math.min(batch.remainingQuantity, remainingOutbound);

            // 记录出库详情
            outboundDetails.add(new OutboundDetail(
                    batch.record.getId(),
                    batch.location.getId(),
                    batch.location.getLocationCode(),
                    takeQuantity
            ));

            remainingOutbound -= takeQuantity;
        }

        // 6. 更新数据库：删除或更新入库记录
        for (OutboundDetail detail : outboundDetails) {
            InboundRecord record = inboundRepository.findById(detail.recordId)
                    .orElseThrow(() -> new BusinessException(500, "入库记录不存在"));

            int newQuantity = record.getQuantity() - detail.quantity;

            if (newQuantity <= 0) {
                // 批次全部出库，删除记录
                inboundRepository.delete(record);

                // 释放货位
                StorageLocation location = locationRepository.findById(record.getLocationId())
                        .orElseThrow(() -> new BusinessException(500, "货位不存在"));
                location.setIsOccupied(false);
                location.setCurrentProductId(null);
                locationRepository.save(location);
            } else {
                // 部分出库，更新数量
                record.setQuantity(newQuantity);
                inboundRepository.save(record);
            }
        }

        // 7. 创建出库记录
        OutboundRecord outboundRecord = new OutboundRecord();
        outboundRecord.setProductId(product.getId());
        outboundRecord.setQuantity(request.getQuantity());
        outboundRecord.setLocationId(outboundDetails.get(0).locationId); // 主货位取第一个
        outboundRecord.setOutboundTime(LocalDateTime.now());
        outboundRecord.setOperatorId(operatorId);

        OutboundRecord saved = outboundRepository.save(outboundRecord);

        // 8. 构建响应
        String locationInfo = outboundDetails.stream()
                .map(d -> d.locationCode + "(" + d.quantity + ")")
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        return new OutboundResponse(
                saved.getId(),
                product.getId(),
                product.getName(),
                saved.getQuantity(),
                outboundDetails.get(0).locationId,
                outboundDetails.get(0).locationCode,
                saved.getOutboundTime(),
                "出库成功，从货位: " + locationInfo + " 出库"
        );
    }

    /**
     * 获取所有出库记录
     */
    public List<OutboundRecord> getAllOutboundRecords() {
        return outboundRepository.findAll();
    }

    /**
     * 根据商品ID获取出库记录
     */
    public List<OutboundRecord> getOutboundRecordsByProduct(Long productId) {
        return outboundRepository.findByProductId(productId);
    }

    /**
     * 出库详情内部类
     */
    private static class OutboundDetail {
        Long recordId;
        Long locationId;
        String locationCode;
        int quantity;

        OutboundDetail(Long recordId, Long locationId, String locationCode, int quantity) {
            this.recordId = recordId;
            this.locationId = locationId;
            this.locationCode = locationCode;
            this.quantity = quantity;
        }
    }
}