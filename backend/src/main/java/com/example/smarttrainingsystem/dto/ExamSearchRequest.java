package com.example.smarttrainingsystem.dto;

import com.example.smarttrainingsystem.entity.Exam;
import lombok.Data;

/**
 * 考试搜索请求对象
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Data
public class ExamSearchRequest {
    private String title;
    private Exam.ExamType examType;
    private Exam.ExamStatus status;
    private String courseId;
    private String createdBy;
    private String sortBy = "createdAt";
    private String sortOrder = "desc";
    private Integer page = 0;
    private Integer size = 20;
}