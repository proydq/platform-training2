package com.example.smarttrainingsystem.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程数据传输对象
 * 
 * @author 开发者
 * @version 1.0
 * @since 2025-07-17
 */
public class CourseDTO {

    /**
     * 课程创建请求DTO
     */
    @Data
    public static class CreateRequest {
        @NotBlank(message = "课程标题不能为空")
        @Size(max = 200, message = "课程标题长度不能超过200字符")
        private String title;

        @Size(max = 2000, message = "课程描述长度不能超过2000字符")
        private String description;

        private String coverImageUrl;

        @NotBlank(message = "课程分类不能为空")
        @Size(max = 50, message = "课程分类长度不能超过50字符")
        private String category;

        private String tags;

        @Min(value = 1, message = "难度级别必须在1-3之间")
        @Max(value = 3, message = "难度级别必须在1-3之间")
        private Integer difficultyLevel = 1;

        @Min(value = 0, message = "预计学习时长不能为负数")
        private Integer estimatedDuration;

        @NotBlank(message = "讲师ID不能为空")
        private String instructorId;

        private String instructorName;

        @Size(max = 2000, message = "学习目标长度不能超过2000字符")
        private String learningObjectives;

        @Size(max = 2000, message = "先决条件长度不能超过2000字符")
        private String prerequisites;

        private Boolean isRequired = false;

        @Min(value = 1, message = "课程有效期必须大于0")
        private Integer validityDays;
    }

    /**
     * 课程更新请求DTO
     */
    @Data
    public static class UpdateRequest {
        @Size(max = 200, message = "课程标题长度不能超过200字符")
        private String title;

        @Size(max = 2000, message = "课程描述长度不能超过2000字符")
        private String description;

        private String coverImageUrl;

        @Size(max = 50, message = "课程分类长度不能超过50字符")
        private String category;

        private String tags;

        @Min(value = 1, message = "难度级别必须在1-3之间")
        @Max(value = 3, message = "难度级别必须在1-3之间")
        private Integer difficultyLevel;

        @Min(value = 0, message = "预计学习时长不能为负数")
        private Integer estimatedDuration;

        private String instructorName;

        @Size(max = 2000, message = "学习目标长度不能超过2000字符")
        private String learningObjectives;

        @Size(max = 2000, message = "先决条件长度不能超过2000字符")
        private String prerequisites;

        private Boolean isRequired;

        @Min(value = 1, message = "课程有效期必须大于0")
        private Integer validityDays;
    }

    /**
     * 课程响应DTO
     */
    @Data
    public static class Response {
        private String id;
        private String title;
        private String description;
        private String coverImageUrl;
        private String category;
        private String tags;
        private Integer status;
        private String statusText;
        private Integer difficultyLevel;
        private String difficultyText;
        private Integer estimatedDuration;
        private BigDecimal rating;
        private Integer ratingCount;
        private Integer studentCount;
        private String instructorId;
        private String instructorName;
        private String learningObjectives;
        private String prerequisites;
        private Boolean isRequired;
        private Integer validityDays;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private LocalDateTime publishTime;
        private Integer chapterCount;
        private Integer publishedChapterCount;
        private List<CourseChapterDTO.Response> chapters;

        // 状态文本转换
        public String getStatusText() {
            if (status == null) return "未知";
            switch (status) {
                case 0: return "草稿";
                case 1: return "已发布";
                case 2: return "已下架";
                default: return "未知";
            }
        }

        // 难度文本转换
        public String getDifficultyText() {
            if (difficultyLevel == null) return "未设置";
            switch (difficultyLevel) {
                case 1: return "初级";
                case 2: return "中级";
                case 3: return "高级";
                default: return "未设置";
            }
        }
    }

    /**
     * 课程列表项DTO（简化版本，用于列表显示）
     */
    @Data
    public static class ListItem {
        private String id;
        private String title;
        private String description;
        private String coverImageUrl;
        private String category;
        private Integer status;
        private String statusText;
        private Integer difficultyLevel;
        private String difficultyText;
        private Integer estimatedDuration;
        private BigDecimal rating;
        private Integer studentCount;
        private String instructorName;
        private Boolean isRequired;
        private LocalDateTime publishTime;
        private Integer chapterCount;

        // 状态文本转换
        public String getStatusText() {
            if (status == null) return "未知";
            switch (status) {
                case 0: return "草稿";
                case 1: return "已发布";
                case 2: return "已下架";
                default: return "未知";
            }
        }

        // 难度文本转换
        public String getDifficultyText() {
            if (difficultyLevel == null) return "未设置";
            switch (difficultyLevel) {
                case 1: return "初级";
                case 2: return "中级";
                case 3: return "高级";
                default: return "未设置";
            }
        }
    }

    /**
     * 课程搜索请求DTO
     */
    @Data
    public static class SearchRequest {
        private String keyword;
        private String category;
        private Integer difficultyLevel;
        private Boolean isRequired;
        private String sortBy = "createTime"; // createTime, rating, studentCount
        private String sortOrder = "desc"; // asc, desc
        
        @Min(value = 0, message = "页码不能小于0")
        private Integer page = 0;
        
        @Min(value = 1, message = "页面大小必须大于0")
        @Max(value = 100, message = "页面大小不能超过100")
        private Integer size = 20;
    }

    /**
     * 课程统计DTO
     */
    @Data
    public static class Statistics {
        private Long totalCourses;
        private Long publishedCourses;
        private Long draftCourses;
        private Long unpublishedCourses;
        private Double averageRating;
        private Long totalStudents;
        private List<CategoryStatistics> categoryStatistics;
    }

    /**
     * 分类统计DTO
     */
    @Data
    public static class CategoryStatistics {
        private String category;
        private Long courseCount;
        private Long studentCount;
        private Double averageRating;
    }
}