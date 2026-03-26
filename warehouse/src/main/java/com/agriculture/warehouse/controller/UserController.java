package com.agriculture.warehouse.controller;

import com.agriculture.warehouse.annotation.Log;
import com.agriculture.warehouse.dto.LoginRequest;
import com.agriculture.warehouse.dto.LoginResponse;
import com.agriculture.warehouse.dto.RegisterRequest;
import com.agriculture.warehouse.entity.User;
import com.agriculture.warehouse.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户登录、注册等接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    @Log(module = "用户管理", operation = "用户登录")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

        if (user != null) {
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
    @Operation(summary = "用户注册", description = "货主注册账号，角色默认为客户")
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
}