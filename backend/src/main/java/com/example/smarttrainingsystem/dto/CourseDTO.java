// 文件路径: backend/src/main/java/com/example/smarttrainingsystem/dto/CourseDTO.java
package com.example.smarttrainingsystem.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 课程数据传输对象
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-07-18
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

        @Size(max = 100, message = "课程分类长度不能超过100字符")
        private String category;

        @NotBlank(message = "讲师ID不能为空")
        private String instructorId;

        @Size(max = 100, message = "讲师姓名长度不能超过100字符")
        private String instructorName;

        @DecimalMin(value = "0.0", message = "课程价格不能为负数")
        private BigDecimal price = BigDecimal.ZERO;

        @Min(value = 1, message = "难度级别必须在1-3之间")
        @Max(value = 3, message = "难度级别必须在1-3之间")
        private Integer difficultyLevel;

        @Min(value = 0, message = "预计学时不能为负数")
        private Integer estimatedDuration;

        private Boolean isRequired = false;

        @Size(max = 2000, message = "学习目标长度不能超过2000字符")
        private String learningObjectives;

        @Size(max = 2000, message = "前置要求长度不能超过2000字符")
        private String prerequisites;

        @Size(max = 1000, message = "标签长度不能超过1000字符")
        private String tags;

        @Size(max = 500, message = "封面图片URL长度不能超过500字符")
        private String coverImageUrl;

        @Size(max = 500, message = "学习资料URL长度不能超过500字符")
        private String materialUrls;

        @Size(max = 500, message = "视频资料URL长度不能超过500字符")
        private String videoUrls;

        @Valid
        private List<CourseChapterDTO.CreateRequest> chapters;
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

        @Size(max = 100, message = "课程分类长度不能超过100字符")
        private String category;

        @Size(max = 100, message = "讲师姓名长度不能超过100字符")
        private String instructorName;

        @DecimalMin(value = "0.0", message = "课程价格不能为负数")
        private BigDecimal price;

        @Min(value = 1, message = "难度级别必须在1-3之间")
        @Max(value = 3, message = "难度级别必须在1-3之间")
        private Integer difficultyLevel;

        @Min(value = 0, message = "预计学时不能为负数")
        private Integer estimatedDuration;

        private Boolean isRequired;

        @Size(max = 2000, message = "学习目标长度不能超过2000字符")
        private String learningObjectives;

        @Size(max = 2000, message = "前置要求长度不能超过2000字符")
        private String prerequisites;

        @Size(max = 1000, message = "标签长度不能超过1000字符")
        private String tags;

        @Size(max = 500, message = "封面图片URL长度不能超过500字符")
        private String coverImageUrl;

        @Size(max = 500, message = "学习资料URL长度不能超过500字符")
        private String materialUrls;

        @Size(max = 500, message = "视频资料URL长度不能超过500字符")
        private String videoUrls;
    }

    /**
     * 课程搜索请求DTO
     */
    @Data
    public static class SearchRequest {
        private String keyword;
        private String category;
        private Integer difficultyLevel;
        private Integer status;
        private Boolean isRequired;
        private String instructorId;

        @Min(value = 0, message = "页码不能为负数")
        private Integer page = 0;

        @Min(value = 1, message = "页面大小必须大于0")
        @Max(value = 100, message = "页面大小不能超过100")
        private Integer size = 20;

        private String sortBy = "createTime";
        private String sortOrder = "desc";
    }

    /**
     * 课程详情响应DTO
     */
    @Data
    public static class Response {
        private String id;
        private String title;
        private String description;
        private String coverImageUrl;
        private String category;
        private String instructorId;
        private String instructorName;
        private BigDecimal price;
        private Integer status;
        private String statusText;
        private Integer difficultyLevel;
        private String difficultyText;
        private Integer estimatedDuration;
        private Boolean isRequired;
        private BigDecimal rating;
        private Integer ratingCount;
        private Integer studentCount;
        private Integer viewCount;
        private Long publishTime;
        private String learningObjectives;
        private String prerequisites;
        private String tags;
        private String materialUrls;
        private String videoUrls;
        private Long createTime;
        private Long updateTime;

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
     * 课程列表项DTO（用于前端列表展示）
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
        private Long publishTime;
        private Integer chapterCount;
        private String materialUrls;
        private String videoUrls;

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
     * 课程统计DTO
     */
    @Data
    public static class Statistics {
        private Long totalCourses;
        private Long publishedCourses;
        private Long draftCourses;
        private Long unpublishedCourses;
        private BigDecimal averageRating;
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
    }
}