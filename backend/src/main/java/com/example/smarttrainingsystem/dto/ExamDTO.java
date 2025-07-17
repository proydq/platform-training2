package com.example.smarttrainingsystem.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 考试数据传输对象
 * 用于考试基本信息的传输
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Data
public class ExamDTO {
    
    /**
     * 考试ID
     */
    private String id;
    
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
     * 题目数量
     */
    private Integer questionCount;
    
    /**
     * 考试类型
     */
    private String examType;
    
    /**
     * 考试状态
     */
    private String status;
    
    /**
     * 考试开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 考试结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 是否可以参加考试
     */
    private Boolean canTakeExam;
    
    /**
     * 允许重考次数
     */
    private Integer retryCount;
    
    /**
     * 是否随机出题
     */
    private Boolean isRandom;
    
    /**
     * 是否防止切换页面
     */
    private Boolean preventSwitch;
}