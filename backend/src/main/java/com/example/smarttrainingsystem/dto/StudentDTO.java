package com.example.smarttrainingsystem.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学员管理相关DTO类
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
public class StudentDTO {

    /**
     * 学员列表项DTO
     */
    @Data
    public static class ListItem {
        private String userId;
        private String username;
        private String nickname;
        private String email;
        private String phone;
        private Boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime lastLoginAt;
        private Integer enrolledCourses;
        private Integer completedCourses;
        private String status;
    }

    /**
     * 学员详情DTO
     */
    @Data
    public static class Detail {
        private String userId;
        private String username;
        private String nickname;
        private String email;
        private String phone;
        private String gender;
        private String avatar;
        private Boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime lastLoginAt;
        private StudyStatistics statistics;
    }

    /**
     * 学员创建请求DTO
     */
    @Data
    public static class CreateRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度必须在3-20字符之间")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
        private String username;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度必须在6-20字符之间")
        private String password;

        @NotBlank(message = "昵称不能为空")
        @Size(max = 50, message = "昵称长度不能超过50字符")
        private String nickname;

        @Email(message = "邮箱格式不正确")
        private String email;

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;

        @Pattern(regexp = "^(MALE|FEMALE|UNKNOWN)$", message = "性别值不正确")
        private String gender;

        private String avatar;
    }

    /**
     * 学员更新请求DTO
     */
    @Data
    public static class UpdateRequest {
        @Size(max = 50, message = "昵称长度不能超过50字符")
        private String nickname;

        @Email(message = "邮箱格式不正确")
        private String email;

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;

        @Pattern(regexp = "^(MALE|FEMALE|UNKNOWN)$", message = "性别值不正确")
        private String gender;

        private String avatar;
    }

    /**
     * 批量操作请求DTO
     */
    @Data
    public static class BatchOperationRequest {
        @NotEmpty(message = "学员ID列表不能为空")
        private List<String> studentIds;

        @NotBlank(message = "操作类型不能为空")
        @Pattern(regexp = "^(activate|deactivate|delete|reset_password)$", 
                 message = "操作类型必须是activate、deactivate、delete或reset_password")
        private String operation;

        private String reason;
    }

    /**
     * 学习统计DTO
     */
    @Data
    public static class StudyStatistics {
        private Integer enrolledCourses;
        private Integer completedCourses;
        private Integer totalExams;
        private Integer passedExams;
        private Integer totalStudyHours;
        private Double averageScore;
        private Double completionRate;
        private Double passRate;
    }

    /**
     * 课程进度DTO
     */
    @Data
    public static class CourseProgress {
        private String courseId;
        private String courseTitle;
        private String courseCover;
        private Integer progress;
        private String status;
        private Integer studyTime;
        private LocalDateTime lastStudyAt;
    }

    /**
     * 考试记录DTO
     */
    @Data
    public static class ExamRecord {
        private String examId;
        private String examTitle;
        private Integer score;
        private Integer totalScore;
        private String passStatus;
        private Integer duration;
        private LocalDateTime examTime;
    }

    /**
     * 通知请求DTO
     */
    @Data
    public static class NotificationRequest {
        @NotBlank(message = "通知标题不能为空")
        @Size(max = 100, message = "通知标题长度不能超过100字符")
        private String title;

        @NotBlank(message = "通知内容不能为空")
        @Size(max = 1000, message = "通知内容长度不能超过1000字符")
        private String content;

        @NotBlank(message = "通知类型不能为空")
        @Pattern(regexp = "^(SYSTEM|COURSE|EXAM|REMINDER)$", 
                 message = "通知类型必须是SYSTEM、COURSE、EXAM或REMINDER")
        private String type;
    }

    /**
     * 批量通知请求DTO
     */
    @Data
    public static class BatchNotificationRequest {
        @NotEmpty(message = "学员ID列表不能为空")
        private List<String> studentIds;

        @NotBlank(message = "通知标题不能为空")
        @Size(max = 100, message = "通知标题长度不能超过100字符")
        private String title;

        @NotBlank(message = "通知内容不能为空")
        @Size(max = 1000, message = "通知内容长度不能超过1000字符")
        private String content;

        @NotBlank(message = "通知类型不能为空")
        @Pattern(regexp = "^(SYSTEM|COURSE|EXAM|REMINDER)$", 
                 message = "通知类型必须是SYSTEM、COURSE、EXAM或REMINDER")
        private String type;
    }

    /**
     * 导入请求DTO
     */
    @Data
    public static class ImportRequest {
        @NotBlank(message = "文件路径不能为空")
        private String filePath;

        private Boolean skipDuplicates = true;
        private Boolean sendWelcomeEmail = false;
        private String defaultPassword = "123456";
    }

    /**
     * 统计概览DTO
     */
    @Data
    public static class OverviewStatistics {
        private Integer totalStudents;
        private Integer activeStudents;
        private Integer newStudents;
        private Integer completedStudents;
        private Double activeRate;
        private Double completionRate;
    }

    /**
     * 活跃度报告DTO
     */
    @Data
    public static class ActivityReport {
        private LocalDate date;
        private Integer activeCount;
        private Integer studyHours;
        private Integer completedLessons;
        private Double engagementRate;
    }

    /**
     * 学员搜索请求DTO
     */
    @Data
    public static class SearchRequest {
        private String keyword;
        private String status; // active, inactive, all
        private String role; // STUDENT, TEACHER, ADMIN
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String sortBy = "createdAt";
        private String sortOrder = "desc";
        private Integer page = 0;
        private Integer size = 20;
    }

    /**
     * 学员筛选DTO
     */
    @Data
    public static class FilterRequest {
        private List<String> courseIds;
        private List<String> examIds;
        private Double minScore;
        private Double maxScore;
        private Integer minStudyHours;
        private Integer maxStudyHours;
        private String completionStatus;
        private String engagementLevel;
    }

    /**
     * 学员分组DTO
     */
    @Data
    public static class StudentGroup {
        private String groupId;
        private String groupName;
        private String description;
        private List<String> studentIds;
        private LocalDateTime createdAt;
        private String createdBy;
    }

    /**
     * 学习路径DTO
     */
    @Data
    public static class LearningPath {
        private String pathId;
        private String pathName;
        private List<String> courseIds;
        private Integer totalLessons;
        private Integer completedLessons;
        private Double progress;
        private String status;
        private LocalDateTime startedAt;
        private LocalDateTime estimatedCompletion;
    }

    /**
     * 成绩分析DTO
     */
    @Data
    public static class GradeAnalysis {
        private String subjectArea;
        private Double averageScore;
        private Double highestScore;
        private Double lowestScore;
        private Integer totalAttempts;
        private Double improvementRate;
        private List<String> weakAreas;
        private List<String> strongAreas;
    }

    /**
     * 学习建议DTO
     */
    @Data
    public static class StudyRecommendation {
        private String recommendationType;
        private String title;
        private String description;
        private List<String> recommendedCourses;
        private List<String> recommendedExams;
        private Integer priority;
        private String reason;
    }
}