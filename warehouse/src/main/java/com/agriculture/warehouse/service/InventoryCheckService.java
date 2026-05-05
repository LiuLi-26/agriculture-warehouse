package com.agriculture.warehouse.service;

import com.agriculture.warehouse.dto.InventoryCheckRequest;
import com.agriculture.warehouse.dto.InventoryCheckResponse;
import com.agriculture.warehouse.entity.InventoryCheck;
import com.agriculture.warehouse.entity.Product;
import com.agriculture.warehouse.entity.StorageLocation;
import com.agriculture.warehouse.exception.BusinessException;
import com.agriculture.warehouse.repository.InventoryCheckRepository;
import com.agriculture.warehouse.repository.ProductRepository;
import com.agriculture.warehouse.repository.StorageLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryCheckService {

    @Autowired
    private InventoryCheckRepository inventoryCheckRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StorageLocationRepository locationRepository;

    /**
     * 生成盘点单号
     */
    private String generateCheckNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = inventoryCheckRepository.count() + 1;
        return "PD" + date + String.format("%04d", count);
    }

    /**
     * 创建盘点记录
     */
    @Transactional
    public InventoryCheck createInventoryCheck(InventoryCheckRequest request, Long operatorId) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));

        StorageLocation location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new BusinessException(404, "货位不存在"));

        int difference = request.getActualQuantity() - request.getSystemQuantity();
        String status = difference == 0 ? "COMPLETED" : "PENDING";

        InventoryCheck check = new InventoryCheck();
        check.setCheckNo(generateCheckNo());
        check.setCheckDate(LocalDate.now());
        check.setProductId(request.getProductId());
        check.setLocationId(request.getLocationId());
        check.setSystemQuantity(request.getSystemQuantity());
        check.setActualQuantity(request.getActualQuantity());
        check.setDifference(difference);
        check.setStatus(status);
        check.setRemark(request.getRemark());
        check.setOperatorId(operatorId);
        check.setCreateTime(LocalDateTime.now());

        return inventoryCheckRepository.save(check);
    }

    /**
     * 确认盘点结果（更新库存）
     */
    @Transactional
    public void confirmCheck(Long id) {
        InventoryCheck check = inventoryCheckRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "盘点记录不存在"));

        if ("CONFIRMED".equals(check.getStatus())) {
            throw new BusinessException(400, "盘点记录已确认");
        }

        check.setStatus("CONFIRMED");
        inventoryCheckRepository.save(check);

        // TODO: 更新实际库存数量
        // 这里可以根据差异数量调整库存
    }

    /**
     * 获取所有盘点记录
     */
    public List<InventoryCheck> getAllChecks() {
        return inventoryCheckRepository.findAll();
    }

    /**
     * 获取盘点记录（带关联信息）
     */
    public List<InventoryCheckResponse> getAllChecksWithInfo() {
        List<InventoryCheck> checks = inventoryCheckRepository.findAll();

        return checks.stream().map(check -> {
            Product product = productRepository.findById(check.getProductId()).orElse(null);
            StorageLocation location = locationRepository.findById(check.getLocationId()).orElse(null);

            InventoryCheckResponse response = new InventoryCheckResponse();
            response.setId(check.getId());
            response.setCheckNo(check.getCheckNo());
            response.setCheckDate(check.getCheckDate());
            response.setProductId(check.getProductId());
            response.setProductName(product != null ? product.getName() : "未知");
            response.setLocationId(check.getLocationId());
            response.setLocationCode(location != null ? location.getLocationCode() : "未知");
            response.setSystemQuantity(check.getSystemQuantity());
            response.setActualQuantity(check.getActualQuantity());
            response.setDifference(check.getDifference());
            response.setStatus(check.getStatus());
            response.setStatusName(getStatusName(check.getStatus()));
            response.setRemark(check.getRemark());
            response.setCreateTime(check.getCreateTime());
            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 获取盘点统计
     */
    public Map<String, Object> getCheckStatistics(String startDate, String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

        List<InventoryCheck> checks;
        if (start != null && end != null) {
            checks = inventoryCheckRepository.findByCheckDateBetween(start, end);
        } else {
            checks = inventoryCheckRepository.findAll();
        }

        // 按状态统计
        Map<String, Integer> statusStats = new HashMap<>();
        for (InventoryCheck check : checks) {
            statusStats.put(check.getStatus(), statusStats.getOrDefault(check.getStatus(), 0) + 1);
        }

        // 按日期统计盘点次数
        Map<String, Integer> dailyStats = new LinkedHashMap<>();
        for (InventoryCheck check : checks) {
            String date = check.getCheckDate().toString();
            dailyStats.put(date, dailyStats.getOrDefault(date, 0) + 1);
        }

        // 差异统计
        int totalDifference = checks.stream().mapToInt(InventoryCheck::getDifference).sum();
        int positiveDifference = checks.stream().filter(c -> c.getDifference() > 0).mapToInt(InventoryCheck::getDifference).sum();
        int negativeDifference = checks.stream().filter(c -> c.getDifference() < 0).mapToInt(InventoryCheck::getDifference).sum();

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", checks.size());
        result.put("totalDifference", totalDifference);
        result.put("positiveDifference", positiveDifference);
        result.put("negativeDifference", Math.abs(negativeDifference));
        result.put("statusStats", statusStats);
        result.put("dailyStats", dailyStats);
        result.put("pendingCount", (int) checks.stream().filter(c -> "PENDING".equals(c.getStatus())).count());
        result.put("completedCount", (int) checks.stream().filter(c -> "COMPLETED".equals(c.getStatus())).count());
        result.put("confirmedCount", (int) checks.stream().filter(c -> "CONFIRMED".equals(c.getStatus())).count());

        return result;
    }

    private String getStatusName(String status) {
        switch (status) {
            case "PENDING": return "待处理";
            case "COMPLETED": return "已盘平";
            case "CONFIRMED": return "已确认";
            default: return status;
        }
    }
}