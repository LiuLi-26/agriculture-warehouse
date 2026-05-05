package com.agriculture.warehouse.service;

import com.agriculture.warehouse.dto.ChangePasswordRequest;
import com.agriculture.warehouse.dto.RegisterRequest;
import com.agriculture.warehouse.dto.UpdateProfileRequest;
import com.agriculture.warehouse.entity.User;
import com.agriculture.warehouse.exception.BusinessException;
import com.agriculture.warehouse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 用户登录
     */
    public User login(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (password.equals(user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    /**
     * 用户注册
     */
    public User register(RegisterRequest request) {
        // 验证用户名是否已存在
        Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
        if (existingUser.isPresent()) {
            throw new BusinessException(400, "用户名已存在，请更换用户名");
        }

        // 验证密码和确认密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }

        // 验证密码长度
        if (request.getPassword().length() < 6) {
            throw new BusinessException(400, "密码长度不能少于6位");
        }

        // 验证用户名长度
        if (request.getUsername().length() < 2) {
            throw new BusinessException(400, "用户名长度不能少于2位");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRole("CUSTOMER");
        user.setCreateTime(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 根据ID获取用户
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    /**
     * 修改密码
     */
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getUserById(userId);

        // 验证原密码
        if (!user.getPassword().equals(request.getOldPassword())) {
            throw new BusinessException(400, "原密码错误");
        }

        // 验证新密码与确认密码
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的新密码不一致");
        }

        // 验证新密码长度
        if (request.getNewPassword().length() < 6) {
            throw new BusinessException(400, "新密码长度不能少于6位");
        }

        // 更新密码
        user.setPassword(request.getNewPassword());
        userRepository.save(user);
    }

    /**
     * 更新个人信息
     */
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserById(userId);

        // 检查用户名是否被占用
        if (!user.getUsername().equals(request.getUsername())) {
            Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
            if (existingUser.isPresent()) {
                throw new BusinessException(400, "用户名已被占用");
            }
            user.setUsername(request.getUsername());
        }

        return userRepository.save(user);
    }

    /**
     * 更新用户角色
     */
    public User updateUserRole(Long userId, String role) {
        User user = getUserById(userId);

        // 验证角色是否有效
        if (!Arrays.asList("ADMIN", "WAREHOUSE", "CUSTOMER").contains(role)) {
            throw new BusinessException(400, "无效的角色类型");
        }

        user.setRole(role);
        return userRepository.save(user);
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}