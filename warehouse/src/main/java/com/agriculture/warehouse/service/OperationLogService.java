package com.agriculture.warehouse.service;

import com.agriculture.warehouse.dto.LogQueryDTO;
import com.agriculture.warehouse.dto.OperationLogDTO;
import com.agriculture.warehouse.entity.OperationLog;
import com.agriculture.warehouse.repository.OperationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class OperationLogService {

    @Autowired
    private OperationLogRepository logRepository;

    /**
     * 保存操作日志
     */
    public void saveLog(OperationLog log) {
        log.setCreateTime(LocalDateTime.now());
        logRepository.save(log);
    }

    /**
     * 分页查询日志
     */
    public Page<OperationLogDTO> queryLogs(LogQueryDTO queryDTO) {
        Pageable pageable = PageRequest.of(queryDTO.getPage() - 1, queryDTO.getSize());

        Page<OperationLog> logPage = logRepository.search(
                queryDTO.getUserId(),
                queryDTO.getOperationType(),
                queryDTO.getModule(),
                queryDTO.getStartTime(),
                queryDTO.getEndTime(),
                pageable
        );

        return logPage.map(this::convertToDTO);
    }

    /**
     * 根据用户ID查询日志
     */
    public Page<OperationLogDTO> getLogsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<OperationLog> logPage = logRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
        return logPage.map(this::convertToDTO);
    }

    /**
     * 获取统计信息
     */
    public LogStatistics getStatistics() {
        LogStatistics stats = new LogStatistics();
        stats.setTotalCount(logRepository.count());
        stats.setLoginCount(logRepository.countByOperationType("LOGIN"));
        stats.setCreateCount(logRepository.countByOperationType("CREATE"));
        stats.setUpdateCount(logRepository.countByOperationType("UPDATE"));
        stats.setDeleteCount(logRepository.countByOperationType("DELETE"));
        return stats;
    }

    /**
     * 转换实体到DTO
     */
    private OperationLogDTO convertToDTO(OperationLog log) {
        OperationLogDTO dto = new OperationLogDTO();
        dto.setId(log.getId());
        dto.setUserId(log.getUserId());
        dto.setUsername(log.getUsername());
        dto.setOperationType(log.getOperationType());
        dto.setModule(log.getModule());
        dto.setDescription(log.getDescription());
        dto.setDetail(log.getDetail());
        dto.setIpAddress(log.getIpAddress());
        dto.setRequestUrl(log.getRequestUrl());
        dto.setRequestMethod(log.getRequestMethod());
        dto.setStatus(log.getStatus());
        dto.setErrorMsg(log.getErrorMsg());
        dto.setDurationMs(log.getDurationMs());
        dto.setCreateTime(log.getCreateTime());
        return dto;
    }

    /**
     * 统计信息内部类
     */
    public static class LogStatistics {
        private long totalCount;
        private long loginCount;
        private long createCount;
        private long updateCount;
        private long deleteCount;

        // getter/setter
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
        public long getLoginCount() { return loginCount; }
        public void setLoginCount(long loginCount) { this.loginCount = loginCount; }
        public long getCreateCount() { return createCount; }
        public void setCreateCount(long createCount) { this.createCount = createCount; }
        public long getUpdateCount() { return updateCount; }
        public void setUpdateCount(long updateCount) { this.updateCount = updateCount; }
        public long getDeleteCount() { return deleteCount; }
        public void setDeleteCount(long deleteCount) { this.deleteCount = deleteCount; }
    }
}