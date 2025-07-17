package com.example.smarttrainingsystem.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 试卷生成请求对象
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Data
public class GeneratePaperRequest {
    @NotBlank(message = "考试ID不能为空")
    private String examId;
    
    private Boolean shuffle = true;
    private List<QuestionSelection> questionSelections;
}