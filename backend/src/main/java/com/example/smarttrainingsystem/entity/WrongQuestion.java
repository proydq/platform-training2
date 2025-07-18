package com.example.smarttrainingsystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 错题记录实体
 * 用于记录用户的错题信息和练习情况
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Entity
@Table(name = "t_wrong_question")
@Data
@EqualsAndHashCode(callSuper = false)
public class WrongQuestion {

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
     * 题目ID
     */
    @Column(name = "question_id", length = 36, nullable = false)
    private String questionId;

    /**
     * 考试ID（第一次出错的考试）
     */
    @Column(name = "exam_id", length = 36, nullable = false)
    private String examId;

    /**
     * 错误次数
     */
    @Column(name = "wrong_count", nullable = false)
    private Integer wrongCount = 0;

    /**
     * 练习正确次数
     */
    @Column(name = "correct_count", nullable = false)
    private Integer correctCount = 0;

    /**
     * 练习总次数
     */
    @Column(name = "practice_count", nullable = false)
    private Integer practiceCount = 0;

    /**
     * 最后一次错误答案
     */
    @Column(name = "last_user_answer", columnDefinition = "TEXT")
    private String lastUserAnswer;

    /**
     * 用户笔记
     */
    @Column(name = "user_note", columnDefinition = "TEXT")
    private String userNote;

    /**
     * 是否已掌握
     */
    @Column(name = "is_mastered", nullable = false)
    private Boolean mastered = false;

    /**
     * 最后错误时间
     */
    @Column(name = "last_wrong_at", nullable = false)
    private LocalDateTime lastWrongAt;

    /**
     * 最后练习时间
     */
    @Column(name = "last_practice_at")
    private LocalDateTime lastPracticeAt;

    /**
     * 掌握时间
     */
    @Column(name = "mastered_at")
    private LocalDateTime masteredAt;

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
     * 题目实体关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private Question question;

    /**
     * 考试实体关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", insertable = false, updatable = false)
    private Exam exam;

    /**
     * 计算正确率
     *
     * @return 正确率（百分比）
     */
    public double getAccuracyRate() {
        if (practiceCount == 0) {
            return 0.0;
        }
        return (double) correctCount / practiceCount * 100;
    }

    /**
     * 判断是否需要重点练习
     * 规则：错误次数 > 2 且正确率 < 60%
     *
     * @return 是否需要重点练习
     */
    public boolean needsIntensivePractice() {
        return wrongCount > 2 && getAccuracyRate() < 60.0;
    }

    /**
     * 获取掌握状态描述
     *
     * @return 掌握状态
     */
    public String getMasteryStatus() {
        if (mastered) {
            return "已掌握";
        } else if (getAccuracyRate() >= 80.0 && practiceCount >= 3) {
            return "基本掌握";
        } else if (needsIntensivePractice()) {
            return "需要重点练习";
        } else {
            return "练习中";
        }
    }
}