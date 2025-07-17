package com.example.smarttrainingsystem.dto;

import com.example.smarttrainingsystem.entity.Question;
import lombok.Data;

import javax.validation.constraints.*;
import java.util.List;

/**
 * 题目更新请求对象
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Data
public class QuestionUpdateRequest {
    @Size(max = 2000, message = "题目内容长度不能超过2000字符")
    private String content;

    private Question.QuestionType questionType;

    private List<String> options;

    private String correctAnswer;

    @Size(max = 1000, message = "答案解析长度不能超过1000字符")
    private String explanation;

    @Min(value = 1, message = "题目分数至少1分")
    @Max(value = 100, message = "题目分数最多100分")
    private Integer score;

    @Min(value = 1, message = "难度等级至少1级")
    @Max(value = 5, message = "难度等级最多5级")
    private Integer difficulty;

    @Size(max = 100, message = "题目分类长度不能超过100字符")
    private String category;

    @Size(max = 500, message = "标签长度不能超过500字符")
    private String tags;
}