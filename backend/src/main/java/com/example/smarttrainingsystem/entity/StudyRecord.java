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
 * 用于记录用户的课程学习情况
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
     * 章节ID
     */
    @Column(name = "chapter_id", length = 36)
    private String chapterId;

    /**
     * 学习状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.NOT_STARTED;

    /**
     * 学习进度（百分比）
     */
    @Column(name = "progress", nullable = false)
    private Integer progress = 0;

    /**
     * 学习进度（百分比） - 兼容字段
     */
    @Column(name = "progress_percent")
    private Integer progressPercent = 0;

    /**
     * 学习时长（分钟）
     */
    @Column(name = "study_time", nullable = false)
    private Integer studyTime = 0;

    /**
     * 学习时长（分钟） - 兼容字段
     */
    @Column(name = "study_duration")
    private Integer studyDuration = 0;

    /**
     * 最后学习位置
     */
    @Column(name = "last_position", length = 100)
    private String lastPosition;

    /**
     * 开始学习时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 最后学习时间
     */
    @Column(name = "last_study_at")
    private LocalDateTime lastStudyAt;

    /**
     * 最后学习时间 - 兼容字段
     */
    @Column(name = "last_study_time")
    private LocalDateTime lastStudyTime;

    /**
     * 完成时间
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 完成时间 - 兼容字段
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
     * 学习状态枚举 (统一使用 Status)
     */
    public enum Status {
        NOT_STARTED("未开始"),
        IN_PROGRESS("学习中"),
        COMPLETED("已完成"),
        SUSPENDED("已暂停"),
        PAUSED("暂停"); // 兼容旧枚举值

        private final String description;

        Status(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 获取有效的学习进度
     */
    public Integer getEffectiveProgress() {
        return progress != null ? progress : (progressPercent != null ? progressPercent : 0);
    }

    /**
     * 获取有效的学习时长
     */
    public Integer getEffectiveStudyTime() {
        return studyTime != null ? studyTime : (studyDuration != null ? studyDuration : 0);
    }

    /**
     * 获取有效的最后学习时间
     */
    public LocalDateTime getEffectiveLastStudyTime() {
        return lastStudyAt != null ? lastStudyAt : lastStudyTime;
    }

    /**
     * 获取有效的完成时间
     */
    public LocalDateTime getEffectiveCompletionTime() {
        return completedAt != null ? completedAt : completionTime;
    }

    /**
     * 开始学习
     */
    public void startStudy() {
        if (this.status == Status.NOT_STARTED) {
            this.status = Status.IN_PROGRESS;
            this.startTime = LocalDateTime.now();
        }
        this.setEffectiveLastStudyTime(LocalDateTime.now());
    }

    /**
     * 更新学习进度
     */
    public void updateProgress(Integer newProgress) {
        this.setEffectiveProgress(newProgress);
        this.setEffectiveLastStudyTime(LocalDateTime.now());

        if (newProgress >= 100) {
            this.status = Status.COMPLETED;
            this.setEffectiveCompletionTime(LocalDateTime.now());
        } else if (this.status == Status.NOT_STARTED) {
            this.status = Status.IN_PROGRESS;
        }
    }

    /**
     * 更新学习进度（重载方法）
     */
    public void updateProgress(Integer newProgress, String position) {
        this.updateProgress(newProgress);
        this.lastPosition = position;
    }

    /**
     * 增加学习时长
     */
    public void addStudyTime(Integer minutes) {
        int currentTime = getEffectiveStudyTime();
        this.setEffectiveStudyTime(currentTime + minutes);
        this.setEffectiveLastStudyTime(LocalDateTime.now());
    }

    /**
     * 增加学习时长（兼容方法）
     */
    public void addStudyDuration(Integer minutes) {
        addStudyTime(minutes);
    }

    /**
     * 标记为完成
     */
    public void markAsCompleted() {
        this.status = Status.COMPLETED;
        this.setEffectiveProgress(100);
        this.setEffectiveCompletionTime(LocalDateTime.now());
        this.setEffectiveLastStudyTime(LocalDateTime.now());
    }

    /**
     * 完成学习（兼容方法）
     */
    public void completeStudy() {
        markAsCompleted();
    }

    /**
     * 暂停学习
     */
    public void pauseStudy() {
        if (this.status == Status.IN_PROGRESS) {
            this.status = Status.PAUSED;
        }
    }

    /**
     * 恢复学习
     */
    public void resumeStudy() {
        if (this.status == Status.PAUSED || this.status == Status.SUSPENDED) {
            this.status = Status.IN_PROGRESS;
            this.setEffectiveLastStudyTime(LocalDateTime.now());
        }
    }

    /**
     * 检查是否已完成
     */
    public boolean isCompleted() {
        return Status.COMPLETED.equals(this.status);
    }

    /**
     * 检查是否正在学习
     */
    public boolean isInProgress() {
        return Status.IN_PROGRESS.equals(this.status);
    }

    /**
     * 获取完成率
     */
    public double getCompletionRate() {
        return getEffectiveProgress() / 100.0;
    }

    // ============ 私有设置方法，统一字段更新 ============

    private void setEffectiveProgress(Integer progress) {
        this.progress = progress;
        this.progressPercent = progress;
    }

    private void setEffectiveStudyTime(Integer time) {
        this.studyTime = time;
        this.studyDuration = time;
    }

    private void setEffectiveLastStudyTime(LocalDateTime time) {
        this.lastStudyAt = time;
        this.lastStudyTime = time;
    }

    private void setEffectiveCompletionTime(LocalDateTime time) {
        this.completedAt = time;
        this.completionTime = time;
    }

    // ============ Getter/Setter 覆盖以保证兼容性 ============

    public Integer getProgressPercent() {
        return getEffectiveProgress();
    }

    public void setProgressPercent(Integer progressPercent) {
        setEffectiveProgress(progressPercent);
    }

    public Integer getStudyDuration() {
        return getEffectiveStudyTime();
    }

    public void setStudyDuration(Integer studyDuration) {
        setEffectiveStudyTime(studyDuration);
    }

    public LocalDateTime getLastStudyTime() {
        return getEffectiveLastStudyTime();
    }

    public void setLastStudyTime(LocalDateTime lastStudyTime) {
        setEffectiveLastStudyTime(lastStudyTime);
    }

    public LocalDateTime getCompletionTime() {
        return getEffectiveCompletionTime();
    }

    public void setCompletionTime(LocalDateTime completionTime) {
        setEffectiveCompletionTime(completionTime);
    }

    // ============ 兼容旧的枚举访问 ============

    /**
     * 获取 StudyStatus（兼容旧代码）
     */
    public Status getStudyStatus() {
        return this.status;
    }

    /**
     * 设置 StudyStatus（兼容旧代码）
     */
    public void setStudyStatus(Status status) {
        this.status = status;
    }
}