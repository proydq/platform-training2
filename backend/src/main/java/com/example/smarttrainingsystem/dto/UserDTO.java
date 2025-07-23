package com.example.smarttrainingsystem.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 用户管理相关DTO
 */
public class UserDTO {

    /**
     * 用户列表项
     */
    @Data
    public static class ListItem {
        private String id;
        private String username;
        private String name;
        private String email;
        private String role;
        private String status;
        private String department;
        private String phone;
        private LocalDateTime lastLogin;
    }

    /**
     * 用户创建请求
     */
    @Data
    public static class CreateRequest {
        @NotBlank(message = "姓名不能为空")
        private String name;

        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度必须在3-20字符之间")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
        private String username;

        @Email(message = "邮箱格式不正确")
        private String email;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度必须在6-20字符之间")
        private String password;

        @NotBlank(message = "角色不能为空")
        private String role;

        @NotBlank(message = "状态不能为空")
        private String status;

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;

        private String department;

        private String description;
    }
}
