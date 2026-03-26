package com.agriculture.warehouse.service;

import com.agriculture.warehouse.dto.EnvironmentAlertDTO;
import com.agriculture.warehouse.dto.EnvironmentDataDTO;
import com.agriculture.warehouse.dto.EnvironmentThresholdDTO;
import com.agriculture.warehouse.entity.EnvironmentMonitor;
import com.agriculture.warehouse.entity.EnvironmentThreshold;
import com.agriculture.warehouse.repository.EnvironmentMonitorRepository;
import com.agriculture.warehouse.repository.EnvironmentThresholdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class EnvironmentService {

    @Autowired
    private EnvironmentMonitorRepository monitorRepository;

    @Autowired
    private EnvironmentThresholdRepository thresholdRepository;

    private Random random = new Random();

    /**
     * 模拟传感器数据（每5分钟调用一次）
     */
    @Transactional
    public void simulateSensorData() {
        List<EnvironmentThreshold> thresholds = thresholdRepository.findAll();

        for (EnvironmentThreshold threshold : thresholds) {
            String zone = threshold.getZone();

            // 模拟温度（在阈值附近波动）
            double tempBase = (threshold.getTempMin() + threshold.getTempMax()) / 2;
            double temp = tempBase + (random.nextDouble() - 0.5) * 5;
            temp = Math.max(threshold.getTempMin() - 2, Math.min(threshold.getTempMax() + 2, temp));

            // 模拟湿度
            double humidityBase = (threshold.getHumidityMin() + threshold.getHumidityMax()) / 2;
            double humidity = humidityBase + (random.nextDouble() - 0.5) * 15;
            humidity = Math.max(threshold.getHumidityMin() - 5, Math.min(threshold.getHumidityMax() + 5, humidity));

            // 判断状态
            String status = checkStatus(threshold, temp, humidity);

            EnvironmentMonitor monitor = new EnvironmentMonitor();
            monitor.setZone(zone);
            monitor.setTemperature(Math.round(temp * 10) / 10.0);
            monitor.setHumidity(Math.round(humidity * 10) / 10.0);
            monitor.setStatus(status);
            monitor.setCreateTime(LocalDateTime.now());

            monitorRepository.save(monitor);
        }
    }

    /**
     * 获取所有区域的最新环境数据
     */
    public List<EnvironmentDataDTO> getLatestData() {
        List<EnvironmentDataDTO> result = new ArrayList<>();
        List<EnvironmentThreshold> thresholds = thresholdRepository.findAll();

        for (EnvironmentThreshold threshold : thresholds) {
            List<EnvironmentMonitor> monitors = monitorRepository.findByZoneOrderByCreateTimeDesc(threshold.getZone());
            if (!monitors.isEmpty()) {
                EnvironmentMonitor latest = monitors.get(0);
                EnvironmentDataDTO dto = new EnvironmentDataDTO();
                dto.setId(latest.getId());
                dto.setZone(latest.getZone());
                dto.setTemperature(latest.getTemperature());
                dto.setHumidity(latest.getHumidity());
                dto.setStatus(latest.getStatus());
                dto.setCreateTime(latest.getCreateTime());
                result.add(dto);
            }
        }

        return result;
    }

    /**
     * 获取环境预警列表
     */
    public List<EnvironmentAlertDTO> getAlerts() {
        List<EnvironmentAlertDTO> alerts = new ArrayList<>();
        List<EnvironmentDataDTO> latestData = getLatestData();

        for (EnvironmentDataDTO data : latestData) {
            EnvironmentThreshold threshold = thresholdRepository.findByZone(data.getZone()).orElse(null);
            if (threshold == null) continue;

            // 检查温度异常
            if (data.getTemperature() < threshold.getTempMin()) {
                EnvironmentAlertDTO alert = new EnvironmentAlertDTO();
                alert.setZone(data.getZone());
                alert.setType("TEMPERATURE");
                alert.setCurrentValue(data.getTemperature());
                alert.setMinValue(threshold.getTempMin());
                alert.setMaxValue(threshold.getTempMax());
                alert.setLevel(data.getTemperature() < threshold.getTempMin() - 3 ? "CRITICAL" : "WARNING");
                alert.setMessage(String.format("温度过低: %.1f℃ (正常范围: %.1f~%.1f℃)",
                        data.getTemperature(), threshold.getTempMin(), threshold.getTempMax()));
                alerts.add(alert);
            } else if (data.getTemperature() > threshold.getTempMax()) {
                EnvironmentAlertDTO alert = new EnvironmentAlertDTO();
                alert.setZone(data.getZone());
                alert.setType("TEMPERATURE");
                alert.setCurrentValue(data.getTemperature());
                alert.setMinValue(threshold.getTempMin());
                alert.setMaxValue(threshold.getTempMax());
                alert.setLevel(data.getTemperature() > threshold.getTempMax() + 3 ? "CRITICAL" : "WARNING");
                alert.setMessage(String.format("温度过高: %.1f℃ (正常范围: %.1f~%.1f℃)",
                        data.getTemperature(), threshold.getTempMin(), threshold.getTempMax()));
                alerts.add(alert);
            }

            // 检查湿度异常
            if (data.getHumidity() < threshold.getHumidityMin()) {
                EnvironmentAlertDTO alert = new EnvironmentAlertDTO();
                alert.setZone(data.getZone());
                alert.setType("HUMIDITY");
                alert.setCurrentValue(data.getHumidity());
                alert.setMinValue(threshold.getHumidityMin());
                alert.setMaxValue(threshold.getHumidityMax());
                alert.setLevel(data.getHumidity() < threshold.getHumidityMin() - 10 ? "CRITICAL" : "WARNING");
                alert.setMessage(String.format("湿度过低: %.1f%% (正常范围: %.1f~%.1f%%)",
                        data.getHumidity(), threshold.getHumidityMin(), threshold.getHumidityMax()));
                alerts.add(alert);
            } else if (data.getHumidity() > threshold.getHumidityMax()) {
                EnvironmentAlertDTO alert = new EnvironmentAlertDTO();
                alert.setZone(data.getZone());
                alert.setType("HUMIDITY");
                alert.setCurrentValue(data.getHumidity());
                alert.setMinValue(threshold.getHumidityMin());
                alert.setMaxValue(threshold.getHumidityMax());
                alert.setLevel(data.getHumidity() > threshold.getHumidityMax() + 10 ? "CRITICAL" : "WARNING");
                alert.setMessage(String.format("湿度过高: %.1f%% (正常范围: %.1f~%.1f%%)",
                        data.getHumidity(), threshold.getHumidityMin(), threshold.getHumidityMax()));
                alerts.add(alert);
            }
        }

        return alerts;
    }

    /**
     * 获取历史数据（用于图表）
     */
    public List<EnvironmentDataDTO> getHistoryData(String zone, int hours) {
        List<EnvironmentMonitor> monitors = monitorRepository.findByZoneOrderByCreateTimeDesc(zone);
        return monitors.stream()
                .limit(hours * 12) // 每5分钟一条，12条/小时
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 更新阈值配置
     */
    @Transactional
    public EnvironmentThreshold updateThreshold(EnvironmentThresholdDTO dto) {
        EnvironmentThreshold threshold = thresholdRepository.findByZone(dto.getZone())
                .orElse(new EnvironmentThreshold());

        threshold.setZone(dto.getZone());
        threshold.setTempMin(dto.getTempMin());
        threshold.setTempMax(dto.getTempMax());
        threshold.setHumidityMin(dto.getHumidityMin());
        threshold.setHumidityMax(dto.getHumidityMax());
        threshold.setUpdateTime(LocalDateTime.now());

        return thresholdRepository.save(threshold);
    }

    /**
     * 获取所有阈值配置
     */
    public List<EnvironmentThreshold> getAllThresholds() {
        return thresholdRepository.findAll();
    }

    private String checkStatus(EnvironmentThreshold threshold, double temp, double humidity) {
        boolean tempAbnormal = temp < threshold.getTempMin() || temp > threshold.getTempMax();
        boolean humidityAbnormal = humidity < threshold.getHumidityMin() || humidity > threshold.getHumidityMax();

        if (tempAbnormal || humidityAbnormal) {
            boolean tempCritical = temp < threshold.getTempMin() - 3 || temp > threshold.getTempMax() + 3;
            boolean humidityCritical = humidity < threshold.getHumidityMin() - 10 || humidity > threshold.getHumidityMax() + 10;
            return (tempCritical || humidityCritical) ? "CRITICAL" : "WARNING";
        }

        return "NORMAL";
    }

    private EnvironmentDataDTO convertToDTO(EnvironmentMonitor monitor) {
        EnvironmentDataDTO dto = new EnvironmentDataDTO();
        dto.setId(monitor.getId());
        dto.setZone(monitor.getZone());
        dto.setTemperature(monitor.getTemperature());
        dto.setHumidity(monitor.getHumidity());
        dto.setStatus(monitor.getStatus());
        dto.setCreateTime(monitor.getCreateTime());
        return dto;
    }
}