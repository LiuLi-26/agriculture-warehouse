package com.agriculture.warehouse.service;

import com.agriculture.warehouse.entity.SystemConfig;
import com.agriculture.warehouse.exception.BusinessException;
import com.agriculture.warehouse.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemConfigService {

    @Autowired
    private SystemConfigRepository configRepository;

    /**
     * 获取所有配置
     */
    public Map<String, String> getAllConfigs() {
        List<SystemConfig> configs = configRepository.findAll();
        Map<String, String> result = new HashMap<>();
        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

    /**
     * 获取单个配置
     */
    public String getConfig(String key) {
        return configRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
    }

    /**
     * 更新配置
     */
    public void updateConfig(String key, String value, String description) {
        SystemConfig config = configRepository.findByConfigKey(key)
                .orElse(new SystemConfig());

        config.setConfigKey(key);
        config.setConfigValue(value);
        if (description != null) {
            config.setDescription(description);
        }
        config.setUpdateTime(LocalDateTime.now());

        configRepository.save(config);
    }

    /**
     * 批量更新配置
     */
    public void batchUpdateConfigs(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            updateConfig(entry.getKey(), entry.getValue(), null);
        }
    }

    /**
     * 初始化默认配置
     */
    public void initDefaultConfigs() {
        // 预警阈值配置
        saveIfNotExists("stock_warning_threshold", "50", "库存预警阈值（件）");
        saveIfNotExists("expiry_warning_days", "7", "保质期预警天数");

        // 系统配置
        saveIfNotExists("system_name", "农产品仓储管理系统", "系统名称");
        saveIfNotExists("company_name", "XX农业科技有限公司", "公司名称");
        saveIfNotExists("page_size", "20", "默认每页显示数量");

        // 入库配置
        saveIfNotExists("inbound_default_unit", "kg", "入库默认单位");

        // 出库配置
        saveIfNotExists("outbound_fifo_enabled", "true", "是否启用先进先出");

        // 环境监测配置
        saveIfNotExists("environment_monitor_interval", "30", "环境监测间隔（秒）");
    }

    private void saveIfNotExists(String key, String value, String description) {
        if (configRepository.findByConfigKey(key).isEmpty()) {
            SystemConfig config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setDescription(description);
            config.setUpdateTime(LocalDateTime.now());
            configRepository.save(config);
        }
    }
}