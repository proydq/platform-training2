package com.example.smarttrainingsystem.dto;

import lombok.Data;

/**
 * 用户学习概览 DTO
 */
public class UserStudyDTO {
    @Data
    public static class Overview {
        private Long completed;
        private Long inProgress;
        private Long notStarted;
        private Long totalTime; // seconds
    }
}
