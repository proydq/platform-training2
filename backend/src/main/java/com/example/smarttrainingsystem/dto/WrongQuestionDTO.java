package com.example.smarttrainingsystem.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 错题相关DTO类
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
public class WrongQuestionDTO {

    /**
     * 错题列表项DTO
     */
    @Data
    public static class ListItem {
        private String questionId;
        private String content;
        private String questionType;
        private Integer difficulty;
        private String category;
        private Integer wrongCount;
        private Integer correctCount;
        private Integer practiceCount;
        private Boolean mastered;
        private Double accuracyRate;
        private String masteryStatus;
        private LocalDateTime lastWrongAt;
        private LocalDateTime lastPracticeAt;
        private LocalDateTime masteredAt;
    }

    /**
     * 错题详情DTO
     */
    @Data
    public static class Detail {
        private String questionId;
        private String content;
        private String questionType;
        private List<Map<String, String>> options;
        private String correctAnswer;
        private String explanation;
        private Integer difficulty;
        private String category;
        private String tags;
        private Integer wrongCount;
        private Integer correctCount;
        private Integer practiceCount;
        private String lastUserAnswer;
        private String userNote;
        private Boolean mastered;
        private Double accuracyRate;
        private String masteryStatus;
        private LocalDateTime lastWrongAt;
        private LocalDateTime lastPracticeAt;
        private LocalDateTime masteredAt;
    }

    /**
     * 错题统计DTO
     */
    @Data
    public static class Statistics {
        private Long totalCount;
        private Long masteredCount;
        private Long unmasteredCount;
        private Double masteryRate;
        private Map<Integer, Long> difficultyStats;
        private Map<String, Long> categoryStats;
        private List<TrendData> trendData;
        
        public Double getMasteryRate() {
            if (totalCount == 0) return 0.0;
            return (double) masteredCount / totalCount * 100;
        }
    }

    /**
     * 趋势数据DTO
     */
    @Data
    public static class TrendData {
        private String month;
        private Long count;
    }

    /**
     * 错题练习DTO
     */
    @Data
    public static class Practice {
        private String questionId;
        private String content;
        private String questionType;
        private List<Map<String, String>> options;
        private Integer score;
        private Integer wrongCount;
        private String lastUserAnswer;
    }

    /**
     * 练习结果DTO
     */
    @Data
    public static class PracticeResult {
        private Integer totalCount;
        private Integer correctCount;
        private Integer wrongCount;
        private Double accuracyRate;
        private List<PracticeAnswer> answerDetails;
    }

    /**
     * 练习答案详情DTO
     */
    @Data
    public static class PracticeAnswer {
        private String questionId;
        private String questionContent;
        private String userAnswer;
        private String correctAnswer;
        private Boolean isCorrect;
        private String explanation;
    }

    /**
     * 错题搜索请求DTO
     */
    @Data
    public static class SearchRequest {
        private String keyword;
        private Integer difficulty;
        private String category;
        private String status; // all, mastered, unmastered, intensive
        private String sortBy = "wrongCount";
        private String sortOrder = "desc";
        private Integer page = 0;
        private Integer size = 20;
    }

    /**
     * 错题练习请求DTO
     */
    @Data
    public static class PracticeRequest {
        private Integer count = 10;
        private Integer difficulty;
        private String category;
        private Boolean onlyUnmastered = true;
        private String mode = "random"; // random, difficulty, recent
    }

    /**
     * 错题导出请求DTO
     */
    @Data
    public static class ExportRequest {
        private String format = "pdf"; // pdf, excel, word
        private Integer difficulty;
        private String category;
        private Boolean includeExplanation = true;
        private Boolean includeUserNote = true;
        private String sortBy = "category";
    }

    /**
     * 笔记添加请求DTO
     */
    @Data
    public static class NoteRequest {
        private String questionId;
        private String note;
    }

    /**
     * 批量操作请求DTO
     */
    @Data
    public static class BatchOperationRequest {
        private List<String> questionIds;
        private String operation; // mark_mastered, delete, add_note
        private String note; // 用于批量添加笔记
    }

    /**
     * 错题分析DTO
     */
    @Data
    public static class Analysis {
        private WeakPoint weakestArea;
        private List<String> recommendedActions;
        private Double overallProgress;
        private Integer practiceGoal;
        private String studyPlan;
    }

    /**
     * 薄弱点DTO
     */
    @Data
    public static class WeakPoint {
        private String category;
        private Integer difficulty;
        private Long wrongCount;
        private Double errorRate;
        private String suggestion;
    }

    /**
     * 学习计划DTO
     */
    @Data
    public static class StudyPlan {
        private List<StudyTask> dailyTasks;
        private Integer estimatedDays;
        private String goal;
    }

    /**
     * 学习任务DTO
     */
    @Data
    public static class StudyTask {
        private String taskType; // review, practice, new_questions
        private String description;
        private Integer questionCount;
        private List<String> categories;
        private Integer difficulty;
        private Integer estimatedMinutes;
    }

    /**
     * 错题复习计划DTO
     */
    @Data
    public static class ReviewPlan {
        private String planId;
        private String userId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer totalQuestions;
        private Integer completedQuestions;
        private Double progress;
        private List<ReviewSession> sessions;
    }

    /**
     * 复习会话DTO
     */
    @Data
    public static class ReviewSession {
        private String sessionId;
        private LocalDateTime scheduledTime;
        private List<String> questionIds;
        private Integer duration;
        private String status; // pending, completed, skipped
        private Double accuracy;
    }
}