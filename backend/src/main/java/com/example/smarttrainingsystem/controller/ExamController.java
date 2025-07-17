package com.example.smarttrainingsystem.controller;

import com.example.smarttrainingsystem.common.Result;
import com.example.smarttrainingsystem.dto.ExamDTO;
import com.example.smarttrainingsystem.dto.ExamResultDTO;
import com.example.smarttrainingsystem.dto.ExamSessionDTO;
import com.example.smarttrainingsystem.dto.SubmitAnswerDTO;
import com.example.smarttrainingsystem.service.ExamService;
import com.example.smarttrainingsystem.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 考试管理控制器
 * 提供考试的基本功能：列表、开始、提交、结果查看
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@Slf4j
public class ExamController {

    private final ExamService examService;

    /**
     * 获取可用考试列表
     *
     * @return 考试列表
     */
    @GetMapping
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<List<ExamDTO>> getExamList() {
        log.info("收到获取考试列表请求");
        
        String userId = SecurityUtils.getCurrentUserId();
        List<ExamDTO> exams = examService.getAvailableExams(userId);
        
        return Result.success("获取考试列表成功", exams);
    }

    /**
     * 开始考试
     *
     * @param examId 考试ID
     * @return 考试会话信息（包含题目）
     */
    @PostMapping("/{examId}/start")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<ExamSessionDTO> startExam(@PathVariable String examId) {
        log.info("收到开始考试请求 - 考试ID: {}", examId);
        
        String userId = SecurityUtils.getCurrentUserId();
        ExamSessionDTO session = examService.startExam(userId, examId);
        
        return Result.success("开始考试成功", session);
    }

    /**
     * 提交考试答案
     *
     * @param examId 考试ID
     * @param submitData 提交的答案数据
     * @return 考试结果
     */
    @PostMapping("/{examId}/submit")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<ExamResultDTO> submitExam(@PathVariable String examId, 
                                           @Valid @RequestBody SubmitAnswerDTO submitData) {
        log.info("收到提交考试请求 - 考试ID: {}", examId);
        
        String userId = SecurityUtils.getCurrentUserId();
        ExamResultDTO result = examService.submitExam(userId, examId, submitData);
        
        return Result.success("提交考试成功", result);
    }

    /**
     * 获取考试结果
     *
     * @param examId 考试ID
     * @return 考试结果
     */
    @GetMapping("/{examId}/result")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<ExamResultDTO> getExamResult(@PathVariable String examId) {
        log.info("收到获取考试结果请求 - 考试ID: {}", examId);
        
        String userId = SecurityUtils.getCurrentUserId();
        ExamResultDTO result = examService.getExamResult(userId, examId);
        
        return Result.success("获取考试结果成功", result);
    }

    /**
     * 获取考试详情
     *
     * @param examId 考试ID
     * @return 考试详情
     */
    @GetMapping("/{examId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<ExamDTO> getExamDetails(@PathVariable String examId) {
        log.info("收到获取考试详情请求 - 考试ID: {}", examId);
        
        ExamDTO exam = examService.getExamDetails(examId);
        
        return Result.success("获取考试详情成功", exam);
    }

    /**
     * 检查考试状态
     *
     * @param examId 考试ID
     * @return 考试状态信息
     */
    @GetMapping("/{examId}/status")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<Object> getExamStatus(@PathVariable String examId) {
        log.info("收到检查考试状态请求 - 考试ID: {}", examId);
        
        String userId = SecurityUtils.getCurrentUserId();
        Object status = examService.getExamStatus(userId, examId);
        
        return Result.success("获取考试状态成功", status);
    }
}