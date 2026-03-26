package com.agriculture.warehouse.service;

import com.agriculture.warehouse.dto.RegisterRequest;
import com.agriculture.warehouse.entity.User;
import com.agriculture.warehouse.exception.BusinessException;
import com.agriculture.warehouse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
     * 用户注册（货主）
     */
    public User register(RegisterRequest request) {
        // 1. 验证用户名是否已存在
        Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
        if (existingUser.isPresent()) {
            throw new BusinessException(400, "用户名已存在，请更换用户名");
        }

        // 2. 验证密码和确认密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }

        // 3. 验证密码长度（至少6位）
        if (request.getPassword().length() < 6) {
            throw new BusinessException(400, "密码长度不能少于6位");
        }

        // 4. 验证用户名长度（至少3位）
        if (request.getUsername().length() < 3) {
            throw new BusinessException(400, "用户名长度不能少于3位");
        }

        // 5. 创建新用户（角色默认为 CUSTOMER）
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // 生产环境需要加密
        user.setRole("CUSTOMER");  // 注册用户默认为客户角色
        user.setCreateTime(LocalDateTime.now());

        return userRepository.save(user);
    }
}