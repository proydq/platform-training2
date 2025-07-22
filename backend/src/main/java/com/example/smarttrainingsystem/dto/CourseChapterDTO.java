// 文件路径: backend/src/main/java/com/example/smarttrainingsystem/dto/CourseChapterDTO.java
package com.example.smarttrainingsystem.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.util.List;

/**
 * 课程章节数据传输对象
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-07-18
 */
public class CourseChapterDTO {

    /**
     * 章节创建请求DTO
     */
    @Data
    public static class CreateRequest {
        //@NotBlank(message = "课程ID不能为空")
        private String courseId;

        @NotBlank(message = "章节标题不能为空")
        @Size(max = 200, message = "章节标题长度不能超过200字符")
        private String title;

        @Size(max = 2000, message = "章节描述长度不能超过2000字符")
        private String description;

        @NotBlank(message = "章节类型不能为空")
        @Pattern(regexp = "^(video|document|audio|quiz)$", message = "章节类型必须是video、document、audio或quiz")
        private String chapterType;

        private String contentUrl;

        @Min(value = 0, message = "章节时长不能为负数")
        private Integer duration;

        @NotNull(message = "排序序号不能为空")
        @Min(value = 1, message = "排序序号必须大于0")
        private Integer sortOrder;

        private Boolean isFree = false;

        @Size(max = 2000, message = "学习要求长度不能超过2000字符")
        private String requirements;

        @Size(max = 2000, message = "学习目标长度不能超过2000字符")
        private String learningObjectives;

        private Long fileSize;

        @Size(max = 20, message = "文件格式长度不能超过20字符")
        private String fileFormat;

        private String thumbnailUrl;

        private String materialUrls; // 关联的学习资料

        private String videoUrls; // 关联的视频资料
    }

    /**
     * 章节更新请求DTO
     */
    @Data
    public static class UpdateRequest {
        // 章节ID, 用于在课程更新时定位章节
        private String id;
        @Size(max = 200, message = "章节标题长度不能超过200字符")
        private String title;

        @Size(max = 2000, message = "章节描述长度不能超过2000字符")
        private String description;

        @Pattern(regexp = "^(video|document|audio|quiz)$", message = "章节类型必须是video、document、audio或quiz")
        private String chapterType;

        private String contentUrl;

        @Min(value = 0, message = "章节时长不能为负数")
        private Integer duration;

        @Min(value = 1, message = "排序序号必须大于0")
        private Integer sortOrder;

        private Boolean isFree;

        @Size(max = 2000, message = "学习要求长度不能超过2000字符")
        private String requirements;

        @Size(max = 2000, message = "学习目标长度不能超过2000字符")
        private String learningObjectives;

        private Long fileSize;

        @Size(max = 20, message = "文件格式长度不能超过20字符")
        private String fileFormat;

        private String thumbnailUrl;

        private String materialUrls;

        private String videoUrls;

        // 章节状态: 0-草稿 1-已发布
        private Integer status;
    }

    /**
     * 章节响应DTO
     */
    @Data
    public static class Response {
        private String id;
        private String courseId;
        private String courseTitle;
        private String title;
        private String description;
        private String chapterType;
        private String chapterTypeText;
        private String contentUrl;
        private Integer duration;
        private Integer sortOrder;
        private Integer status;
        private String statusText;
        private Boolean isFree;
        private String requirements;
        private String learningObjectives;
        private Long fileSize;
        private String fileFormat;
        private String thumbnailUrl;
        private String materialUrls;
        private String videoUrls;
        private Long createTime;
        private Long updateTime;

        // 章节类型文本转换
        public String getChapterTypeText() {
            if (chapterType == null) return "未知";
            switch (chapterType.toLowerCase()) {
                case "video": return "视频";
                case "document": return "文档";
                case "audio": return "音频";
                case "quiz": return "测验";
                default: return "未知";
            }
        }

        // 状态文本转换
        public String getStatusText() {
            if (status == null) return "未知";
            switch (status) {
                case 0: return "草稿";
                case 1: return "已发布";
                default: return "未知";
            }
        }
    }

    /**
     * 章节排序请求DTO
     */
    @Data
    public static class SortRequest {
        @NotBlank(message = "章节ID不能为空")
        private String chapterId;

        @NotNull(message = "新排序序号不能为空")
        @Min(value = 1, message = "排序序号必须大于0")
        private Integer newSortOrder;
    }

    /**
     * 批量章节排序请求DTO
     */
    @Data
    public static class BatchSortRequest {
        //@NotBlank(message = "课程ID不能为空")
        private String courseId;

        @NotEmpty(message = "章节排序列表不能为空")
        private List<SortRequest> chapterSorts;
    }

    /**
     * 章节学习进度DTO
     */
    @Data
    public static class StudyProgress {
        private String chapterId;
        private String chapterTitle;
        private String chapterType;
        private Integer duration;
        private Boolean isCompleted;
        private Integer studyTime; // 已学习时长（分钟）
        private Double progress; // 学习进度百分比
        private Long lastStudyTime;
    }
}