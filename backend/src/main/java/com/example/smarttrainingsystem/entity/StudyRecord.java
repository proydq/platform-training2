package com.example.smarttrainingsystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学习记录实体
 * 记录用户的学习行为和进度
 * 
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Entity
@Table(name = "t_study_record")
@Data
@EqualsAndHashCode(callSuper = false)
public class StudyRecord {

    /**
     * 主键ID
     */
    @Id
    @Column(name = "id", length = 36)
    private String id = UUID.randomUUID().toString();

    /**
     * 用户ID
     */
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    /**
     * 课程ID
     */
    @Column(name = "course_id", length = 36, nullable = false)
    private String courseId;

    /**
     * 章节ID (可选，用于记录章节级别的学习)
     */
    @Column(name = "chapter_id", length = 36)
    private String chapterId;

    /**
     * 学习状态
     * NOT_STARTED - 未开始
     * IN_PROGRESS - 学习中
     * COMPLETED - 已完成
     * PAUSED - 暂停
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "study_status", length = 20, nullable = false)
    private StudyStatus studyStatus = StudyStatus.NOT_STARTED;

    /**
     * 学习进度百分比 (0-100)
     */
    @Column(name = "progress_percent", nullable = false)
    private Integer progressPercent = 0;

    /**
     * 学习时长(分钟)
     */
    @Column(name = "study_duration", nullable = false)
    private Integer studyDuration = 0;

    /**
     * 最后学习位置 (视频时间戳或章节位置)
     */
    @Column(name = "last_position", length = 100)
    private String lastPosition;

    /**
     * 学习开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 最后学习时间
     */
    @Column(name = "last_study_time")
    private LocalDateTime lastStudyTime;

    /**
     * 完成时间
     */
    @Column(name = "completion_time")
    private LocalDateTime completionTime;

    /**
     * 学习笔记
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * 学习评分 (1-5星)
     */
    @Column(name = "rating")
    private Integer rating;

    /**
     * 学习评价
     */
    @Column(name = "review", length = 500)
    private String review;

    /**
     * 是否收藏
     */
    @Column(name = "is_favorited", nullable = false)
    private Boolean isFavorited = false;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 用户实体关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 课程实体关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    private Course course;

    /**
     * 章节实体关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", insertable = false, updatable = false)
    private CourseChapter chapter;

    /**
     * 学习状态枚举
     */
    public enum StudyStatus {
        NOT_STARTED("未开始"),
        IN_PROGRESS("学习中"),
        COMPLETED("已完成"),
        PAUSED("暂停");

        private final String description;

        StudyStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 开始学习
     */
    public void startStudy() {
        if (this.studyStatus == StudyStatus.NOT_STARTED) {
            this.studyStatus = StudyStatus.IN_PROGRESS;
            this.startTime = LocalDateTime.now();
        }
        this.lastStudyTime = LocalDateTime.now();
    }

    /**
     * 更新学习进度
     */
    public void updateProgress(int progressPercent, String position) {
        this.progressPercent = Math.max(0, Math.min(100, progressPercent));
        this.lastPosition = position;
        this.lastStudyTime = LocalDateTime.now();
        
        // 自动更新状态
        if (this.studyStatus == StudyStatus.NOT_STARTED) {
            this.studyStatus = StudyStatus.IN_PROGRESS;
            this.startTime = LocalDateTime.now();
        }
        
        // 完成学习
        if (progressPercent >= 100) {
            this.studyStatus = StudyStatus.COMPLETED;
            this.completionTime = LocalDateTime.now();
        }
    }

    /**
     * 暂停学习
     */
    public void pauseStudy() {
        if (this.studyStatus == StudyStatus.IN_PROGRESS) {
            this.studyStatus = StudyStatus.PAUSED;
        }
    }

    /**
     * 恢复学习
     */
    public void resumeStudy() {
        if (this.studyStatus == StudyStatus.PAUSED) {
            this.studyStatus = StudyStatus.IN_PROGRESS;
            this.lastStudyTime = LocalDateTime.now();
        }
    }

    /**
     * 完成学习
     */
    public void completeStudy() {
        this.studyStatus = StudyStatus.COMPLETED;
        this.progressPercent = 100;
        this.completionTime = LocalDateTime.now();
        this.lastStudyTime = LocalDateTime.now();
    }

    /**
     * 添加学习时长
     */
    public void addStudyDuration(int minutes) {
        this.studyDuration += minutes;
        this.lastStudyTime = LocalDateTime.now();
    }

    /**
     * 检查是否已完成
     */
    public boolean isCompleted() {
        return this.studyStatus == StudyStatus.COMPLETED;
    }

    /**
     * 检查是否正在学习
     */
    public boolean isInProgress() {
        return this.studyStatus == StudyStatus.IN_PROGRESS;
    }
}