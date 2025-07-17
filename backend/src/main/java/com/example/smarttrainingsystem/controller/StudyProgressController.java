package com.example.smarttrainingsystem.controller;

import com.example.smarttrainingsystem.common.Result;
import com.example.smarttrainingsystem.service.StudyProgressService;
import com.example.smarttrainingsystem.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学习进度控制器
 * 提供学习进度统计、分析和排行榜功能
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@RestController
@RequestMapping("/api/v1/study-progress")
@RequiredArgsConstructor
@Slf4j
public class StudyProgressController {

    private final StudyProgressService studyProgressService;

    /**
     * 获取学习进度概览
     *
     * @return 学习进度概览
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<Map<String, Object>> getProgressOverview() {
        log.info("收到获取学习进度概览请求");

        String userId = SecurityUtils.getCurrentUserId();
        Map<String, Object> overview = studyProgressService.getUserProgressOverview(userId);

        return Result.success("获取学习进度概览成功", overview);
    }

    /**
     * 获取学习趋势
     *
     * @param days 统计天数
     * @return 学习趋势数据
     */
    @GetMapping("/trend")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<List<Map<String, Object>>> getProgressTrend(
            @RequestParam(defaultValue = "30") int days) {
        log.info("收到获取学习趋势请求 - 天数: {}", days);

        String userId = SecurityUtils.getCurrentUserId();
        List<Map<String, Object>> trend = studyProgressService.getUserProgressTrend(userId, days);

        return Result.success("获取学习趋势成功", trend);
    }

    /**
     * 获取学习日历数据
     *
     * @param year 年份
     * @param month 月份
     * @return 学习日历数据
     */
    @GetMapping("/calendar")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<List<Map<String, Object>>> getCalendarData(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") int month) {
        log.info("收到获取学习日历数据请求 - 年月: {}-{}", year, month);

        String userId = SecurityUtils.getCurrentUserId();
        List<Map<String, Object>> calendar = studyProgressService.getUserCalendarData(userId, year, month);

        return Result.success("获取学习日历数据成功", calendar);
    }

    /**
     * 获取学习成就统计
     *
     * @return 学习成就统计
     */
    @GetMapping("/achievements")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<Map<String, Object>> getAchievementStats() {
        log.info("收到获取学习成就统计请求");

        String userId = SecurityUtils.getCurrentUserId();
        Map<String, Object> achievements = studyProgressService.getUserAchievementStats(userId);

        return Result.success("获取学习成就统计成功", achievements);
    }

    /**
     * 获取活跃度排行榜
     *
     * @param date 日期
     * @param limit 限制数量
     * @return 活跃度排行榜
     */
    @GetMapping("/ranking/activity")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<List<Map<String, Object>>> getActivityRanking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "10") int limit) {

        if (date == null) {
            date = LocalDate.now();
        }

        log.info("收到获取活跃度排行榜请求 - 日期: {}, 限制: {}", date, limit);

        List<Map<String, Object>> ranking = studyProgressService.getActivityRanking(date, limit);

        return Result.success("获取活跃度排行榜成功", ranking);
    }

    /**
     * 设置学习目标
     *
     * @param goalDuration 目标时长(分钟)
     * @return 操作结果
     */
    @PostMapping("/goal")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<Void> setStudyGoal(@RequestParam int goalDuration) {
        log.info("收到设置学习目标请求 - 目标时长: {}分钟", goalDuration);

        String userId = SecurityUtils.getCurrentUserId();
        studyProgressService.setUserGoal(userId, goalDuration);

        return Result.success();
    }

    /**
     * 手动更新学习进度
     *
     * @param date 日期
     * @return 操作结果
     */
    @PostMapping("/update")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<Void> updateProgress(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }

        log.info("收到手动更新学习进度请求 - 日期: {}", date);

        String userId = SecurityUtils.getCurrentUserId();
        studyProgressService.updateDailyProgress(userId, date);

        return Result.success();
    }

    // ========== 管理员接口 ==========

    /**
     * 获取系统整体学习统计 (管理员)
     *
     * @param date 日期
     * @return 系统统计数据
     */
    @GetMapping("/admin/system-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getSystemStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }

        log.info("收到获取系统整体学习统计请求 - 日期: {}", date);

        Map<String, Object> stats = studyProgressService.getSystemStatistics(date);

        return Result.success("获取系统统计数据成功", stats);
    }

    /**
     * 获取用户学习进度概览 (管理员/讲师)
     *
     * @param userId 用户ID
     * @return 用户学习进度概览
     */
    @GetMapping("/admin/user/{userId}/overview")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<Map<String, Object>> getUserProgressOverview(@PathVariable String userId) {
        log.info("收到获取用户学习进度概览请求 - 用户ID: {}", userId);

        Map<String, Object> overview = studyProgressService.getUserProgressOverview(userId);

        return Result.success("获取用户学习进度概览成功", overview);
    }

    /**
     * 获取用户学习趋势 (管理员/讲师)
     *
     * @param userId 用户ID
     * @param days 统计天数
     * @return 用户学习趋势数据
     */
    @GetMapping("/admin/user/{userId}/trend")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<List<Map<String, Object>>> getUserProgressTrend(@PathVariable String userId,
                                                                  @RequestParam(defaultValue = "30") int days) {
        log.info("收到获取用户学习趋势请求 - 用户ID: {}, 天数: {}", userId, days);

        List<Map<String, Object>> trend = studyProgressService.getUserProgressTrend(userId, days);

        return Result.success("获取用户学习趋势成功", trend);
    }

    /**
     * 获取用户学习成就统计 (管理员/讲师)
     *
     * @param userId 用户ID
     * @return 用户学习成就统计
     */
    @GetMapping("/admin/user/{userId}/achievements")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<Map<String, Object>> getUserAchievementStats(@PathVariable String userId) {
        log.info("收到获取用户学习成就统计请求 - 用户ID: {}", userId);

        Map<String, Object> achievements = studyProgressService.getUserAchievementStats(userId);

        return Result.success("获取用户学习成就统计成功", achievements);
    }

    /**
     * 批量更新用户学习进度 (管理员)
     *
     * @param date 日期
     * @return 操作结果
     */
    @PostMapping("/admin/batch-update")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchUpdateProgress(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date == null) {
            date = LocalDate.now().minusDays(1);
        }

        log.info("收到批量更新用户学习进度请求 - 日期: {}", date);

        // 异步执行批量更新
        studyProgressService.scheduledUpdateAllUsersProgress();

        return Result.success();
    }

    /**
     * 清理过期学习进度记录 (管理员)
     *
     * @return 操作结果
     */
    @PostMapping("/admin/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> cleanupExpiredProgress() {
        log.info("收到清理过期学习进度记录请求");

        // 异步执行清理任务
        studyProgressService.scheduledCleanExpiredProgress();

        return Result.success();
    }

    /**
     * 获取学习进度统计报告 (管理员)
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计报告
     */
    @GetMapping("/admin/report")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getProgressReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("收到获取学习进度统计报告请求 - 时间范围: {} 至 {}", startDate, endDate);

        // TODO: 实现统计报告功能
        Map<String, Object> report = new HashMap<>();
        report.put("message", "统计报告功能开发中");
        report.put("startDate", startDate);
        report.put("endDate", endDate);

        return Result.success("获取统计报告成功", report);
    }

    /**
     * 导出学习进度数据 (管理员)
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param format 导出格式
     * @return 导出结果
     */
    @PostMapping("/admin/export")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Object> exportProgressData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "excel") String format) {

        log.info("收到导出学习进度数据请求 - 时间范围: {} 至 {}, 格式: {}", startDate, endDate, format);

        // TODO: 实现数据导出功能
        return Result.success("导出功能开发中", null);
    }
}