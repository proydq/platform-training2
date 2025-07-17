package com.example.smarttrainingsystem.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 题目数据传输对象
 * 用于题目信息的传输（不包含正确答案）
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Data
public class QuestionDTO {
    
    /**
     * 题目ID
     */
    private String id;
    
    /**
     * 题目内容
     */
    private String content;
    
    /**
     * 题目类型
     */
    private String questionType;
    
    /**
     * 选择题选项
     * 格式：[{"key":"A","value":"选项A"},{"key":"B","value":"选项B"}]
     */
    private List<Map<String, String>> options;
    
    /**
     * 题目分值
     */
    private Integer score;
    
    /**
     * 难度等级
     */
    private Integer difficulty;
    
    /**
     * 题目分类
     */
    private String category;
    
    /**
     * 题目标签
     */
    private String tags;
    
    /**
     * 题目序号（在考试中的位置）
     */
    private Integer orderNumber;
    
    /**
     * 是否必答
     */
    private Boolean isRequired;
    
    /**
     * 答题时间限制（秒）
     */
    private Integer timeLimit;
    
    // 注意：这里不包含正确答案和解析，保证考试的公平性
}