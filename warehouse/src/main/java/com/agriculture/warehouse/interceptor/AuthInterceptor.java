package com.agriculture.warehouse.interceptor;

import com.agriculture.warehouse.annotation.RequireRole;
import com.agriculture.warehouse.entity.User;
import com.agriculture.warehouse.exception.BusinessException;
import com.agriculture.warehouse.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 打印调试信息
        System.out.println("=== AuthInterceptor ===");
        System.out.println("请求路径: " + request.getRequestURI());
        System.out.println("请求方法: " + request.getMethod());

        // 如果是 OPTIONS 请求（预检请求），直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            System.out.println("OPTIONS 请求，放行");
            return true;
        }

        // 如果不是方法映射，直接放行
        if (!(handler instanceof HandlerMethod)) {
            System.out.println("不是方法映射，放行");
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);

        // 如果方法没有权限注解，直接放行
        if (requireRole == null) {
            System.out.println("方法没有权限注解，放行");
            return true;
        }

        // 获取当前登录用户ID
        String userIdStr = request.getHeader("X-User-Id");
        System.out.println("X-User-Id: " + userIdStr);

        if (userIdStr == null || userIdStr.isEmpty()) {
            System.out.println("未登录，拒绝访问");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return false;
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            System.out.println("用户ID格式错误");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"用户ID格式错误\"}");
            return false;
        }

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            System.out.println("用户不存在");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"用户不存在\"}");
            return false;
        }

        System.out.println("用户: " + user.getUsername() + ", 角色: " + user.getRole());

        // 检查角色权限
        String userRole = user.getRole();
        String[] allowedRoles = requireRole.value();

        if (Arrays.asList(allowedRoles).contains(userRole)) {
            System.out.println("权限验证通过");
            return true;
        } else {
            System.out.println("权限不足，需要角色: " + Arrays.toString(allowedRoles) + ", 当前角色: " + userRole);
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
            return false;
        }
    }
}