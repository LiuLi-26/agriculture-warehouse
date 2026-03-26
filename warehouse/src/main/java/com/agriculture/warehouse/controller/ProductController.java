package com.agriculture.warehouse.controller;

import com.agriculture.warehouse.annotation.Log;
import com.agriculture.warehouse.annotation.RequireRole;
import com.agriculture.warehouse.dto.ProductRequest;
import com.agriculture.warehouse.dto.ProductResponse;
import com.agriculture.warehouse.entity.Product;
import com.agriculture.warehouse.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@Tag(name = "农产品管理", description = "农产品的增删改查接口")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取所有农产品
     */
    @GetMapping
    @Operation(summary = "获取所有农产品")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "农产品管理", operation = "查询农产品列表")
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取农产品
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取农产品")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "农产品管理", operation = "查询农产品详情")
    public ProductResponse getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("农产品不存在，ID: " + id));
        return convertToResponse(product);
    }

    /**
     * 根据分类查询
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "根据分类查询")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "农产品管理", operation = "按分类查询农产品")
    public List<ProductResponse> getProductsByCategory(@PathVariable String category) {
        return productService.getProductsByCategory(category).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 搜索农产品
     */
    @GetMapping("/search")
    @Operation(summary = "搜索农产品")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "农产品管理", operation = "搜索农产品")
    public List<ProductResponse> searchProducts(@RequestParam String name) {
        return productService.searchProductsByName(name).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 新增农产品
     */
    @PostMapping
    @Operation(summary = "新增农产品")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "农产品管理", operation = "新增农产品")
    public ProductResponse createProduct(@RequestBody ProductRequest request) {
        Product product = convertToEntity(request);
        Product saved = productService.createProduct(product);
        return convertToResponse(saved);
    }

    /**
     * 更新农产品
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新农产品")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    @Log(module = "农产品管理", operation = "更新农产品")
    public ProductResponse updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        Product product = convertToEntity(request);
        Product updated = productService.updateProduct(id, product);
        return convertToResponse(updated);
    }

    /**
     * 删除农产品
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除农产品")
    @RequireRole({"ADMIN"})
    @Log(module = "农产品管理", operation = "删除农产品")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "删除成功";
    }

    // ========== 转换方法 ==========

    /**
     * Request -> Entity 转换
     */
    private Product convertToEntity(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setStorageCondition(request.getStorageCondition());
        product.setShelfLifeDays(request.getShelfLifeDays());
        product.setAlertThreshold(request.getAlertThreshold());
        product.setDescription(request.getDescription());
        product.setUnit(request.getUnit());  // ← 添加单位字段
        return product;
    }

    /**
     * Entity -> Response 转换
     */
    private ProductResponse convertToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getStorageCondition(),
                product.getShelfLifeDays(),
                product.getAlertThreshold(),
                product.getDescription(),
                product.getUnit(),
                product.getCreateTime()
        );
    }
}