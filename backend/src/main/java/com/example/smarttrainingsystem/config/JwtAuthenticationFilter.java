package com.example.smarttrainingsystem.config;

import com.example.smarttrainingsystem.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 简化的JWT认证过滤器（调试版本）
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-07-17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * 不需要认证的路径（调整排除范围）
     */
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/api/v1/auth/login",        // 只排除登录接口
            "/api/v1/auth/check-username", // 检查用户名接口
            "/api/v1/test",              // 所有测试接口
            "/api/v1/debug/public",      // 公开调试接口
            "/api/health",               // 健康检查
            "/api/error",                 // 错误页面
            "/api/v1/files/",  // 添加这一行，允许文件访问不需要认证
            "/api/v1/media/video/",        // ✅ 视频播放白名单
            "/api/v1/media/document/" // 文档播放白名单
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        log.info("=== JWT过滤器处理请求: {} {} ===", method, requestPath);

        // 1. 检查是否为排除路径
        if (isExcludedPath(requestPath)) {
            log.info("✅ 跳过JWT验证: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        log.info("🔐 需要JWT验证的路径: {}", requestPath);

        // 2. 提取Token
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("❌ 请求{}缺少Authorization头", requestPath);
            sendUnauthorizedError(response, "缺少访问令牌");
            return;
        }

        log.info("🎫 提取到Token: {}...", token.substring(0, Math.min(20, token.length())));

        // 3. 验证Token
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            String userId = jwtUtil.getUserIdFromToken(token);
            List<String> roles = jwtUtil.getRolesFromToken(token);

            log.info("👤 Token解析结果 - 用户: {}, ID: {}, 角色: {}", username, userId, roles);

            if (username == null || !jwtUtil.validateToken(token, username)) {
                log.warn("❌ Token验证失败");
                sendUnauthorizedError(response, "访问令牌无效或已过期");
                return;
            }

            // 4. 设置Spring Security认证信息
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 5. 设置请求属性
            request.setAttribute("username", username);
            request.setAttribute("userId", userId);
            request.setAttribute("roles", roles);

            log.info("✅ JWT验证成功，用户: {}, 权限: {}", username, authorities);

        } catch (Exception e) {
            log.error("❌ JWT验证异常: {}", e.getMessage(), e);
            sendUnauthorizedError(response, "访问令牌验证失败");
            return;
        }

        // 6. 继续处理请求
        filterChain.doFilter(request, response);
    }

    /**
     * 检查是否为排除路径
     */
    private boolean isExcludedPath(String requestPath) {
        boolean excluded = EXCLUDED_PATHS.stream().anyMatch(path -> {
            // 处理通配符路径
            if (path.endsWith("/")) {
                return requestPath.startsWith(path);
            } else {
                return requestPath.equals(path) || requestPath.startsWith(path + "/");
            }
        });
        log.info("🔍 路径检查: {} -> {}", requestPath, excluded ? "排除" : "需要验证");
        return excluded;
    }

    /**
     * 从请求中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        log.debug("Authorization头: {}", authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null");

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    /**
     * 发送未授权错误响应
     */
    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String jsonResponse = String.format(
                "{\"code\": 401, \"message\": \"%s\", \"data\": null, \"timestamp\": %d, \"success\": false}",
                message, System.currentTimeMillis()
        );

        response.getWriter().write(jsonResponse);
        log.info("📤 发送401响应: {}", message);
    }
}