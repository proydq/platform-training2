package com.example.smarttrainingsystem.controller;

import com.example.smarttrainingsystem.common.Result;
import com.example.smarttrainingsystem.dto.WrongQuestionDTO;
import com.example.smarttrainingsystem.service.WrongQuestionsService;
import com.example.smarttrainingsystem.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 错题集控制器
 * 提供错题管理相关的RESTful API接口
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@RestController
@RequestMapping("/api/v1/wrong-questions")
@Slf4j
public class WrongQuestionsController {

    @Autowired
    private WrongQuestionsService wrongQuestionsService;

    /**
     * 获取用户错题列表
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sort 排序字段
     * @param order 排序方向（asc/desc）
     * @param difficulty 难度筛选
     * @param category 分类筛选
     * @param status 掌握状态筛选
     * @return 错题分页列表
     */
    @GetMapping
    public Result<Page<WrongQuestionDTO.ListItem>> getWrongQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "wrongCount") String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {

        try {
            String userId = SecurityUtils.getCurrentUserId();

            // 构建分页参数
            Sort.Direction direction = "desc".equalsIgnoreCase(order) ?
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

            Page<WrongQuestionDTO.ListItem> result = wrongQuestionsService.getUserWrongQuestions(
                    userId, difficulty, category, status, pageable);

            return Result.success("获取错题列表成功", result);
        } catch (Exception e) {
            log.error("获取错题列表失败", e);
            return Result.error("获取错题列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取错题详情
     *
     * @param questionId 题目ID
     * @return 错题详情
     */
    @GetMapping("/{questionId}")
    public Result<WrongQuestionDTO.Detail> getWrongQuestionDetail(@PathVariable String questionId) {
        try {
            String userId = SecurityUtils.getCurrentUserId();
            WrongQuestionDTO.Detail detail = wrongQuestionsService.getWrongQuestionDetail(userId, questionId);
            return Result.success("获取错题详情成功", detail);
        } catch (Exception e) {
            log.error("获取错题详情失败", e);
            return Result.error("获取错题详情失败: " + e.getMessage());
        }
    }

    /**
     * 标记题目为已掌握
     *
     * @param questionId 题目ID
     * @return 操作结果
     */
    @PostMapping("/{questionId}/master")
    public Result<Void> markAsMastered(@PathVariable String questionId) {
        try {
            String userId = SecurityUtils.getCurrentUserId();
            wrongQuestionsService.markQuestionAsMastered(userId, questionId);
            return Result.success("标记成功", null);
        } catch (Exception e) {
            log.error("标记题目为已掌握失败", e);
            return Result.error("标记失败: " + e.getMessage());
        }
    }

    /**
     * 添加题目笔记
     *
     * @param questionId 题目ID
     * @param noteRequest 笔记内容
     * @return 操作结果
     */
    @PostMapping("/{questionId}/note")
    public Result<Void> addNote(@PathVariable String questionId,
                                @RequestBody Map<String, String> noteRequest) {
        try {
            String userId = SecurityUtils.getCurrentUserId();
            String note = noteRequest.get("note");
            wrongQuestionsService.addQuestionNote(userId, questionId, note);
            return Result.success("添加笔记成功", null);
        } catch (Exception e) {
            log.error("添加笔记失败", e);
            return Result.error("添加笔记失败: " + e.getMessage());
        }
    }

    /**
     * 清除已掌握的题目
     *
     * @return 操作结果
     */
    @DeleteMapping("/mastered")
    public Result<Map<String, Object>> clearMasteredQuestions() {
        try {
            String userId = SecurityUtils.getCurrentUserId();
            int clearedCount = wrongQuestionsService.clearMasteredQuestions(userId);

            // Java 8兼容写法：使用HashMap而不是Map.of
            Map<String, Object> result = new HashMap<>();
            result.put("clearedCount", clearedCount);
            return Result.success("清除已掌握题目成功", result);
        } catch (Exception e) {
            log.error("清除已掌握题目失败", e);
            return Result.error("清除失败: " + e.getMessage());
        }
    }

    /**
     * 获取错题统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public Result<WrongQuestionDTO.Statistics> getWrongQuestionsStatistics() {
        try {
            String userId = SecurityUtils.getCurrentUserId();
            WrongQuestionDTO.Statistics statistics = wrongQuestionsService.getWrongQuestionsStatistics(userId);
            return Result.success("获取统计信息成功", statistics);
        } catch (Exception e) {
            log.error("获取错题统计失败", e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 生成错题练习
     *
     * @param count 题目数量
     * @param difficulty 难度筛选
     * @param category 分类筛选
     * @return 练习题目列表
     */
    @PostMapping("/practice")
    public Result<List<WrongQuestionDTO.Practice>> generatePractice(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String category) {

        try {
            String userId = SecurityUtils.getCurrentUserId();
            List<WrongQuestionDTO.Practice> questions = wrongQuestionsService.generateWrongQuestionsPractice(
                    userId, count, difficulty, category);

            return Result.success("生成练习题目成功", questions);
        } catch (Exception e) {
            log.error("生成错题练习失败", e);
            return Result.error("生成练习失败: " + e.getMessage());
        }
    }

    /**
     * 提交错题练习结果
     *
     * @param answers 答案数据
     * @return 练习结果
     */
    @PostMapping("/practice/submit")
    public Result<WrongQuestionDTO.PracticeResult> submitPractice(
            @RequestBody Map<String, String> answers) {

        try {
            String userId = SecurityUtils.getCurrentUserId();
            WrongQuestionDTO.PracticeResult result = wrongQuestionsService.submitWrongQuestionsPractice(
                    userId, answers);

            return Result.success("提交练习成功", result);
        } catch (Exception e) {
            log.error("提交错题练习失败", e);
            return Result.error("提交练习失败: " + e.getMessage());
        }
    }

    /**
     * 导出错题集
     *
     * @param format 导出格式（pdf/excel）
     * @param difficulty 难度筛选
     * @param category 分类筛选
     * @return 导出文件信息
     */
    @PostMapping("/export")
    public Result<Map<String, Object>> exportWrongQuestions(
            @RequestParam(defaultValue = "pdf") String format,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String category) {

        try {
            String userId = SecurityUtils.getCurrentUserId();
            String fileName = wrongQuestionsService.exportWrongQuestions(
                    userId, format, difficulty, category);

            // Java 8兼容写法：使用HashMap而不是Map.of
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("downloadUrl", "/api/v1/files/download/" + fileName);

            return Result.success("导出错题集成功", result);
        } catch (Exception e) {
            log.error("导出错题集失败", e);
            return Result.error("导出失败: " + e.getMessage());
        }
    }
}