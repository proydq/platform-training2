package com.example.smarttrainingsystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学习进度统计实体
 * 用于记录用户每日/每周/每月的学习统计数据
 * 
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Entity
@Table(name = "t_study_progress")
@Data
@EqualsAndHashCode(callSuper = false)
public class StudyProgress {

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
     * 统计日期
     */
    @Column(name = "progress_date", nullable = false)
    private LocalDate progressDate;

    /**
     * 统计类型
     * DAILY - 每日统计
     * WEEKLY - 每周统计
     * MONTHLY - 每月统计
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "progress_type", length = 20, nullable = false)
    private ProgressType progressType = ProgressType.DAILY;

    /**
     * 学习课程数量
     */
    @Column(name = "courses_studied", nullable = false)
    private Integer coursesStudied = 0;

    /**
     * 完成课程数量
     */
    @Column(name = "courses_completed", nullable = false)
    private Integer coursesCompleted = 0;

    /**
     * 学习时长(分钟)
     */
    @Column(name = "study_duration", nullable = false)
    private Integer studyDuration = 0;

    /**
     * 完成章节数量
     */
    @Column(name = "chapters_completed", nullable = false)
    private Integer chaptersCompleted = 0;

    /**
     * 学习活跃度分数 (0-100)
     */
    @Column(name = "activity_score", nullable = false)
    private Integer activityScore = 0;

    /**
     * 连续学习天数
     */
    @Column(name = "continuous_days", nullable = false)
    private Integer continuousDays = 0;

    /**
     * 当日是否达成学习目标
     */
    @Column(name = "goal_achieved", nullable = false)
    private Boolean goalAchieved = false;

    /**
     * 学习目标时长(分钟)
     */
    @Column(name = "goal_duration", nullable = false)
    private Integer goalDuration = 60; // 默认60分钟

    /**
     * 平均学习进度百分比
     */
    @Column(name = "avg_progress_percent", nullable = false)
    private Double avgProgressPercent = 0.0;

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
     * 进度类型枚举
     */
    public enum ProgressType {
        DAILY("每日"),
        WEEKLY("每周"),
        MONTHLY("每月");

        private final String description;

        ProgressType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 更新学习统计
     */
    public void updateStatistics(int coursesStudied, int coursesCompleted, 
                               int studyDuration, int chaptersCompleted) {
        this.coursesStudied = coursesStudied;
        this.coursesCompleted = coursesCompleted;
        this.studyDuration = studyDuration;
        this.chaptersCompleted = chaptersCompleted;
        
        // 计算活跃度分数
        this.activityScore = calculateActivityScore();
        
        // 检查是否达成目标
        this.goalAchieved = this.studyDuration >= this.goalDuration;
        
        // 计算平均进度
        if (this.coursesStudied > 0) {
            this.avgProgressPercent = (double) this.coursesCompleted / this.coursesStudied * 100;
        }
    }

    /**
     * 计算活跃度分数
     * 基于学习时长、完成课程数、章节数等综合计算
     */
    private int calculateActivityScore() {
        int score = 0;
        
        // 学习时长贡献 (最高40分)
        score += Math.min(40, this.studyDuration / 3);
        
        // 完成课程贡献 (最高30分)
        score += Math.min(30, this.coursesCompleted * 10);
        
        // 完成章节贡献 (最高20分)
        score += Math.min(20, this.chaptersCompleted * 2);
        
        // 目标达成奖励 (10分)
        if (this.goalAchieved) {
            score += 10;
        }
        
        return Math.min(100, score);
    }

    /**
     * 增加连续学习天数
     */
    public void incrementContinuousDays() {
        this.continuousDays++;
    }

    /**
     * 重置连续学习天数
     */
    public void resetContinuousDays() {
        this.continuousDays = 0;
    }

    /**
     * 设置学习目标
     */
    public void setGoalDuration(int goalDuration) {
        this.goalDuration = goalDuration;
        this.goalAchieved = this.studyDuration >= this.goalDuration;
    }

    /**
     * 检查是否为高活跃度
     */
    public boolean isHighActivity() {
        return this.activityScore >= 80;
    }

    /**
     * 检查是否为中等活跃度
     */
    public boolean isMediumActivity() {
        return this.activityScore >= 50 && this.activityScore < 80;
    }

    /**
     * 检查是否为低活跃度
     */
    public boolean isLowActivity() {
        return this.activityScore < 50;
    }

    /**
     * 获取学习效率百分比
     */
    public double getEfficiencyPercent() {
        if (this.studyDuration == 0) {
            return 0.0;
        }
        return (double) this.chaptersCompleted / (this.studyDuration / 30.0) * 100;
    }

    /**
     * 获取目标完成度
     */
    public double getGoalCompletionPercent() {
        if (this.goalDuration == 0) {
            return 100.0;
        }
        return Math.min(100.0, (double) this.studyDuration / this.goalDuration * 100);
    }
}