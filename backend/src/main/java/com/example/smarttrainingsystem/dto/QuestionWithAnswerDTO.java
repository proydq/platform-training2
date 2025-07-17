package com.example.smarttrainingsystem.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 带答案的题目数据传输对象
 * 用于查看答案解析时的数据传输
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Data
public class QuestionWithAnswerDTO {
    
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
     * 正确答案
     */
    private String correctAnswer;
    
    /**
     * 用户答案
     */
    private String userAnswer;
    
    /**
     * 是否答对
     */
    private Boolean isCorrect;
    
    /**
     * 获得分数
     */
    private Integer earnedScore;
    
    /**
     * 答案解析
     */
    private String explanation;
    
    /**
     * 题目序号
     */
    private Integer orderNumber;
    
    /**
     * 答题状态
     */
    private String answerStatus; // CORRECT, WRONG, UNANSWERED
    
    /**
     * 知识点
     */
    private String knowledgePoint;
    
    /**
     * 相关参考资料
     */
    private List<String> references;
}