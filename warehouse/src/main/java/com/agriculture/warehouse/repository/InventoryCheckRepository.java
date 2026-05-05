package com.agriculture.warehouse.repository;

import com.agriculture.warehouse.entity.InventoryCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface InventoryCheckRepository extends JpaRepository<InventoryCheck, Long> {

    List<InventoryCheck> findByCheckDateBetween(LocalDate startDate, LocalDate endDate);

    List<InventoryCheck> findByStatus(String status);

    List<InventoryCheck> findByProductId(Long productId);

    @Query("SELECT SUM(i.difference) FROM InventoryCheck i WHERE i.checkDate BETWEEN :startDate AND :endDate")
    Integer getTotalDifferenceBetween(LocalDate startDate, LocalDate endDate);
}