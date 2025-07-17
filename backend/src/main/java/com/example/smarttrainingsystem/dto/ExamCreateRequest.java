package com.example.smarttrainingsystem.dto;

import com.example.smarttrainingsystem.entity.Exam;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试创建请求对象
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Data
public class ExamCreateRequest {
    @NotBlank(message = "考试标题不能为空")
    @Size(max = 200, message = "考试标题长度不能超过200字符")
    private String title;

    @Size(max = 1000, message = "考试描述长度不能超过1000字符")
    private String description;

    private String courseId;

    @NotNull(message = "考试时长不能为空")
    @Min(value = 1, message = "考试时长至少1分钟")
    @Max(value = 480, message = "考试时长最多480分钟")
    private Integer duration;

    @NotNull(message = "总分不能为空")
    @Min(value = 1, message = "总分至少1分")
    @Max(value = 1000, message = "总分最多1000分")
    private Integer totalScore = 100;

    @NotNull(message = "及格分数不能为空")
    @Min(value = 1, message = "及格分数至少1分")
    private Integer passScore = 60;

    @NotNull(message = "题目数量不能为空")
    @Min(value = 1, message = "题目数量至少1题")
    @Max(value = 200, message = "题目数量最多200题")
    private Integer questionCount;

    @NotNull(message = "考试类型不能为空")
    private Exam.ExamType examType = Exam.ExamType.PRACTICE;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Boolean isRandom = false;

    private Boolean preventSwitch = true;

    @Min(value = 1, message = "重考次数至少1次")
    @Max(value = 10, message = "重考次数最多10次")
    private Integer retryCount = 1;

    // 题目配置
    private List<QuestionConfig> questionConfigs;
}