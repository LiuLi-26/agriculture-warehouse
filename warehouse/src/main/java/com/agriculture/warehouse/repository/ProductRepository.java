package com.agriculture.warehouse.repository;

import com.agriculture.warehouse.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 根据分类查询农产品
    List<Product> findByCategory(String category);

    // 根据名称模糊查询
    List<Product> findByNameContaining(String name);
}