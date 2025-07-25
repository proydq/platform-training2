package com.example.smarttrainingsystem.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

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

        //@NotBlank(message = "讲师ID不能为空")
        private String instructorId;

        @Size(max = 100, message = "讲师姓名长度不能超过100字符")
        private String instructorName;

        @DecimalMin(value = "0.0", message = "课程价格不能为负数")
        private BigDecimal price = BigDecimal.ZERO;

        @Min(value = 1, message = "难度级别必须在1-5之间")
        @Max(value = 5, message = "难度级别必须在1-5之间")
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

        // 🔧 兼容旧格式
        @Size(max = 2000, message = "学习资料URL长度不能超过2000字符")
        private String materialUrls;

        @Size(max = 2000, message = "视频资料URL长度不能超过2000字符")
        private String videoUrls;

        // 🔧 新格式：包含文件名的资料信息
        private List<MaterialInfo> materials;
        private List<VideoInfo> videos;

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

        // 与创建课程保持一致，允许1-5级难度
        @Min(value = 1, message = "难度级别必须在1-5之间")
        @Max(value = 5, message = "难度级别必须在1-5之间")
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

        // 🔧 兼容旧格式
        @Size(max = 2000, message = "学习资料URL长度不能超过2000字符")
        private String materialUrls;

        @Size(max = 2000, message = "视频资料URL长度不能超过2000字符")
        private String videoUrls;

        // 🔧 新格式：包含文件名的资料信息
        private List<MaterialInfo> materials;
        private List<VideoInfo> videos;

        @Valid
        private List<CourseChapterDTO.UpdateRequest> chapters;
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

        // 🔧 兼容旧格式
        private String materialUrls;
        private String materialNames;
        private String videoUrls;
        private String videoNames;

        // 🔧 新格式：包含文件名的资料信息
        private List<MaterialInfo> materialList;
        private List<VideoInfo> videoList;

        private List<CourseChapterDTO.Response> chapters;

        // 统计信息
        @JsonProperty("chapterCount")
        private Integer totalChapters;
        private Integer totalDuration;

        private Long createTime;
        private Long updateTime;
        private String createBy;
        private String updateBy;
    }

    /**
     * 课程列表项DTO
     */
    @Data
    public static class ListItem {
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

        // 🔧 简化的材料信息
        private List<MaterialInfo> materialList;
        private List<VideoInfo> videoList;

        // 统计信息
        @JsonProperty("chapterCount")
        private Integer totalChapters;
        private Integer totalDuration;

        private Long createTime;
    }

    /**
     * 🔧 学习资料信息DTO
     */
    @Data
    public static class MaterialInfo {
        @NotBlank(message = "资料URL不能为空")
        private String url;

        @NotBlank(message = "资料名称不能为空")
        @Size(min = 1, max = 200, message = "资料名称长度在1到200个字符之间")
        private String name;

        private String originalName; // 兼容字段
        private Long size; // 文件大小（字节）
        private String contentType; // 文件类型
        private String description; // 资料描述

        public MaterialInfo() {}

        public MaterialInfo(String url, String name) {
            this.url = url;
            this.name = name;
        }
    }

    /**
     * 🔧 视频资料信息DTO
     */
    @Data
    public static class VideoInfo {
        @NotBlank(message = "视频URL不能为空")
        private String url;

        @NotBlank(message = "视频名称不能为空")
        @Size(min = 1, max = 200, message = "视频名称长度在1到200个字符之间")
        private String name;

        private String originalName; // 兼容字段
        private Long size; // 文件大小（字节）
        private Integer duration; // 视频时长（秒）
        private String resolution; // 分辨率
        private String thumbnailUrl; // 缩略图URL
        private String description; // 视频描述

        public VideoInfo() {}

        public VideoInfo(String url, String name) {
            this.url = url;
            this.name = name;
        }
    }

    /**
     * 课程搜索请求DTO
     */
    @Data
    public static class SearchRequest {
        private String keyword; // 关键词搜索
        private String category; // 分类筛选
        private Integer difficultyLevel; // 难度筛选
        private String instructorId; // 讲师筛选
        private Integer status; // 状态筛选
        private BigDecimal minPrice; // 最低价格
        private BigDecimal maxPrice; // 最高价格
        private Boolean isRequired; // 是否必修
        private String sortBy = "createTime"; // 排序字段
        private String sortOrder = "desc"; // 排序方向
        private Integer page = 0; // 页码
        private Integer size = 10; // 页大小
    }

    /**
     * 课程统计信息DTO
     */
    @Data
    public static class Statistics {
        private Long totalCourses; // 总课程数
        private Long publishedCourses; // 已发布课程数
        private Long draftCourses; // 草稿课程数
        private Long unpublishedCourses; // 已下架课程数
    }
}