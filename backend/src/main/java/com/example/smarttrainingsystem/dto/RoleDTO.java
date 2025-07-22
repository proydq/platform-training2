package com.example.smarttrainingsystem.dto;

import lombok.Data;

/**
 * 角色下拉选项DTO
 */
public class RoleDTO {

    @Data
    public static class Option {
        private String label;
        private String value;
    }
}
