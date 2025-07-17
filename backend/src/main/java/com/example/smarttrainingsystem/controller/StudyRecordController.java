package com.example.smarttrainingsystem.controller;

import com.example.smarttrainingsystem.common.Result;
import com.example.smarttrainingsystem.dto.StudyRecordDTO;
import com.example.smarttrainingsystem.service.StudyRecordService;
import com.example.smarttrainingsystem.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 学习记录管理控制器
 * 提供学习记录的增删改查和统计功能
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@RestController
@RequestMapping("/api/v1/study-records")
@RequiredArgsConstructor
@Slf4j
public class StudyRecordController {

    private final StudyRecordService studyRecordService;

    /**
     * 创建学习记录
     *
     * @param request 创建请求
     * @return 学习记录信息
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<StudyRecordDTO.Response> createStudyRecord(@Valid @RequestBody StudyRecordDTO.CreateRequest request) {
        log.info("收到创建学习记录请求 - 课程ID: {}", request.getCourseId());

        String userId = SecurityUtils.getCurrentUserId();
        StudyRecordDTO.Response response = studyRecordService.createStudyRecord(userId, request);

        return Result.success("学习记录创建成功", response);
    }

    /**
     * 开始学习
     *
     * @param courseId 课程ID
     * @return 学习记录信息
     */
    @PostMapping("/start/{courseId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<StudyRecordDTO.Response> startStudy(@PathVariable String courseId) {
        log.info("收到开始学习请求 - 课程ID: {}", courseId);

        String userId = SecurityUtils.getCurrentUserId();
        StudyRecordDTO.Response response = studyRecordService.startStudy(userId, courseId);

        return Result.success("开始学习成功", response);
    }

    /**
     * 更新学习进度
     *
     * @param recordId 学习记录ID
     * @param request 更新请求
     * @return 学习记录信息
     */
    @PutMapping("/{recordId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<StudyRecordDTO.Response> updateProgress(@PathVariable String recordId,
                                                          @Valid @RequestBody StudyRecordDTO.UpdateRequest request) {
        log.info("收到更新学习进度请求 - 记录ID: {}, 进度: {}%", recordId, request.getProgressPercent());

        String userId = SecurityUtils.getCurrentUserId();
        StudyRecordDTO.Response response = studyRecordService.updateProgress(userId, recordId, request);

        return Result.success("学习进度更新成功", response);
    }

    /**
     * 暂停学习
     *
     * @param recordId 学习记录ID
     * @return 学习记录信息
     */
    @PostMapping("/{recordId}/pause")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<StudyRecordDTO.Response> pauseStudy(@PathVariable String recordId) {
        log.info("收到暂停学习请求 - 记录ID: {}", recordId);

        String userId = SecurityUtils.getCurrentUserId();
        StudyRecordDTO.Response response = studyRecordService.pauseStudy(userId, recordId);

        return Result.success("暂停学习成功", response);
    }

    /**
     * 恢复学习
     *
     * @param recordId 学习记录ID
     * @return 学习记录信息
     */
    @PostMapping("/{recordId}/resume")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<StudyRecordDTO.Response> resumeStudy(@PathVariable String recordId) {
        log.info("收到恢复学习请求 - 记录ID: {}", recordId);

        String userId = SecurityUtils.getCurrentUserId();
        StudyRecordDTO.Response response = studyRecordService.resumeStudy(userId, recordId);

        return Result.success("恢复学习成功", response);
    }

    /**
     * 完成学习
     *
     * @param recordId 学习记录ID
     * @return 学习记录信息
     */
    @PostMapping("/{recordId}/complete")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<StudyRecordDTO.Response> completeStudy(@PathVariable String recordId) {
        log.info("收到完成学习请求 - 记录ID: {}", recordId);

        String userId = SecurityUtils.getCurrentUserId();
        StudyRecordDTO.Response response = studyRecordService.completeStudy(userId, recordId);

        return Result.success("完成学习成功", response);
    }

    /**
     * 获取学习记录详情
     *
     * @param recordId 学习记录ID
     * @return 学习记录信息
     */
    @GetMapping("/{recordId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<StudyRecordDTO.Response> getStudyRecord(@PathVariable String recordId) {
        log.info("收到获取学习记录详情请求 - 记录ID: {}", recordId);

        String userId = SecurityUtils.getCurrentUserId();
        StudyRecordDTO.Response response = studyRecordService.getStudyRecord(userId, recordId);

        return Result.success("获取学习记录成功", response);
    }

    /**
     * 根据课程ID获取学习记录
     *
     * @param courseId 课程ID
     * @return 学习记录信息
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<StudyRecordDTO.Response> getStudyRecordByCourse(@PathVariable String courseId) {
        log.info("收到根据课程ID获取学习记录请求 - 课程ID: {}", courseId);

        String userId = SecurityUtils.getCurrentUserId();
        StudyRecordDTO.Response response = studyRecordService.getStudyRecordByCourse(userId, courseId);

        return Result.success("获取学习记录成功", response);
    }

    /**
     * 搜索学习记录
     *
     * @param request 搜索请求
     * @return 学习记录分页列表
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<Page<StudyRecordDTO.BriefResponse>> searchStudyRecords(@Valid @RequestBody StudyRecordDTO.SearchRequest request) {
        log.info("收到搜索学习记录请求 - 关键词: {}, 状态: {}", request.getKeyword(), request.getStatus());

        String userId = SecurityUtils.getCurrentUserId();
        Page<StudyRecordDTO.BriefResponse> response = studyRecordService.searchStudyRecords(userId, request);

        return Result.success("搜索学习记录成功", response);
    }

    /**
     * 获取我的学习记录
     *
     * @param page 页码
     * @param size 每页大小
     * @return 学习记录分页列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<Page<StudyRecordDTO.BriefResponse>> getMyStudyRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("收到获取我的学习记录请求 - 页码: {}, 大小: {}", page, size);

        String userId = SecurityUtils.getCurrentUserId();
        StudyRecordDTO.SearchRequest request = new StudyRecordDTO.SearchRequest();
        request.setPage(page);
        request.setSize(size);
        request.setSortBy("lastStudyTime");
        request.setSortOrder("desc");

        Page<StudyRecordDTO.BriefResponse> response = studyRecordService.searchStudyRecords(userId, request);

        return Result.success("获取我的学习记录成功", response);
    }

    /**
     * 获取正在学习的课程
     *
     * @return 正在学习的课程列表
     */
    @GetMapping("/in-progress")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<List<StudyRecordDTO.BriefResponse>> getInProgressCourses() {
        log.info("收到获取正在学习的课程请求");

        String userId = SecurityUtils.getCurrentUserId();
        List<StudyRecordDTO.BriefResponse> response = studyRecordService.getInProgressCourses(userId);

        return Result.success("获取正在学习的课程成功", response);
    }

    /**
     * 获取已完成的课程
     *
     * @param page 页码
     * @param size 每页大小
     * @return 已完成的课程分页列表
     */
    @GetMapping("/completed")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<Page<StudyRecordDTO.BriefResponse>> getCompletedCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("收到获取已完成的课程请求 - 页码: {}, 大小: {}", page, size);

        String userId = SecurityUtils.getCurrentUserId();
        Page<StudyRecordDTO.BriefResponse> response = studyRecordService.getCompletedCourses(userId, page, size);

        return Result.success("获取已完成的课程成功", response);
    }

    /**
     * 获取收藏的课程
     *
     * @param page 页码
     * @param size 每页大小
     * @return 收藏的课程分页列表
     */
    @GetMapping("/favorites")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<Page<StudyRecordDTO.BriefResponse>> getFavoriteCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("收到获取收藏的课程请求 - 页码: {}, 大小: {}", page, size);

        String userId = SecurityUtils.getCurrentUserId();
        Page<StudyRecordDTO.BriefResponse> response = studyRecordService.getFavoriteCourses(userId, page, size);

        return Result.success("获取收藏的课程成功", response);
    }

    /**
     * 获取学习统计
     *
     * @return 学习统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<StudyRecordDTO.StatisticsResponse> getUserStatistics() {
        log.info("收到获取学习统计请求");

        String userId = SecurityUtils.getCurrentUserId();
        StudyRecordDTO.StatisticsResponse response = studyRecordService.getUserStatistics(userId);

        return Result.success("获取学习统计成功", response);
    }

    /**
     * 获取学习趋势
     *
     * @param days 统计天数
     * @return 学习趋势数据
     */
    @GetMapping("/trend")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<List<StudyRecordDTO.StatisticsResponse.TrendData>> getUserStudyTrend(
            @RequestParam(defaultValue = "30") int days) {
        log.info("收到获取学习趋势请求 - 天数: {}", days);

        String userId = SecurityUtils.getCurrentUserId();
        List<StudyRecordDTO.StatisticsResponse.TrendData> response = studyRecordService.getUserStudyTrend(userId, days);

        return Result.success("获取学习趋势成功", response);
    }

    /**
     * 获取学习时长排行榜
     *
     * @param limit 限制数量
     * @return 学习时长排行榜
     */
    @GetMapping("/ranking/study-time")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<List<StudyRecordDTO.RankingResponse>> getStudyTimeRanking(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("收到获取学习时长排行榜请求 - 限制: {}", limit);

        String userId = SecurityUtils.getCurrentUserId();
        List<StudyRecordDTO.RankingResponse> response = studyRecordService.getStudyTimeRanking(userId, limit);

        return Result.success("获取学习时长排行榜成功", response);
    }

    /**
     * 获取完成课程数排行榜
     *
     * @param limit 限制数量
     * @return 完成课程数排行榜
     */
    @GetMapping("/ranking/completion")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<List<StudyRecordDTO.RankingResponse>> getCompletionRanking(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("收到获取完成课程数排行榜请求 - 限制: {}", limit);

        String userId = SecurityUtils.getCurrentUserId();
        List<StudyRecordDTO.RankingResponse> response = studyRecordService.getCompletionRanking(userId, limit);

        return Result.success("获取完成课程数排行榜成功", response);
    }

    /**
     * 删除学习记录
     *
     * @param recordId 学习记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{recordId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<Void> deleteStudyRecord(@PathVariable String recordId) {
        log.info("收到删除学习记录请求 - 记录ID: {}", recordId);

        String userId = SecurityUtils.getCurrentUserId();
        studyRecordService.deleteStudyRecord(userId, recordId);

        return Result.success();
    }

    /**
     * 批量操作学习记录
     *
     * @param request 批量操作请求
     * @return 操作结果
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<Void> batchOperateRecords(@Valid @RequestBody StudyRecordDTO.BatchOperationRequest request) {
        log.info("收到批量操作学习记录请求 - 操作: {}, 记录数: {}", request.getOperation(), request.getRecordIds().length);

        String userId = SecurityUtils.getCurrentUserId();
        studyRecordService.batchOperateRecords(userId, request);

        return Result.success();
    }

    // ========== 管理员接口 ==========

    /**
     * 获取系统学习统计 (管理员)
     *
     * @return 系统学习统计信息
     */
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Object> getSystemStatistics() {
        log.info("收到获取系统学习统计请求");

        // TODO: 实现系统级学习统计
        return Result.success("功能开发中", null);
    }

    /**
     * 获取用户学习报告 (管理员/讲师)
     *
     * @param userId 用户ID
     * @return 用户学习报告
     */
    @GetMapping("/admin/user/{userId}/report")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<StudyRecordDTO.StatisticsResponse> getUserReport(@PathVariable String userId) {
        log.info("收到获取用户学习报告请求 - 用户ID: {}", userId);

        StudyRecordDTO.StatisticsResponse response = studyRecordService.getUserStatistics(userId);

        return Result.success("获取用户学习报告成功", response);
    }

    /**
     * 导出学习记录 (管理员)
     *
     * @param request 导出请求
     * @return 导出结果
     */
    @PostMapping("/admin/export")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Object> exportStudyRecords(@Valid @RequestBody StudyRecordDTO.ExportRequest request) {
        log.info("收到导出学习记录请求 - 格式: {}", request.getFormat());

        // TODO: 实现学习记录导出功能
        return Result.success("功能开发中", null);
    }
}