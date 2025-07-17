package com.example.smarttrainingsystem.controller;

import com.example.smarttrainingsystem.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 调试测试控制器
 * 用于调试JWT认证问题
 * 
 * @author 开发者
 * @version 1.0
 * @since 2025-07-17
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/debug")
public class DebugController {

    /**
     * 调试请求信息
     */
    @GetMapping("/request-info")
    public Result<Map<String, Object>> debugRequestInfo(HttpServletRequest request) {
        Map<String, Object> info = new HashMap<>();
        
        // 请求基本信息
        info.put("requestURI", request.getRequestURI());
        info.put("method", request.getMethod());
        info.put("remoteAddr", request.getRemoteAddr());
        
        // 请求头信息
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        info.put("headers", headers);
        
        // 请求属性（JWT过滤器设置的）
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("username", request.getAttribute("username"));
        attributes.put("userId", request.getAttribute("userId"));
        attributes.put("roles", request.getAttribute("roles"));
        info.put("attributes", attributes);
        
        // Spring Security上下文
        try {
            org.springframework.security.core.context.SecurityContext securityContext = 
                org.springframework.security.core.context.SecurityContextHolder.getContext();
            org.springframework.security.core.Authentication authentication = securityContext.getAuthentication();
            
            Map<String, Object> securityInfo = new HashMap<>();
            if (authentication != null) {
                securityInfo.put("principal", authentication.getPrincipal());
                securityInfo.put("authorities", authentication.getAuthorities());
                securityInfo.put("authenticated", authentication.isAuthenticated());
            } else {
                securityInfo.put("authentication", "null");
            }
            info.put("security", securityInfo);
        } catch (Exception e) {
            info.put("securityError", e.getMessage());
        }
        
        log.info("调试请求信息: {}", info);
        return Result.success("调试信息获取成功", info);
    }

    /**
     * 无需认证的调试接口
     */
    @GetMapping("/public")
    public Result<String> publicDebug() {
        return Result.success("公开调试接口正常", "这是一个无需认证的接口");
    }
}