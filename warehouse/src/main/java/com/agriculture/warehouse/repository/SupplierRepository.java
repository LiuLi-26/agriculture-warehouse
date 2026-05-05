package com.agriculture.warehouse.repository;

import com.agriculture.warehouse.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByNameContaining(String name);
    List<Supplier> findByCategory(String category);
}