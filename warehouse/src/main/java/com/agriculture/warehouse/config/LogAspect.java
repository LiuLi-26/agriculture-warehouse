package com.agriculture.warehouse.config;

import com.agriculture.warehouse.annotation.Log;
import com.agriculture.warehouse.entity.OperationLog;
import com.agriculture.warehouse.service.OperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private OperationLogService logService;

    @Autowired
    private ObjectMapper objectMapper;

    @Around("@annotation(com.agriculture.warehouse.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();

        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // ========== 排除日志模块自身的接口，避免递归 ==========
        String className = method.getDeclaringClass().getSimpleName();
        if ("LogController".equals(className)) {
            // 日志模块的接口不记录日志，直接执行
            System.out.println("【跳过日志】" + className + "." + method.getName() + " - 避免递归");
            return joinPoint.proceed();
        }
        // ===================================================

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }

        // 获取当前用户信息（从请求头获取）
        Long userId = null;
        String username = null;
        String role = null;

        if (request != null) {
            String userIdStr = request.getHeader("X-User-Id");
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    userId = Long.parseLong(userIdStr);
                } catch (NumberFormatException e) {
                    userId = null;
                }
            }
            username = request.getHeader("X-Username");
            role = request.getHeader("X-Role");
        }

        // 如果请求头中没有用户名，但用户ID存在，设置默认用户名
        if ((username == null || username.isEmpty()) && userId != null) {
            username = "用户" + userId;
        }

        // 获取日志注解信息
        Log logAnnotation = method.getAnnotation(Log.class);

        // 构建日志对象
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperationType(getOperationType(method.getName()));
        log.setModule(logAnnotation != null ? logAnnotation.module() : "");
        log.setDescription(logAnnotation != null ? logAnnotation.operation() : "");
        log.setIpAddress(request != null ? getIpAddress(request) : "");
        log.setRequestUrl(request != null ? request.getRequestURI() : "");
        log.setRequestMethod(request != null ? request.getMethod() : "");
        log.setStatus("SUCCESS");
        log.setCreateTime(LocalDateTime.now());

        // 保存请求参数详情
        boolean saveDetail = logAnnotation != null && logAnnotation.saveDetail();
        if (saveDetail) {
            try {
                Object[] args = joinPoint.getArgs();
                Map<String, Object> detailMap = new HashMap<>();
                String[] paramNames = signature.getParameterNames();
                for (int i = 0; i < args.length; i++) {
                    if (args[i] != null && !(args[i] instanceof HttpServletRequest)) {
                        String paramName = paramNames != null && i < paramNames.length ? paramNames[i] : "arg" + i;
                        // 避免记录过大的对象
                        if (args[i] instanceof String && ((String) args[i]).length() > 500) {
                            detailMap.put(paramName, "内容过长，已省略");
                        } else {
                            detailMap.put(paramName, args[i]);
                        }
                    }
                }
                if (!detailMap.isEmpty()) {
                    log.setDetail(objectMapper.writeValueAsString(detailMap));
                }
            } catch (Exception e) {
                log.setDetail("无法序列化参数: " + e.getMessage());
            }
        }

        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();

            log.setStatus("SUCCESS");
            log.setDurationMs(endTime - startTime);
            logService.saveLog(log);

            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();

            log.setStatus("FAILURE");
            log.setDurationMs(endTime - startTime);
            log.setErrorMsg(e.getMessage() != null ? e.getMessage() : "未知错误");
            logService.saveLog(log);

            throw e;
        }
    }

    /**
     * 根据方法名推断操作类型
     */
    private String getOperationType(String methodName) {
        if (methodName == null) return "OTHER";
        if (methodName.startsWith("login")) return "LOGIN";
        if (methodName.startsWith("logout")) return "LOGOUT";
        if (methodName.startsWith("create") || methodName.startsWith("add") || methodName.startsWith("save")) return "CREATE";
        if (methodName.startsWith("update") || methodName.startsWith("edit") || methodName.startsWith("modify")) return "UPDATE";
        if (methodName.startsWith("delete") || methodName.startsWith("remove")) return "DELETE";
        if (methodName.startsWith("get") || methodName.startsWith("find") || methodName.startsWith("query") || methodName.startsWith("list")) return "QUERY";
        return "OTHER";
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        if (request == null) return "";

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理情况下，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "";
    }
}