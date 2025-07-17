package com.example.smarttrainingsystem.dto;

import lombok.Data;

/**
 * 题目配置对象
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Data
public class QuestionConfig {
    private String questionType;
    private Integer count;
    private Integer score;
    private String category;
    private Integer difficulty;
}