package com.agriculture.warehouse.service;

import com.agriculture.warehouse.entity.Product;
import com.agriculture.warehouse.entity.StorageLocation;
import com.agriculture.warehouse.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class LocationScoreCalculator {

    @Autowired
    private ProductRepository productRepository;

    // 权重常量
    private static final int WEIGHT_SAME_PRODUCT = 100;   // 相同商品
    private static final int WEIGHT_SAME_CATEGORY = 80;   // 相同品类
    private static final int WEIGHT_STORAGE_MATCH = 60;   // 存储条件匹配
    private static final int WEIGHT_NEAR_EXIT = 40;       // 靠近出口
    private static final int WEIGHT_LOW_SHELF = 30;       // 低货位
    private static final int WEIGHT_HIGH_CAPACITY = 20;   // 容量充足
    private static final int WEIGHT_FAST_MOVING = 15;     // 高频商品
    private static final int WEIGHT_SHORT_SHELF_OUTER = 25; // 短期商品靠外

    // 高频商品列表（可配置）
    private static final List<String> FAST_MOVING_PRODUCTS = Arrays.asList(
            "苹果", "香蕉", "土豆", "西红柿", "鸡蛋"
    );

    /**
     * 计算货位综合评分
     * @param location 候选货位
     * @param product 要入库的商品
     * @param quantity 入库数量
     * @return 综合得分
     */
    public int calculateScore(StorageLocation location, Product product, int quantity) {
        int score = 0;

        // 规则1：相同商品优先（权重最高）
        score += scoreSameProduct(location, product);

        // 规则2：相同品类优先
        score += scoreSameCategory(location, product);

        // 规则3：存储条件完全匹配
        score += scoreStorageMatch(location, product);

        // 规则4：靠近出口（货位编号越小越靠近）
        score += scoreNearExit(location);

        // 规则5：低货位优先（搬运更方便）
        score += scoreLowShelf(location);

        // 规则6：容量充足
        score += scoreCapacity(location, quantity);

        // 规则7：高频商品放黄金区域
        score += scoreFastMovingZone(product, location);

        // 规则8：短期商品靠外放
        score += scoreShortShelfOuter(product, location);

        return score;
    }

    /**
     * 规则1：相同商品优先
     */
    private int scoreSameProduct(StorageLocation location, Product product) {
        if (location.getIsOccupied() &&
                location.getCurrentProductId() != null &&
                location.getCurrentProductId().equals(product.getId())) {
            return WEIGHT_SAME_PRODUCT;
        }
        return 0;
    }

    /**
     * 规则2：相同品类优先
     */
    private int scoreSameCategory(StorageLocation location, Product product) {
        if (location.getIsOccupied() && location.getCurrentProductId() != null) {
            Product currentProduct = productRepository.findById(location.getCurrentProductId()).orElse(null);
            if (currentProduct != null &&
                    currentProduct.getCategory() != null &&
                    currentProduct.getCategory().equals(product.getCategory())) {
                return WEIGHT_SAME_CATEGORY;
            }
        }
        return 0;
    }

    /**
     * 规则3：存储条件匹配
     */
    private int scoreStorageMatch(StorageLocation location, Product product) {
        String zone = location.getZone();
        String condition = product.getStorageCondition();

        if (condition == null) condition = "常温";

        // 冷藏品必须放冷藏区
        if ("冷藏".equals(condition) && "冷藏区".equals(zone)) {
            return WEIGHT_STORAGE_MATCH;
        }
        // 冷冻品必须放冷冻区
        if ("冷冻".equals(condition) && "冷冻区".equals(zone)) {
            return WEIGHT_STORAGE_MATCH;
        }
        // 常温品放非冷藏冷冻区
        if (("常温".equals(condition)) &&
                !zone.contains("冷藏") && !zone.contains("冷冻")) {
            return WEIGHT_STORAGE_MATCH;
        }
        return 0;
    }

    /**
     * 规则4：靠近出口（货位编号越小分越高）
     */
    private int scoreNearExit(StorageLocation location) {
        int distance = extractDistanceNumber(location.getLocationCode());
        // 距离越小分越高，最大40分
        int score = (int)(WEIGHT_NEAR_EXIT * (1.0 / (distance + 1)));
        return Math.min(score, WEIGHT_NEAR_EXIT);
    }

    /**
     * 规则5：低货位优先
     */
    private int scoreLowShelf(StorageLocation location) {
        if (isLowShelf(location)) {
            return WEIGHT_LOW_SHELF;
        }
        return 0;
    }

    /**
     * 规则6：容量充足
     */
    private int scoreCapacity(StorageLocation location, int quantity) {
        if (location.getCapacity() != null && location.getCapacity() >= quantity) {
            return WEIGHT_HIGH_CAPACITY;
        }
        return 0;
    }

    /**
     * 规则7：高频商品放黄金区域
     */
    private int scoreFastMovingZone(Product product, StorageLocation location) {
        if (isFastMoving(product) && isGoldenZone(location)) {
            return WEIGHT_FAST_MOVING;
        }
        return 0;
    }

    /**
     * 规则8：短期商品靠外放
     */
    private int scoreShortShelfOuter(Product product, StorageLocation location) {
        if (product.getShelfLifeDays() != null &&
                product.getShelfLifeDays() < 7 &&
                isOuterShelf(location)) {
            return WEIGHT_SHORT_SHELF_OUTER;
        }
        return 0;
    }

    // ========== 辅助方法 ==========

    /**
     * 判断是否为低货位（1-3层）
     */
    private boolean isLowShelf(StorageLocation location) {
        String code = location.getLocationCode();
        String[] parts = code.split("-");
        if (parts.length >= 3) {
            try {
                int shelfNum = Integer.parseInt(parts[2]);
                return shelfNum <= 3;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * 判断是否为高频商品
     */
    private boolean isFastMoving(Product product) {
        if (product.getName() == null) return false;
        return FAST_MOVING_PRODUCTS.stream()
                .anyMatch(name -> product.getName().contains(name));
    }

    /**
     * 判断是否为黄金区域（靠近出口的货位）
     */
    private boolean isGoldenZone(StorageLocation location) {
        String code = location.getLocationCode();
        return code.startsWith("A-01") || code.startsWith("A-02") ||
                code.startsWith("B-01") || code.startsWith("B-02");
    }

    /**
     * 判断是否为外侧货位（方便取货）
     */
    private boolean isOuterShelf(StorageLocation location) {
        String code = location.getLocationCode();
        return code.endsWith("01") || code.endsWith("02");
    }

    /**
     * 从货位编号提取距离数字（A-01-01 → 1）
     */
    private int extractDistanceNumber(String code) {
        try {
            String[] parts = code.split("-");
            if (parts.length >= 2) {
                return Integer.parseInt(parts[1]);
            }
        } catch (NumberFormatException e) {
            // 忽略
        }
        return 999;
    }
}