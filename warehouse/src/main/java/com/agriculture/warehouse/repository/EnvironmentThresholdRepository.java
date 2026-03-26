package com.agriculture.warehouse.repository;

import com.agriculture.warehouse.entity.EnvironmentThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EnvironmentThresholdRepository extends JpaRepository<EnvironmentThreshold, Long> {
    Optional<EnvironmentThreshold> findByZone(String zone);
}