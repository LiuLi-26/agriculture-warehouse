package com.agriculture.warehouse.repository;

import com.agriculture.warehouse.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    // 根据用户ID查询日志（分页）
    Page<OperationLog> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    // 根据操作类型查询
    Page<OperationLog> findByOperationTypeOrderByCreateTimeDesc(String operationType, Pageable pageable);

    // 时间范围查询
    @Query("SELECT l FROM OperationLog l WHERE l.createTime BETWEEN :startTime AND :endTime ORDER BY l.createTime DESC")
    Page<OperationLog> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               Pageable pageable);

    // 多条件查询
    @Query("SELECT l FROM OperationLog l WHERE " +
            "(:userId IS NULL OR l.userId = :userId) AND " +
            "(:operationType IS NULL OR l.operationType = :operationType) AND " +
            "(:module IS NULL OR l.module = :module) AND " +
            "(:startTime IS NULL OR l.createTime >= :startTime) AND " +
            "(:endTime IS NULL OR l.createTime <= :endTime) " +
            "ORDER BY l.createTime DESC")
    Page<OperationLog> search(@Param("userId") Long userId,
                              @Param("operationType") String operationType,
                              @Param("module") String module,
                              @Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime,
                              Pageable pageable);

    // 统计用户操作次数
    Long countByUserId(Long userId);

    // 统计操作类型次数
    Long countByOperationType(String operationType);
}