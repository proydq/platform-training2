package com.example.smarttrainingsystem.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证相关DTO类
 * 
 * @author 开发者
 * @version 1.0
 * @since 2025-07-17
 */
public class AuthDTO {

    /**
     * 登录请求DTO
     */
    @Data
    public static class LoginRequest {
        
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
        private String username;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
        private String password;

        /**
         * 记住我 - 可选参数
         */
        private Boolean remember = false;
    }

    /**
     * 登录响应DTO
     */
    @Data
    public static class LoginResponse {
        
        /**
         * 访问令牌
         */
        private String accessToken;

        /**
         * 令牌类型
         */
        private String tokenType = "Bearer";

        /**
         * 令牌过期时间（毫秒）
         */
        private Long expiresIn;

        /**
         * 用户基本信息
         */
        private UserInfo userInfo;

        /**
         * 用户权限列表
         */
        private List<String> authorities;
    }

    /**
     * 用户信息DTO
     */
    @Data
    public static class UserInfo {
        
        /**
         * 用户ID
         */
        private String userId;

        /**
         * 用户名
         */
        private String username;

        /**
         * 真实姓名
         */
        private String realName;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 手机号
         */
        private String phone;

        /**
         * 头像URL
         */
        private String avatarUrl;

        /**
         * 部门
         */
        private String department;

        /**
         * 职位
         */
        private String position;

        /**
         * 用户状态
         */
        private Integer status;

        /**
         * 角色列表
         */
        private List<RoleInfo> roles;

        /**
         * 最后登录时间
         */
        private LocalDateTime lastLoginTime;
    }

    /**
     * 角色信息DTO
     */
    @Data
    public static class RoleInfo {
        
        /**
         * 角色ID
         */
        private String roleId;

        /**
         * 角色代码
         */
        private String roleCode;

        /**
         * 角色名称
         */
        private String roleName;

        /**
         * 角色描述
         */
        private String description;
    }

    /**
     * 修改密码请求DTO
     */
    @Data
    public static class ChangePasswordRequest {
        
        @NotBlank(message = "原密码不能为空")
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 50, message = "新密码长度必须在6-50个字符之间")
        private String newPassword;

        @NotBlank(message = "确认密码不能为空")
        private String confirmPassword;
    }

    /**
     * 刷新令牌请求DTO
     */
    @Data
    public static class RefreshTokenRequest {
        
        @NotBlank(message = "刷新令牌不能为空")
        private String refreshToken;
    }
}