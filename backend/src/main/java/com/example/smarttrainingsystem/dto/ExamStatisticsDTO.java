package com.example.smarttrainingsystem.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 考试统计数据传输对象
 * 用于考试统计信息的传输
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Data
public class ExamStatisticsDTO {
    
    /**
     * 考试ID
     */
    private String examId;
    
    /**
     * 考试标题
     */
    private String examTitle;
    
    /**
     * 参考人数
     */
    private Long participantCount;
    
    /**
     * 通过人数
     */
    private Long passedCount;
    
    /**
     * 通过率
     */
    private Double passRate;
    
    /**
     * 平均分
     */
    private Double averageScore;
    
    /**
     * 最高分
     */
    private Integer highestScore;
    
    /**
     * 最低分
     */
    private Integer lowestScore;
    
    /**
     * 平均用时（分钟）
     */
    private Double averageDuration;
    
    /**
     * 分数分布
     * key: 分数区间, value: 人数
     */
    private Map<String, Long> scoreDistribution;
    
    /**
     * 题目正确率统计
     */
    private List<QuestionStatisticsDTO> questionStatistics;
    
    /**
     * 难度分析
     */
    private Map<String, Object> difficultyAnalysis;
    
    /**
     * 时间分析
     */
    private Map<String, Object> timeAnalysis;
    
    /**
     * 内部类：题目统计
     */
    @Data
    public static class QuestionStatisticsDTO {
        private String questionId;
        private String questionContent;
        private Long totalAnswers;
        private Long correctAnswers;
        private Double correctRate;
        private String difficulty;
        private String category;
    }
}