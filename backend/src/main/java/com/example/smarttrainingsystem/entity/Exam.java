package com.example.smarttrainingsystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 考试实体
 * 用于管理在线考试的基本信息和配置
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Entity
@Table(name = "t_exam")
@Data
@EqualsAndHashCode(callSuper = false)
public class Exam {

    /**
     * 主键ID
     */
    @Id
    @Column(name = "id", length = 36)
    private String id = UUID.randomUUID().toString();

    /**
     * 考试标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 考试描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 关联课程ID
     */
    @Column(name = "course_id", length = 36)
    private String courseId;

    /**
     * 考试时长（分钟）
     */
    @Column(name = "duration", nullable = false)
    private Integer duration;

    /**
     * 总分
     */
    @Column(name = "total_score", nullable = false)
    private Integer totalScore = 100;

    /**
     * 及格分数
     */
    @Column(name = "pass_score", nullable = false)
    private Integer passScore = 60;

    /**
     * 题目数量
     */
    @Column(name = "question_count", nullable = false)
    private Integer questionCount;

    /**
     * 考试类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", length = 20, nullable = false)
    private ExamType examType = ExamType.PRACTICE;

    /**
     * 考试状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ExamStatus status = ExamStatus.DRAFT;

    /**
     * 考试开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 考试结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 是否随机出题
     */
    @Column(name = "is_random", nullable = false)
    private Boolean isRandom = false;

    /**
     * 是否限制切换页面
     */
    @Column(name = "prevent_switch", nullable = false)
    private Boolean preventSwitch = true;

    /**
     * 允许重考次数
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 1;

    /**
     * 创建者ID
     */
    @Column(name = "created_by", length = 36, nullable = false)
    private String createdBy;

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
     * 考试类型枚举
     */
    public enum ExamType {
        PRACTICE("练习考试"),
        FORMAL("正式考试"),
        MOCK("模拟考试");

        private final String description;

        ExamType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 考试状态枚举
     */
    public enum ExamStatus {
        DRAFT("草稿"),
        PUBLISHED("已发布"),
        IN_PROGRESS("进行中"),
        ENDED("已结束"),
        CANCELLED("已取消");

        private final String description;

        ExamStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 发布考试
     */
    public void publish() {
        this.status = ExamStatus.PUBLISHED;
    }

    /**
     * 开始考试
     */
    public void start() {
        this.status = ExamStatus.IN_PROGRESS;
    }

    /**
     * 结束考试
     */
    public void end() {
        this.status = ExamStatus.ENDED;
    }

    /**
     * 取消考试
     */
    public void cancel() {
        this.status = ExamStatus.CANCELLED;
    }

    /**
     * 检查考试是否可以参加
     */
    public boolean canTakeExam() {
        LocalDateTime now = LocalDateTime.now();
        return status == ExamStatus.PUBLISHED &&
                (startTime == null || now.isAfter(startTime)) &&
                (endTime == null || now.isBefore(endTime));
    }
}