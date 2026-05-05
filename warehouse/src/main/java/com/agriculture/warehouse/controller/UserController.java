package com.agriculture.warehouse.controller;

import com.agriculture.warehouse.annotation.Log;
import com.agriculture.warehouse.annotation.RequireRole;
import com.agriculture.warehouse.dto.*;
import com.agriculture.warehouse.entity.User;
import com.agriculture.warehouse.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户登录、注册、个人信息等接口")
public class UserController {

    @Autowired
    private UserService userService;

    // ========== 登录注册 ==========

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    @Log(module = "用户管理", operation = "用户登录")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

        if (user != null) {
            // ========== 检查账户是否被禁用 ==========
            if ("DISABLED".equals(user.getStatus())) {
                return new LoginResponse(null, null, null, "账户已被禁用，请联系管理员");
            }
            // ====================================

            return new LoginResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getRole(),
                    "登录成功"
            );
        } else {
            return new LoginResponse(null, null, null, "用户名或密码错误");
        }
    }

    /**
     * 用户注册（货主）
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    @Log(module = "用户管理", operation = "用户注册")
    public LoginResponse register(@RequestBody RegisterRequest request) {
        User user = userService.register(request);

        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                "注册成功，请登录"
        );
    }

    // ========== 个人信息 ==========

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取当前用户信息")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "用户管理", operation = "获取个人信息")
    public UserResponse getProfile(@RequestHeader("X-User-Id") Long userId) {
        User user = userService.getUserById(userId);
        return convertToUserResponse(user);
    }

    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    @Operation(summary = "修改密码")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "用户管理", operation = "修改密码")
    public ApiResponse<String> changePassword(@RequestBody ChangePasswordRequest request,
                                              @RequestHeader("X-User-Id") Long userId) {
        userService.changePassword(userId, request);
        return ApiResponse.success("密码修改成功");
    }

    /**
     * 更新个人信息
     */
    @PutMapping("/profile")
    @Operation(summary = "更新个人信息")
    @RequireRole({"ADMIN", "WAREHOUSE", "CUSTOMER"})
    @Log(module = "用户管理", operation = "更新个人信息")
    public UserResponse updateProfile(@RequestBody UpdateProfileRequest request,
                                      @RequestHeader("X-User-Id") Long userId) {
        User user = userService.updateProfile(userId, request);
        return convertToUserResponse(user);
    }

    // ========== 用户管理（管理员功能） ==========

    /**
     * 获取所有用户
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有用户")
    @RequireRole({"ADMIN"})
    @Log(module = "用户管理", operation = "查询用户列表")
    public List<UserResponse> getUserList() {
        return userService.getAllUsers().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户")
    @RequireRole({"ADMIN"})
    @Log(module = "用户管理", operation = "查询用户详情")
    public UserResponse getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return convertToUserResponse(user);
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/{id}/role")
    @Operation(summary = "更新用户角色")
    @RequireRole({"ADMIN"})
    @Log(module = "用户管理", operation = "更新用户角色")
    public UserResponse updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        User user = userService.updateUserRole(id, role);
        return convertToUserResponse(user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @RequireRole({"ADMIN"})
    @Log(module = "用户管理", operation = "删除用户")
    public ApiResponse<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("删除成功");
    }

    // ========== 转换方法 ==========

    /**
     * 转换 User 到 UserResponse
     */
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setCreateTime(user.getCreateTime());
        return response;
    }
}