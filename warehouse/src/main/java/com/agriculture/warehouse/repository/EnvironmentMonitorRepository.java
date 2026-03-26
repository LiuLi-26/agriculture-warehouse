package com.agriculture.warehouse.repository;

import com.agriculture.warehouse.entity.EnvironmentMonitor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnvironmentMonitorRepository extends JpaRepository<EnvironmentMonitor, Long> {
    List<EnvironmentMonitor> findTop10ByOrderByCreateTimeDesc();
    List<EnvironmentMonitor> findByZoneOrderByCreateTimeDesc(String zone);
}