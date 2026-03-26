package com.agriculture.warehouse.service;

import com.agriculture.warehouse.entity.Product;
import com.agriculture.warehouse.exception.BusinessException;
import com.agriculture.warehouse.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * 获取所有农产品
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * 根据ID获取农产品
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * 根据分类获取农产品
     */
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    /**
     * 根据名称模糊搜索
     */
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContaining(name);
    }

    /**
     * 新增农产品
     */
    public Product createProduct(Product product) {
        // 设置创建时间
        product.setCreateTime(LocalDateTime.now());

        // 设置默认预警阈值（如果前端没有传值）
        if (product.getAlertThreshold() == null) {
            product.setAlertThreshold(getDefaultAlertThreshold(product.getShelfLifeDays()));
        }

        // ========== 设置默认单位 ==========
        // 如果前端没有传入单位，或者单位为空，设置为默认单位 "kg"
        if (product.getUnit() == null || product.getUnit().trim().isEmpty()) {
            product.setUnit("kg");
        }
        // =================================

        return productRepository.save(product);
    }

    /**
     * 更新农产品
     */
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "农产品不存在，ID: " + id));

        // 更新字段
        product.setName(productDetails.getName());
        product.setCategory(productDetails.getCategory());
        product.setStorageCondition(productDetails.getStorageCondition());
        product.setShelfLifeDays(productDetails.getShelfLifeDays());
        product.setDescription(productDetails.getDescription());

        // 更新预警阈值（如果传入了新值）
        if (productDetails.getAlertThreshold() != null) {
            product.setAlertThreshold(productDetails.getAlertThreshold());
        }

        // 更新单位（如果传入了新值）
        if (productDetails.getUnit() != null && !productDetails.getUnit().trim().isEmpty()) {
            product.setUnit(productDetails.getUnit());
        }

        return productRepository.save(product);
    }

    /**
     * 删除农产品
     */
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "农产品不存在，ID: " + id));
        productRepository.deleteById(id);
    }

    /**
     * 根据保质期智能计算默认预警阈值
     * 规则：
     * - 保质期 ≤ 7天：阈值为保质期的 50%（至少1天）
     * - 保质期 ≤ 30天：阈值为保质期的 30%
     * - 保质期 ≤ 90天：阈值为保质期的 20%
     * - 保质期 > 90天：阈值为保质期的 15%（最多60天）
     */
    private Integer getDefaultAlertThreshold(Integer shelfLifeDays) {
        if (shelfLifeDays == null) {
            return 7; // 默认7天
        }

        if (shelfLifeDays <= 7) {
            // 保质期短的商品，提前一半时间预警
            return Math.max(1, shelfLifeDays / 2);
        } else if (shelfLifeDays <= 30) {
            // 保质期中等，提前30%预警
            return Math.max(3, (int) Math.ceil(shelfLifeDays * 0.3));
        } else if (shelfLifeDays <= 90) {
            // 保质期较长，提前20%预警
            return Math.max(7, (int) Math.ceil(shelfLifeDays * 0.2));
        } else {
            // 保质期很长（冷冻品等），提前15%预警，最多60天
            int threshold = (int) Math.ceil(shelfLifeDays * 0.15);
            return Math.min(threshold, 60);
        }
    }
}