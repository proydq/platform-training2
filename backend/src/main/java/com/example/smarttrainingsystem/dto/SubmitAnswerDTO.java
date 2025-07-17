package com.example.smarttrainingsystem.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 提交答案数据传输对象
 * 用于考试答案提交
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Data
public class SubmitAnswerDTO {
    
    /**
     * 考试ID
     */
    @NotNull(message = "考试ID不能为空")
    private String examId;
    
    /**
     * 用户答案
     * key: 题目ID, value: 用户答案
     */
    @NotEmpty(message = "答案不能为空")
    private Map<String, String> answers;
    
    /**
     * 考试会话ID（用于防作弊验证）
     */
    private String sessionId;
    
    /**
     * 答题开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 答题结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 实际用时（分钟）
     */
    private Integer actualDuration;
    
    /**
     * 切换页面次数（防作弊）
     */
    private Integer switchCount;
    
    /**
     * 是否提前提交
     */
    private Boolean isEarlySubmit;
    
    /**
     * 客户端信息（用于记录）
     */
    private String clientInfo;
    
    /**
     * 提交方式
     * AUTO: 自动提交（时间到）
     * MANUAL: 手动提交
     */
    private String submitType;
}