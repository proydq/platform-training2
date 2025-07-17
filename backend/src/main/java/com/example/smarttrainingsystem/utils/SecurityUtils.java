package com.example.smarttrainingsystem.utils;

import com.example.smarttrainingsystem.entity.User;
import com.example.smarttrainingsystem.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 安全工具类
 * 提供当前用户信息获取和权限检查功能
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Slf4j
@Component
public class SecurityUtils {

    private static JwtUtil jwtUtil;

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        SecurityUtils.jwtUtil = jwtUtil;
    }

    /**
     * 获取当前认证用户
     *
     * @return 当前用户认证信息
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前用户名
     *
     * @return 当前用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(1001, "用户未登录");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }

        throw new BusinessException(1001, "无法获取当前用户信息");
    }

    /**
     * 获取当前用户ID
     * 从JWT Token中解析用户ID
     *
     * @return 当前用户ID
     */
    public static String getCurrentUserId() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(1001, "用户未登录");
        }

        Object principal = authentication.getPrincipal();

        // 如果是自定义的UserDetails实现
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }

        // 如果是JWT Token，从credentials中获取
        Object credentials = authentication.getCredentials();
        if (credentials instanceof String) {
            String token = (String) credentials;
            return jwtUtil.getUserIdFromToken(token);
        }

        // 从authentication的details中获取
        if (authentication.getDetails() instanceof String) {
            return (String) authentication.getDetails();
        }

        // 临时解决方案：从用户名推断用户ID（仅用于开发阶段）
        String username = getCurrentUsername();
        if ("admin".equals(username)) {
            return "admin";
        } else if ("teacher01".equals(username)) {
            return "teacher01";
        } else if ("student01".equals(username)) {
            return "student01";
        }

        throw new BusinessException(1001, "无法获取当前用户ID");
    }

    /**
     * 获取当前用户角色列表
     *
     * @return 当前用户角色列表
     */
    @SuppressWarnings("unchecked")
    public static List<String> getCurrentUserRoles() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(1001, "用户未登录");
        }

        Object principal = authentication.getPrincipal();

        // 如果是自定义的UserDetails实现
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getRoles();
        }

        // 从JWT Token中获取角色信息
        Object credentials = authentication.getCredentials();
        if (credentials instanceof String) {
            String token = (String) credentials;
            return jwtUtil.getRolesFromToken(token);
        }

        // 从Spring Security的权限中获取
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5)) // 移除"ROLE_"前缀
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 检查当前用户是否拥有指定角色
     *
     * @param role 角色名称
     * @return true-拥有，false-不拥有
     */
    public static boolean hasRole(String role) {
        try {
            List<String> roles = getCurrentUserRoles();
            return roles.contains(role);
        } catch (Exception e) {
            log.warn("检查用户角色失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查当前用户是否拥有任意一个指定角色
     *
     * @param roles 角色列表
     * @return true-拥有任意一个，false-都不拥有
     */
    public static boolean hasAnyRole(String... roles) {
        try {
            List<String> userRoles = getCurrentUserRoles();
            for (String role : roles) {
                if (userRoles.contains(role)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("检查用户角色失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查当前用户是否为管理员
     *
     * @return true-是管理员，false-不是管理员
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * 检查当前用户是否为讲师
     *
     * @return true-是讲师，false-不是讲师
     */
    public static boolean isTeacher() {
        return hasRole("TEACHER");
    }

    /**
     * 检查当前用户是否为学员
     *
     * @return true-是学员，false-不是学员
     */
    public static boolean isStudent() {
        return hasRole("STUDENT");
    }

    /**
     * 检查当前用户是否已登录
     *
     * @return true-已登录，false-未登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getCurrentAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 获取当前用户的详细信息
     * 注意：这需要从数据库查询，建议在需要时才调用
     *
     * @return 当前用户实体
     */
    public static User getCurrentUser() {
        // 这里需要注入UserService或UserRepository来查询用户详细信息
        // 由于是静态方法，建议通过ApplicationContext获取Bean
        throw new UnsupportedOperationException("此方法需要在具体业务中实现");
    }

    /**
     * 从HttpServletRequest中获取用户ID
     * 这是一个备用方法，当Spring Security上下文不可用时使用
     *
     * @param request HTTP请求
     * @return 用户ID
     */
    public static String getUserIdFromRequest(HttpServletRequest request) {
        // 从请求头中获取token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }

        // 从请求属性中获取（如果之前的过滤器已经解析）
        Object userId = request.getAttribute("userId");
        if (userId instanceof String) {
            return (String) userId;
        }

        throw new BusinessException(1001, "无法获取用户ID");
    }

    /**
     * 从HttpServletRequest中获取用户名
     *
     * @param request HTTP请求
     * @return 用户名
     */
    public static String getUsernameFromRequest(HttpServletRequest request) {
        // 从请求头中获取token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.getUsernameFromToken(token);
        }

        // 从请求属性中获取
        Object username = request.getAttribute("username");
        if (username instanceof String) {
            return (String) username;
        }

        throw new BusinessException(1001, "无法获取用户名");
    }

    /**
     * 清除当前用户的认证信息
     */
    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 自定义UserDetails实现
     * 用于扩展Spring Security的UserDetails，包含用户ID和角色信息
     */
    public static class CustomUserDetails implements UserDetails {
        private String userId;
        private String username;
        private String password;
        private List<String> roles;
        private boolean enabled;
        private boolean accountNonExpired;
        private boolean accountNonLocked;
        private boolean credentialsNonExpired;

        public CustomUserDetails(String userId, String username, String password, List<String> roles) {
            this.userId = userId;
            this.username = username;
            this.password = password;
            this.roles = roles;
            this.enabled = true;
            this.accountNonExpired = true;
            this.accountNonLocked = true;
            this.credentialsNonExpired = true;
        }

        public String getUserId() {
            return userId;
        }

        public List<String> getRoles() {
            return roles;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public boolean isAccountNonExpired() {
            return accountNonExpired;
        }

        @Override
        public boolean isAccountNonLocked() {
            return accountNonLocked;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return credentialsNonExpired;
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return roles.stream()
                    .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                    .collect(java.util.stream.Collectors.toList());
        }
    }
}