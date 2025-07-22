package com.example.smarttrainingsystem.dto;

import lombok.Data;

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
        private LocalDateTime lastLogin;
    }
}
