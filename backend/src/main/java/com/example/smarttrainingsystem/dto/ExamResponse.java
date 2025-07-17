package com.example.smarttrainingsystem.dto;

import com.example.smarttrainingsystem.entity.Exam;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试响应对象
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Data
public class ExamResponse {
    private String id;
    private String title;
    private String description;
    private String courseId;
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
    
    private Boolean isRandom;
    private Boolean preventSwitch;
    private Integer retryCount;
    private String createdBy;
    private String createdByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 统计信息
    private Long participantCount;
    private Long passCount;
    private Double averageScore;
    private Boolean canTakeExam;
    private Integer userAttempts;
    private Integer maxAttempts;

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

    /**
     * 获取通过率
     */
    public Double getPassRate() {
        if (participantCount == null || participantCount == 0) {
            return 0.0;
        }
        return (passCount != null ? passCount.doubleValue() : 0.0) / participantCount * 100;
    }

    /**
     * 获取考试状态
     */
    public String getExamStatusInfo() {
        LocalDateTime now = LocalDateTime.now();
        if (status == Exam.ExamStatus.DRAFT) {
            return "草稿";
        } else if (status == Exam.ExamStatus.CANCELLED) {
            return "已取消";
        } else if (status == Exam.ExamStatus.ENDED) {
            return "已结束";
        } else if (status == Exam.ExamStatus.PUBLISHED) {
            if (startTime != null && now.isBefore(startTime)) {
                return "未开始";
            } else if (endTime != null && now.isAfter(endTime)) {
                return "已过期";
            } else {
                return "进行中";
            }
        }
        return "未知";
    }
}