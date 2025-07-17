package com.example.smarttrainingsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 考试更新请求对象
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Data
public class ExamUpdateRequest {
    @Size(max = 200, message = "考试标题长度不能超过200字符")
    private String title;

    @Size(max = 1000, message = "考试描述长度不能超过1000字符")
    private String description;

    @Min(value = 1, message = "考试时长至少1分钟")
    @Max(value = 480, message = "考试时长最多480分钟")
    private Integer duration;

    @Min(value = 1, message = "总分至少1分")
    @Max(value = 1000, message = "总分最多1000分")
    private Integer totalScore;

    @Min(value = 1, message = "及格分数至少1分")
    private Integer passScore;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Boolean isRandom;

    private Boolean preventSwitch;

    @Min(value = 1, message = "重考次数至少1次")
    @Max(value = 10, message = "重考次数最多10次")
    private Integer retryCount;
}