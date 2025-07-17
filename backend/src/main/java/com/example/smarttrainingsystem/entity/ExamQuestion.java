package com.example.smarttrainingsystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 考试题目关联实体
 * 用于记录考试中包含的题目及其顺序
 * 
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Entity
@Table(name = "t_exam_question")
@Data
@EqualsAndHashCode(callSuper = false)
public class ExamQuestion {

    /**
     * 主键ID
     */
    @Id
    @Column(name = "id", length = 36)
    private String id = UUID.randomUUID().toString();

    /**
     * 考试ID
     */
    @Column(name = "exam_id", length = 36, nullable = false)
    private String examId;

    /**
     * 题目ID
     */
    @Column(name = "question_id", length = 36, nullable = false)
    private String questionId;

    /**
     * 题目在试卷中的顺序
     */
    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    /**
     * 题目分数
     */
    @Column(name = "score", nullable = false)
    private Integer score;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 考试实体关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", insertable = false, updatable = false)
    private Exam exam;

    /**
     * 题目实体关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private Question question;
}