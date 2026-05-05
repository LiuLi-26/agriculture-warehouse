package com.agriculture.warehouse.service;

import com.agriculture.warehouse.dto.WasteRequest;
import com.agriculture.warehouse.dto.WasteResponse;
import com.agriculture.warehouse.entity.Product;
import com.agriculture.warehouse.entity.WasteRecord;
import com.agriculture.warehouse.exception.BusinessException;
import com.agriculture.warehouse.repository.ProductRepository;
import com.agriculture.warehouse.repository.WasteRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WasteService {

    @Autowired
    private WasteRecordRepository wasteRecordRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<WasteRecord> getAllWasteRecords() {
        return wasteRecordRepository.findAll();
    }

    public WasteRecord getWasteRecordById(Long id) {
        return wasteRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "损耗记录不存在"));
    }

    @Transactional
    public WasteRecord createWasteRecord(WasteRequest request, Long operatorId) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));

        WasteRecord record = new WasteRecord();
        record.setProductId(request.getProductId());
        record.setQuantity(request.getQuantity());
        record.setWasteType(request.getWasteType());
        record.setWasteDate(request.getWasteDate() != null ? request.getWasteDate() : java.time.LocalDate.now());
        record.setReason(request.getReason());
        record.setOperatorId(operatorId);
        record.setCreateTime(LocalDateTime.now());

        return wasteRecordRepository.save(record);
    }

    @Transactional
    public void deleteWasteRecord(Long id) {
        wasteRecordRepository.deleteById(id);
    }

    /**
     * 损耗统计报表
     */
    public Map<String, Object> getWasteStatistics(String startDate, String endDate) {
        java.time.LocalDate start = startDate != null ? java.time.LocalDate.parse(startDate) : null;
        java.time.LocalDate end = endDate != null ? java.time.LocalDate.parse(endDate) : null;

        List<WasteRecord> records;
        if (start != null && end != null) {
            records = wasteRecordRepository.findByWasteDateBetween(start, end);
        } else {
            records = wasteRecordRepository.findAll();
        }

        // 按日期统计损耗数量
        Map<String, Integer> dailyStats = new LinkedHashMap<>();
        // 按商品统计损耗数量
        Map<String, Integer> productStats = new LinkedHashMap<>();
        // 按损耗类型统计
        Map<String, Integer> typeStats = new LinkedHashMap<>();

        for (WasteRecord record : records) {
            String date = record.getWasteDate().toString();
            dailyStats.put(date, dailyStats.getOrDefault(date, 0) + record.getQuantity());

            Product product = productRepository.findById(record.getProductId()).orElse(null);
            String productName = product != null ? product.getName() : "未知";
            productStats.put(productName, productStats.getOrDefault(productName, 0) + record.getQuantity());

            String typeName = getWasteTypeName(record.getWasteType());
            typeStats.put(typeName, typeStats.getOrDefault(typeName, 0) + record.getQuantity());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dailyStats", dailyStats);
        result.put("productStats", productStats);
        result.put("typeStats", typeStats);
        result.put("totalQuantity", records.stream().mapToInt(WasteRecord::getQuantity).sum());
        result.put("totalCount", records.size());

        return result;
    }

    private String getWasteTypeName(String type) {
        switch (type) {
            case "EXPIRED": return "过期损耗";
            case "DAMAGED": return "损坏损耗";
            case "OTHER": return "其他损耗";
            default: return type;
        }
    }
}