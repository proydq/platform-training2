package com.example.smarttrainingsystem.dto;

import lombok.Data;

import java.util.List;

/**
 * 题目选择配置对象
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Data
public class QuestionSelection {
    private String questionType;
    private Integer count;
    private String category;
    private Integer difficulty;
    private List<String> questionIds;
}