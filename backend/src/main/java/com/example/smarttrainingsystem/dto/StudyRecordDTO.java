package com.example.smarttrainingsystem.dto;

import com.example.smarttrainingsystem.entity.StudyRecord;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 学习记录数据传输对象
 * 
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
public class StudyRecordDTO {

    /**
     * 学习记录请求对象
     */
    @Data
    public static class CreateRequest {
        @NotBlank(message = "课程ID不能为空")
        private String courseId;
        
        private String chapterId;
        
        @Size(max = 1000, message = "学习笔记长度不能超过1000字符")
        private String notes;
    }

    /**
     * 学习记录更新请求对象
     */
    @Data
    public static class UpdateRequest {
        @Min(value = 0, message = "学习进度不能小于0")
        @Max(value = 100, message = "学习进度不能大于100")
        private Integer progressPercent;
        
        @Size(max = 100, message = "学习位置长度不能超过100字符")
        private String lastPosition;
        
        @Min(value = 0, message = "学习时长不能小于0")
        private Integer studyDuration;
        
        @Size(max = 1000, message = "学习笔记长度不能超过1000字符")
        private String notes;
        
        @Min(value = 1, message = "评分不能小于1")
        @Max(value = 5, message = "评分不能大于5")
        private Integer rating;
        
        @Size(max = 500, message = "评价长度不能超过500字符")
        private String review;
        
        private Boolean isFavorited;
    }

    /**
     * 学习记录响应对象
     */
    @Data
    public static class Response {
        private String id;
        private String userId;
        private String courseId;
        private String chapterId;
        private StudyRecord.StudyStatus studyStatus;
        private String studyStatusDesc;
        private Integer progressPercent;
        private Integer studyDuration;
        private String lastPosition;
        private LocalDateTime startTime;
        private LocalDateTime lastStudyTime;
        private LocalDateTime completionTime;
        private String notes;
        private Integer rating;
        private String review;
        private Boolean isFavorited;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // 关联信息
        private CourseInfo courseInfo;
        private ChapterInfo chapterInfo;
        
        /**
         * 课程信息
         */
        @Data
        public static class CourseInfo {
            private String id;
            private String title;
            private String description;
            private String coverUrl;
            private String category;
            private String difficulty;
            private Integer duration;
            private String instructor;
        }
        
        /**
         * 章节信息
         */
        @Data
        public static class ChapterInfo {
            private String id;
            private String title;
            private String description;
            private Integer chapterOrder;
            private Integer duration;
            private String videoUrl;
        }
    }

    /**
     * 学习记录统计响应对象
     */
    @Data
    public static class StatisticsResponse {
        private Long totalRecords;
        private Long completedCourses;
        private Long inProgressCourses;
        private Long totalStudyTime;
        private Double completionRate;
        private Double avgRating;
        private Long favoritedCourses;
        private LocalDateTime lastStudyTime;
        private Integer continuousDays;
        private String learningLevel;
        
        /**
         * 学习趋势数据
         */
        @Data
        public static class TrendData {
            private String date;
            private Integer studyDuration;
            private Integer coursesStudied;
            private Integer chaptersCompleted;
            private Integer activityScore;
        }
    }

    /**
     * 学习记录搜索请求对象
     */
    @Data
    public static class SearchRequest {
        private String keyword;
        private StudyRecord.StudyStatus status;
        private String courseId;
        private String category;
        private Boolean isFavorited;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer minRating;
        private Integer maxRating;
        private String sortBy = "lastStudyTime";
        private String sortOrder = "desc";
        private Integer page = 0;
        private Integer size = 20;
    }

    /**
     * 学习记录简要信息响应对象
     */
    @Data
    public static class BriefResponse {
        private String id;
        private String courseId;
        private String courseTitle;
        private String courseCover;
        private StudyRecord.StudyStatus studyStatus;
        private String studyStatusDesc;
        private Integer progressPercent;
        private Integer studyDuration;
        private LocalDateTime lastStudyTime;
        private Boolean isFavorited;
        private Integer rating;
        
        /**
         * 进度状态描述
         */
        public String getProgressDesc() {
            if (progressPercent == null) {
                return "未开始";
            }
            if (progressPercent == 0) {
                return "未开始";
            } else if (progressPercent == 100) {
                return "已完成";
            } else {
                return "进行中 " + progressPercent + "%";
            }
        }
        
        /**
         * 学习时长格式化
         */
        public String getStudyDurationFormatted() {
            if (studyDuration == null || studyDuration == 0) {
                return "0分钟";
            }
            
            int hours = studyDuration / 60;
            int minutes = studyDuration % 60;
            
            if (hours > 0) {
                return hours + "小时" + minutes + "分钟";
            } else {
                return minutes + "分钟";
            }
        }
    }

    /**
     * 学习排行榜响应对象
     */
    @Data
    public static class RankingResponse {
        private Integer ranking;
        private String userId;
        private String username;
        private String realName;
        private String avatar;
        private Long totalStudyTime;
        private Long completedCourses;
        private Integer activityScore;
        private Boolean isCurrentUser;
        
        /**
         * 学习时长格式化
         */
        public String getStudyTimeFormatted() {
            if (totalStudyTime == null || totalStudyTime == 0) {
                return "0小时";
            }
            
            long hours = totalStudyTime / 60;
            long minutes = totalStudyTime % 60;
            
            if (hours > 0) {
                return hours + "小时" + minutes + "分钟";
            } else {
                return minutes + "分钟";
            }
        }
        
        /**
         * 活跃度等级
         */
        public String getActivityLevel() {
            if (activityScore == null) {
                return "未知";
            }
            if (activityScore >= 80) {
                return "高度活跃";
            } else if (activityScore >= 50) {
                return "中等活跃";
            } else {
                return "低度活跃";
            }
        }
    }

    /**
     * 学习记录批量操作请求对象
     */
    @Data
    public static class BatchOperationRequest {
        private String[] recordIds;
        private String operation; // delete, favorite, unfavorite, archive
        private String reason;
    }

    /**
     * 学习记录导出请求对象
     */
    @Data
    public static class ExportRequest {
        private String userId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private StudyRecord.StudyStatus status;
        private String format = "excel"; // excel, csv, pdf
        private Boolean includeDetails = false;
    }
}