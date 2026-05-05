package com.agriculture.warehouse.service;

import com.agriculture.warehouse.dto.InboundRequest;
import com.agriculture.warehouse.dto.InboundResponse;
import com.agriculture.warehouse.entity.*;
import com.agriculture.warehouse.exception.BusinessException;
import com.agriculture.warehouse.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InboundService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StorageLocationRepository locationRepository;

    @Autowired
    private InboundRepository inboundRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private LocationScoreCalculator scoreCalculator;

    /**
     * 智能货位分配算法（加权评分制）
     * @param product 商品
     * @param quantity 入库数量
     * @return 最佳货位
     */
    private StorageLocation findOptimalLocation(Product product, int quantity) {
        // 1. 获取所有空闲货位
        List<StorageLocation> freeLocations = locationRepository.findByIsOccupiedFalse();

        if (freeLocations.isEmpty()) {
            return null;
        }

        // 2. 额外考虑已存放相同商品的货位（虽然被占用，但可以继续放）
        List<StorageLocation> sameProductLocations = locationRepository.findAll().stream()
                .filter(loc -> loc.getIsOccupied() &&
                        loc.getCurrentProductId() != null &&
                        loc.getCurrentProductId().equals(product.getId()))
                .collect(Collectors.toList());

        // 3. 合并候选货位：相同商品货位 + 空闲货位
        List<StorageLocation> candidates = sameProductLocations;
        candidates.addAll(freeLocations);

        if (candidates.isEmpty()) {
            return null;
        }

        // 4. 为每个候选货位计算综合评分
        StorageLocation bestLocation = null;
        int maxScore = -1;

        for (StorageLocation location : candidates) {
            int score = scoreCalculator.calculateScore(location, product, quantity);

            // 调试日志
            System.out.println("货位: " + location.getLocationCode() +
                    " | 评分: " + score);

            if (score > maxScore) {
                maxScore = score;
                bestLocation = location;
            }
        }

        System.out.println("推荐货位: " + bestLocation.getLocationCode() +
                " | 最终评分: " + maxScore);

        return bestLocation;
    }

    /**
     * 执行入库操作
     */
    @Transactional
    public InboundResponse executeInbound(InboundRequest request, Long operatorId) {
        // 1. 验证商品是否存在
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(404, "商品不存在，ID: " + request.getProductId()));

        // 2. 验证供应商是否存在（如果传入了 supplierId）
        String supplierName = null;
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new BusinessException(404, "供应商不存在，ID: " + request.getSupplierId()));
            supplierName = supplier.getName();
        }

        // 3. 智能分配货位
        StorageLocation location = findOptimalLocation(product, request.getQuantity());

        if (location == null) {
            throw new BusinessException(400, "没有可用的空闲货位，请先释放部分货位");
        }

        // 4. 检查容量
        if (location.getCapacity() != null && request.getQuantity() > location.getCapacity()) {
            throw new BusinessException(400, "入库数量超过货位容量，货位容量: " + location.getCapacity());
        }

        // 5. 如果货位是空闲的，占用它
        if (!location.getIsOccupied()) {
            location.setIsOccupied(true);
            location.setCurrentProductId(product.getId());
            locationRepository.save(location);
        }

        // 6. 创建入库记录（包含 supplierId）
        InboundRecord record = new InboundRecord();
        record.setProductId(product.getId());
        record.setQuantity(request.getQuantity());
        record.setLocationId(location.getId());
        record.setInboundTime(LocalDateTime.now());
        record.setOperatorId(operatorId);
        record.setExpiryDate(request.getExpiryDate());
        record.setSupplierId(request.getSupplierId());

        InboundRecord saved = inboundRepository.save(record);

        // 7. 构建响应
        int finalScore = scoreCalculator.calculateScore(location, product, request.getQuantity());

        return new InboundResponse(
                saved.getId(),
                product.getId(),
                product.getName(),
                saved.getQuantity(),
                location.getId(),
                location.getLocationCode(),
                location.getZone(),
                saved.getInboundTime(),
                saved.getExpiryDate(),
                request.getSupplierId(),
                supplierName,
                "入库成功，货位: " + location.getLocationCode() + " (评分: " + finalScore + ")"
        );
    }

    /**
     * 获取所有入库记录
     */
    public List<InboundRecord> getAllInboundRecords() {
        return inboundRepository.findAll();
    }

    /**
     * 根据商品ID获取入库记录（按过期时间排序，用于先进先出）
     */
    public List<InboundRecord> getInboundRecordsByProduct(Long productId) {
        return inboundRepository.findByProductIdOrderByExpiryDateAsc(productId);
    }

    /**
     * 根据货位ID获取入库记录
     */
    public List<InboundRecord> getInboundRecordsByLocation(Long locationId) {
        return inboundRepository.findByLocationId(locationId);
    }
}