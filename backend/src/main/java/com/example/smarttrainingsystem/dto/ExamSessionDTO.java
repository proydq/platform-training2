package com.example.smarttrainingsystem.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试会话数据传输对象
 * 用于开始考试时返回考试信息和题目列表
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Data
public class ExamSessionDTO {
    
    /**
     * 考试ID
     */
    private String examId;
    
    /**
     * 考试标题
     */
    private String title;
    
    /**
     * 考试描述
     */
    private String description;
    
    /**
     * 考试时长（分钟）
     */
    private Integer duration;
    
    /**
     * 总分
     */
    private Integer totalScore;
    
    /**
     * 及格分数
     */
    private Integer passScore;
    
    /**
     * 考试开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 题目列表
     */
    private List<QuestionDTO> questions;
    
    /**
     * 考试会话ID（用于防作弊）
     */
    private String sessionId;
    
    /**
     * 考试说明
     */
    private String instructions;
    
    /**
     * 是否允许返回上一题
     */
    private Boolean allowPrevious;
    
    /**
     * 是否显示题目分值
     */
    private Boolean showScore;
}