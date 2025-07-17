package com.example.smarttrainingsystem.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 考试结果数据传输对象
 * 用于考试结果的传输
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Data
public class ExamResultDTO {
    
    /**
     * 考试结果ID
     */
    private String id;
    
    /**
     * 考试ID
     */
    private String examId;
    
    /**
     * 考试标题
     */
    private String examTitle;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户姓名
     */
    private String userName;
    
    /**
     * 得分
     */
    private Integer score;
    
    /**
     * 总分
     */
    private Integer totalScore;
    
    /**
     * 及格分数
     */
    private Integer passScore;
    
    /**
     * 正确题目数
     */
    private Integer correctCount;
    
    /**
     * 错误题目数
     */
    private Integer wrongCount;
    
    /**
     * 总题目数
     */
    private Integer totalCount;
    
    /**
     * 正确率
     */
    private Double accuracyRate;
    
    /**
     * 实际用时（分钟）
     */
    private Integer duration;
    
    /**
     * 通过状态
     */
    private String passStatus;
    
    /**
     * 是否通过
     */
    private Boolean passed;
    
    /**
     * 考试开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 考试结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 排名
     */
    private Integer ranking;
    
    /**
     * 本次考试是第几次尝试
     */
    private Integer attemptCount;
    
    /**
     * 错题列表（题目ID）
     */
    private List<String> wrongQuestionIds;
    
    /**
     * 答题详情（可选，根据需要返回）
     */
    private Map<String, Object> answerDetails;
    
    /**
     * 成绩等级
     */
    private String grade;
    
    /**
     * 评语
     */
    private String comment;
    
    /**
     * 是否可以重考
     */
    private Boolean canRetake;
    
    /**
     * 剩余重考次数
     */
    private Integer remainingRetakes;
}