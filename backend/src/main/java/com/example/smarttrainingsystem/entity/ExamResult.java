package com.example.smarttrainingsystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 考试结果实体
 * 用于记录用户的考试成绩和答题详情
 * 
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Entity
@Table(name = "t_exam_result")
@Data
@EqualsAndHashCode(callSuper = false)
public class ExamResult {

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
     * 考试ID
     */
    @Column(name = "exam_id", length = 36, nullable = false)
    private String examId;

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
     * 实际用时（分钟）
     */
    @Column(name = "duration")
    private Integer duration;

    /**
     * 得分
     */
    @Column(name = "score")
    private Integer score;

    /**
     * 总分
     */
    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    /**
     * 通过状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "pass_status", length = 20, nullable = false)
    private PassStatus passStatus = PassStatus.IN_PROGRESS;

    /**
     * 答题详情 (JSON格式)
     */
    @Column(name = "answer_details", columnDefinition = "JSON")
    private String answerDetails;

    /**
     * 正确题目数
     */
    @Column(name = "correct_count")
    private Integer correctCount = 0;

    /**
     * 错误题目数
     */
    @Column(name = "wrong_count")
    private Integer wrongCount = 0;

    /**
     * 总题目数
     */
    @Column(name = "total_count", nullable = false)
    private Integer totalCount;

    /**
     * 正确率
     */
    @Column(name = "accuracy_rate")
    private Double accuracyRate;

    /**
     * 排名
     */
    @Column(name = "ranking")
    private Integer ranking;

    /**
     * 是否作弊
     */
    @Column(name = "is_cheating", nullable = false)
    private Boolean isCheating = false;

    /**
     * 异常行为记录
     */
    @Column(name = "abnormal_behavior", columnDefinition = "TEXT")
    private String abnormalBehavior;

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
     * 考试实体关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", insertable = false, updatable = false)
    private Exam exam;

    /**
     * 通过状态枚举
     */
    public enum PassStatus {
        IN_PROGRESS("进行中"),
        PASS("通过"),
        FAIL("未通过"),
        TIMEOUT("超时"),
        CHEATING("作弊");

        private final String description;

        PassStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 开始考试
     */
    public void startExam() {
        this.startTime = LocalDateTime.now();
        this.passStatus = PassStatus.IN_PROGRESS;
    }

    /**
     * 完成考试
     */
    public void finishExam(Integer score, Integer totalScore, Integer passScore) {
        this.endTime = LocalDateTime.now();
        this.score = score;
        this.totalScore = totalScore;
        
        // 计算用时
        if (startTime != null && endTime != null) {
            this.duration = (int) java.time.Duration.between(startTime, endTime).toMinutes();
        }
        
        // 计算正确率
        if (totalCount > 0) {
            this.accuracyRate = (double) correctCount / totalCount * 100;
        }
        
        // 判断是否通过
        if (isCheating) {
            this.passStatus = PassStatus.CHEATING;
        } else if (score >= passScore) {
            this.passStatus = PassStatus.PASS;
        } else {
            this.passStatus = PassStatus.FAIL;
        }
    }

    /**
     * 超时处理
     */
    public void timeout() {
        this.endTime = LocalDateTime.now();
        this.passStatus = PassStatus.TIMEOUT;
    }

    /**
     * 标记作弊
     */
    public void markCheating(String reason) {
        this.isCheating = true;
        this.passStatus = PassStatus.CHEATING;
        this.abnormalBehavior = reason;
    }

    /**
     * 更新答题统计
     */
    public void updateAnswerStats(Integer correct, Integer wrong, Integer total) {
        this.correctCount = correct;
        this.wrongCount = wrong;
        this.totalCount = total;
        
        if (total > 0) {
            this.accuracyRate = (double) correct / total * 100;
        }
    }
}