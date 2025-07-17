package com.example.smarttrainingsystem.dto;

import lombok.Data;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 创建考试请求数据传输对象
 * 用于创建新考试时的数据传输
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Data
public class ExamCreateRequestDTO {

    /**
     * 考试标题
     */
    @NotBlank(message = "考试标题不能为空")
    @Size(max = 200, message = "考试标题长度不能超过200字符")
    private String title;

    /**
     * 考试描述
     */
    @Size(max = 2000, message = "考试描述长度不能超过2000字符")
    private String description;

    /**
     * 关联课程ID
     */
    private String courseId;

    /**
     * 考试时长（分钟）
     */
    @NotNull(message = "考试时长不能为空")
    @Min(value = 1, message = "考试时长至少1分钟")
    @Max(value = 480, message = "考试时长最多480分钟")
    private Integer duration;

    /**
     * 总分
     */
    @NotNull(message = "总分不能为空")
    @Min(value = 1, message = "总分至少1分")
    @Max(value = 1000, message = "总分最多1000分")
    private Integer totalScore;

    /**
     * 及格分数
     */
    @NotNull(message = "及格分数不能为空")
    @Min(value = 1, message = "及格分数至少1分")
    private Integer passScore;

    /**
     * 考试类型
     */
    @NotBlank(message = "考试类型不能为空")
    private String examType; // PRACTICE, FORMAL, MOCK

    /**
     * 考试开始时间
     */
    private LocalDateTime startTime;

    /**
     * 考试结束时间
     */
    private LocalDateTime endTime;

    /**
     * 是否随机出题
     */
    private Boolean isRandom = false;

    /**
     * 是否限制切换页面
     */
    private Boolean preventSwitch = true;

    /**
     * 允许重考次数
     */
    @Min(value = 0, message = "重考次数不能为负数")
    @Max(value = 10, message = "重考次数最多10次")
    private Integer retryCount = 1;

    /**
     * 题目ID列表
     */
    @NotEmpty(message = "题目列表不能为空")
    private List<String> questionIds;

    /**
     * 题目分值配置
     * key: 题目ID, value: 分值
     */
    private Map<String, Integer> questionScores;

    /**
     * 考试说明
     */
    private String instructions;

    /**
     * 是否立即发布
     */
    private Boolean publishImmediately = false;

    /**
     * 允许查看答案
     */
    private Boolean allowViewAnswer = true;

    /**
     * 显示分数
     */
    private Boolean showScore = true;

    /**
     * 显示正确答案
     */
    private Boolean showCorrectAnswer = true;

    /**
     * 显示解析
     */
    private Boolean showExplanation = true;
}