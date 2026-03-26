package com.agriculture.warehouse.config;

import com.agriculture.warehouse.service.EnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class ScheduledTaskConfig {

    @Autowired
    private EnvironmentService environmentService;

    /**
     * 每5分钟模拟一次传感器数据
     */
    @Scheduled(fixedDelay = 300000)  // 5分钟 = 300000毫秒
    public void simulateEnvironmentData() {
        environmentService.simulateSensorData();
        System.out.println("环境传感器数据已模拟更新");
    }
}