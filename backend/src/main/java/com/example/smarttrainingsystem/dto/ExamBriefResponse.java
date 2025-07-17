package com.example.smarttrainingsystem.dto;

import com.example.smarttrainingsystem.entity.Exam;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试简要信息响应对象
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Data
public class ExamBriefResponse {
    private String id;
    private String title;
    private String courseTitle;
    private Integer duration;
    private Integer totalScore;
    private Integer passScore;
    private Integer questionCount;
    private Exam.ExamType examType;
    private String examTypeDesc;
    private Exam.ExamStatus status;
    private String statusDesc;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    private Boolean canTakeExam;
    private Integer userAttempts;
    private Integer maxAttempts;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 获取考试类型描述
     */
    public String getExamTypeDesc() {
        return examType != null ? examType.getDescription() : null;
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        return status != null ? status.getDescription() : null;
    }
}