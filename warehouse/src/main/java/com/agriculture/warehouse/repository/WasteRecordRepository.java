package com.agriculture.warehouse.repository;

import com.agriculture.warehouse.entity.WasteRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface WasteRecordRepository extends JpaRepository<WasteRecord, Long> {

    List<WasteRecord> findByWasteDateBetween(LocalDate startDate, LocalDate endDate);

    List<WasteRecord> findByProductId(Long productId);

    @Query("SELECT SUM(w.quantity) FROM WasteRecord w WHERE w.wasteDate BETWEEN :startDate AND :endDate")
    Integer getTotalWasteBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}