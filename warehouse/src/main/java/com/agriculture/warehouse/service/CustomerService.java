package com.agriculture.warehouse.service;

import com.agriculture.warehouse.dto.CustomerResponse;
import com.agriculture.warehouse.entity.User;
import com.agriculture.warehouse.exception.BusinessException;
import com.agriculture.warehouse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取所有客户（角色为 CUSTOMER 的用户）
     */
    public List<CustomerResponse> getAllCustomers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .filter(user -> "CUSTOMER".equals(user.getRole()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取客户
     */
    public CustomerResponse getCustomerById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "客户不存在"));

        if (!"CUSTOMER".equals(user.getRole())) {
            throw new BusinessException(400, "该用户不是客户");
        }

        return convertToResponse(user);
    }

    /**
     * 搜索客户
     */
    public List<CustomerResponse> searchCustomers(String keyword) {
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(user -> "CUSTOMER".equals(user.getRole()))
                .filter(user -> user.getUsername().contains(keyword))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 禁用客户
     */
    public void disableCustomer(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "客户不存在"));

        if (!"CUSTOMER".equals(user.getRole())) {
            throw new BusinessException(400, "该用户不是客户");
        }

        user.setStatus("DISABLED");
        userRepository.save(user);
    }

    /**
     * 启用客户
     */
    public void enableCustomer(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "客户不存在"));

        if (!"CUSTOMER".equals(user.getRole())) {
            throw new BusinessException(400, "该用户不是客户");
        }

        user.setStatus("ACTIVE");
        userRepository.save(user);
    }

    /**
     * 删除客户
     */
    public void deleteCustomer(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "客户不存在"));

        if (!"CUSTOMER".equals(user.getRole())) {
            throw new BusinessException(400, "该用户不是客户");
        }

        userRepository.delete(user);
    }

    /**
     * 获取客户统计
     */
    public CustomerStatistics getStatistics() {
        List<User> allUsers = userRepository.findAll();
        List<User> customers = allUsers.stream()
                .filter(user -> "CUSTOMER".equals(user.getRole()))
                .collect(Collectors.toList());

        long activeCount = customers.stream()
                .filter(c -> "ACTIVE".equals(c.getStatus()))
                .count();

        CustomerStatistics stats = new CustomerStatistics();
        stats.setTotalCount((long) customers.size());
        stats.setActiveCount(activeCount);

        // 计算近期注册客户（最近30天）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long newCount = customers.stream()
                .filter(c -> c.getCreateTime() != null && c.getCreateTime().isAfter(thirtyDaysAgo))
                .count();
        stats.setNewCount(newCount);

        return stats;
    }

    private CustomerResponse convertToResponse(User user) {
        CustomerResponse response = new CustomerResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setStatus("ACTIVE".equals(user.getStatus()) ? "正常" : "已禁用");
        response.setCreateTime(user.getCreateTime());
        return response;
    }

    /**
     * 客户统计内部类
     */
    public static class CustomerStatistics {
        private Long totalCount;
        private Long activeCount;
        private Long newCount;

        public Long getTotalCount() { return totalCount; }
        public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
        public Long getActiveCount() { return activeCount; }
        public void setActiveCount(Long activeCount) { this.activeCount = activeCount; }
        public Long getNewCount() { return newCount; }
        public void setNewCount(Long newCount) { this.newCount = newCount; }
    }
}