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
 * 题目实体
 * 用于管理考试题目的内容和配置
 * 
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Entity
@Table(name = "t_question")
@Data
@EqualsAndHashCode(callSuper = false)
public class Question {

    /**
     * 主键ID
     */
    @Id
    @Column(name = "id", length = 36)
    private String id = UUID.randomUUID().toString();

    /**
     * 题目内容
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 题目类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", length = 20, nullable = false)
    private QuestionType questionType;

    /**
     * 选择题选项 (JSON格式)
     */
    @Column(name = "options", columnDefinition = "JSON")
    private String options;

    /**
     * 正确答案
     */
    @Column(name = "correct_answer", nullable = false, columnDefinition = "TEXT")
    private String correctAnswer;

    /**
     * 答案解析
     */
    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    /**
     * 题目分数
     */
    @Column(name = "score", nullable = false)
    private Integer score = 1;

    /**
     * 难度等级 (1-5)
     */
    @Column(name = "difficulty", nullable = false)
    private Integer difficulty = 1;

    /**
     * 题目分类
     */
    @Column(name = "category", length = 100)
    private String category;

    /**
     * 标签 (逗号分隔)
     */
    @Column(name = "tags", length = 500)
    private String tags;

    /**
     * 题目状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private QuestionStatus status = QuestionStatus.ACTIVE;

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
     * 题目类型枚举
     */
    public enum QuestionType {
        SINGLE_CHOICE("单选题"),
        MULTIPLE_CHOICE("多选题"),
        TRUE_FALSE("判断题"),
        FILL_BLANK("填空题"),
        ESSAY("简答题");

        private final String description;

        QuestionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 题目状态枚举
     */
    public enum QuestionStatus {
        ACTIVE("启用"),
        INACTIVE("禁用"),
        DELETED("已删除");

        private final String description;

        QuestionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 激活题目
     */
    public void activate() {
        this.status = QuestionStatus.ACTIVE;
    }

    /**
     * 禁用题目
     */
    public void deactivate() {
        this.status = QuestionStatus.INACTIVE;
    }

    /**
     * 删除题目
     */
    public void delete() {
        this.status = QuestionStatus.DELETED;
    }

    /**
     * 检查题目是否可用
     */
    public boolean isAvailable() {
        return status == QuestionStatus.ACTIVE;
    }
}