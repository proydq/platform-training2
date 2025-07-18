package com.example.smarttrainingsystem.service;

import com.example.smarttrainingsystem.entity.StudyProgress;
import com.example.smarttrainingsystem.entity.StudyRecord;
import com.example.smarttrainingsystem.repository.StudyProgressRepository;
import com.example.smarttrainingsystem.repository.StudyRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习进度业务服务
 * 负责学习进度的统计、分析和更新
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudyProgressService {

    private final StudyProgressRepository studyProgressRepository;
    private final StudyRecordRepository studyRecordRepository;

    /**
     * 更新用户每日学习进度
     *
     * @param userId 用户ID
     * @param date 日期
     */
    @Transactional
    public void updateDailyProgress(String userId, LocalDate date) {
        log.info("更新用户每日学习进度 - 用户ID: {}, 日期: {}", userId, date);

        // 查找或创建每日进度记录
        Optional<StudyProgress> existingProgress = studyProgressRepository
                .findByUserIdAndProgressDateAndProgressType(userId, date, StudyProgress.ProgressType.DAILY);

        StudyProgress dailyProgress = existingProgress.orElse(new StudyProgress());
        if (!existingProgress.isPresent()) {
            dailyProgress.setUserId(userId);
            dailyProgress.setProgressDate(date);
            dailyProgress.setProgressType(StudyProgress.ProgressType.DAILY);
        }

        // 计算当日学习统计
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        // 学习时长统计
        long totalStudyTime = studyRecordRepository.sumStudyDurationByUserIdAndTimeRange(
                userId, startOfDay, endOfDay);

        // 学习课程统计
        List<StudyRecord> studyRecords = studyRecordRepository.findRecentByUserId(
                userId, PageRequest.of(0, 1000));

        int coursesStudied = (int) studyRecords.stream()
                .map(StudyRecord::getCourseId)
                .distinct()
                .count();

        int coursesCompleted = (int) studyRecords.stream()
                .filter(record -> record.getStudyStatus() == StudyRecord.Status.COMPLETED)
                .map(StudyRecord::getCourseId)
                .distinct()
                .count();

        int chaptersCompleted = (int) studyRecords.stream()
                .filter(record -> record.getStudyStatus() == StudyRecord.Status.COMPLETED)
                .count();

        // 更新统计数据
        dailyProgress.updateStatistics(coursesStudied, coursesCompleted,
                (int) totalStudyTime, chaptersCompleted);

        // 更新连续学习天数
        updateContinuousDays(userId, date, dailyProgress);

        studyProgressRepository.save(dailyProgress);
        log.info("用户每日学习进度更新完成 - 用户ID: {}, 学习时长: {}分钟", userId, totalStudyTime);
    }

    /**
     * 更新用户每周学习进度
     *
     * @param userId 用户ID
     * @param date 日期
     */
    @Transactional
    public void updateWeeklyProgress(String userId, LocalDate date) {
        log.info("更新用户每周学习进度 - 用户ID: {}, 日期: {}", userId, date);

        // 计算本周的开始日期(周一)
        LocalDate weekStart = date.with(java.time.DayOfWeek.MONDAY);

        // 查找或创建每周进度记录
        Optional<StudyProgress> existingProgress = studyProgressRepository
                .findByUserIdAndProgressDateAndProgressType(userId, weekStart, StudyProgress.ProgressType.WEEKLY);

        StudyProgress weeklyProgress = existingProgress.orElse(new StudyProgress());
        if (!existingProgress.isPresent()) {
            weeklyProgress.setUserId(userId);
            weeklyProgress.setProgressDate(weekStart);
            weeklyProgress.setProgressType(StudyProgress.ProgressType.WEEKLY);
        }

        // 获取本周的每日进度记录
        LocalDate weekEnd = weekStart.plusDays(6);
        List<StudyProgress> dailyProgresses = studyProgressRepository.findByUserIdAndDateRange(
                userId, weekStart, weekEnd, StudyProgress.ProgressType.DAILY);

        // 汇总每日数据
        int totalCoursesStudied = dailyProgresses.stream()
                .mapToInt(StudyProgress::getCoursesStudied)
                .sum();

        int totalCoursesCompleted = dailyProgresses.stream()
                .mapToInt(StudyProgress::getCoursesCompleted)
                .sum();

        int totalStudyDuration = dailyProgresses.stream()
                .mapToInt(StudyProgress::getStudyDuration)
                .sum();

        int totalChaptersCompleted = dailyProgresses.stream()
                .mapToInt(StudyProgress::getChaptersCompleted)
                .sum();

        // 更新统计数据
        weeklyProgress.updateStatistics(totalCoursesStudied, totalCoursesCompleted,
                totalStudyDuration, totalChaptersCompleted);

        // 设置周目标(默认为7小时)
        weeklyProgress.setGoalDuration(420); // 7小时 = 420分钟

        studyProgressRepository.save(weeklyProgress);
        log.info("用户每周学习进度更新完成 - 用户ID: {}, 周学习时长: {}分钟", userId, totalStudyDuration);
    }

    /**
     * 更新用户每月学习进度
     *
     * @param userId 用户ID
     * @param date 日期
     */
    @Transactional
    public void updateMonthlyProgress(String userId, LocalDate date) {
        log.info("更新用户每月学习进度 - 用户ID: {}, 日期: {}", userId, date);

        // 计算本月的开始日期
        LocalDate monthStart = date.withDayOfMonth(1);

        // 查找或创建每月进度记录
        Optional<StudyProgress> existingProgress = studyProgressRepository
                .findByUserIdAndProgressDateAndProgressType(userId, monthStart, StudyProgress.ProgressType.MONTHLY);

        StudyProgress monthlyProgress = existingProgress.orElse(new StudyProgress());
        if (!existingProgress.isPresent()) {
            monthlyProgress.setUserId(userId);
            monthlyProgress.setProgressDate(monthStart);
            monthlyProgress.setProgressType(StudyProgress.ProgressType.MONTHLY);
        }

        // 获取本月的每日进度记录
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        List<StudyProgress> dailyProgresses = studyProgressRepository.findByUserIdAndDateRange(
                userId, monthStart, monthEnd, StudyProgress.ProgressType.DAILY);

        // 汇总每日数据
        int totalCoursesStudied = dailyProgresses.stream()
                .mapToInt(StudyProgress::getCoursesStudied)
                .sum();

        int totalCoursesCompleted = dailyProgresses.stream()
                .mapToInt(StudyProgress::getCoursesCompleted)
                .sum();

        int totalStudyDuration = dailyProgresses.stream()
                .mapToInt(StudyProgress::getStudyDuration)
                .sum();

        int totalChaptersCompleted = dailyProgresses.stream()
                .mapToInt(StudyProgress::getChaptersCompleted)
                .sum();

        // 更新统计数据
        monthlyProgress.updateStatistics(totalCoursesStudied, totalCoursesCompleted,
                totalStudyDuration, totalChaptersCompleted);

        // 设置月目标(默认为30小时)
        monthlyProgress.setGoalDuration(1800); // 30小时 = 1800分钟

        studyProgressRepository.save(monthlyProgress);
        log.info("用户每月学习进度更新完成 - 用户ID: {}, 月学习时长: {}分钟", userId, totalStudyDuration);
    }

    /**
     * 获取用户学习进度概览
     *
     * @param userId 用户ID
     * @return 学习进度概览
     */
    public Map<String, Object> getUserProgressOverview(String userId) {
        log.info("获取用户学习进度概览 - 用户ID: {}", userId);

        Map<String, Object> overview = new HashMap<>();
        LocalDate today = LocalDate.now();

        // 今日进度
        Optional<StudyProgress> todayProgress = studyProgressRepository
                .findByUserIdAndProgressDateAndProgressType(userId, today, StudyProgress.ProgressType.DAILY);
        overview.put("today", todayProgress.orElse(new StudyProgress()));

        // 本周进度
        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
        Optional<StudyProgress> weekProgress = studyProgressRepository
                .findByUserIdAndProgressDateAndProgressType(userId, weekStart, StudyProgress.ProgressType.WEEKLY);
        overview.put("thisWeek", weekProgress.orElse(new StudyProgress()));

        // 本月进度
        LocalDate monthStart = today.withDayOfMonth(1);
        Optional<StudyProgress> monthProgress = studyProgressRepository
                .findByUserIdAndProgressDateAndProgressType(userId, monthStart, StudyProgress.ProgressType.MONTHLY);
        overview.put("thisMonth", monthProgress.orElse(new StudyProgress()));

        // 总体统计
        Map<String, Object> totalStats = new HashMap<>();
        totalStats.put("totalStudyDays", studyProgressRepository.countStudyDaysByUserId(userId));
        totalStats.put("goalAchievedDays", studyProgressRepository.countGoalAchievedDaysByUserId(userId));
        totalStats.put("maxContinuousDays", studyProgressRepository.getMaxContinuousDaysByUserId(userId));
        totalStats.put("currentContinuousDays", studyProgressRepository.getCurrentContinuousDaysByUserId(userId));
        totalStats.put("avgDailyStudyTime", studyProgressRepository.getAverageStudyDurationByUserId(userId, StudyProgress.ProgressType.DAILY));
        totalStats.put("avgActivityScore", studyProgressRepository.getAverageActivityScoreByUserId(userId, StudyProgress.ProgressType.DAILY));

        overview.put("totalStats", totalStats);

        return overview;
    }

    /**
     * 获取用户学习趋势
     *
     * @param userId 用户ID
     * @param days 天数
     * @return 学习趋势数据
     */
    public List<Map<String, Object>> getUserProgressTrend(String userId, int days) {
        log.info("获取用户学习趋势 - 用户ID: {}, 天数: {}", userId, days);

        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        List<Object[]> trendData = studyProgressRepository.getUserProgressTrend(userId, startDate);

        return trendData.stream()
                .map(data -> {
                    Map<String, Object> trend = new HashMap<>();
                    trend.put("date", data[0]);
                    trend.put("studyDuration", data[1]);
                    trend.put("coursesCompleted", data[2]);
                    trend.put("activityScore", data[3]);
                    trend.put("goalAchieved", data[4]);
                    return trend;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取活跃度排行榜
     *
     * @param date 日期
     * @param limit 限制数量
     * @return 活跃度排行榜
     */
    public List<Map<String, Object>> getActivityRanking(LocalDate date, int limit) {
        log.info("获取活跃度排行榜 - 日期: {}, 限制: {}", date, limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> rankingData = studyProgressRepository.getActivityRanking(
                StudyProgress.ProgressType.DAILY, date, pageable);

        return rankingData.stream()
                .map(data -> {
                    Map<String, Object> ranking = new HashMap<>();
                    ranking.put("userId", data[0]);
                    ranking.put("username", data[1]);
                    ranking.put("realName", data[2]);
                    ranking.put("activityScore", data[3]);
                    ranking.put("studyDuration", data[4]);
                    ranking.put("coursesCompleted", data[5]);
                    return ranking;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取用户学习日历数据
     *
     * @param userId 用户ID
     * @param year 年份
     * @param month 月份
     * @return 学习日历数据
     */
    public List<Map<String, Object>> getUserCalendarData(String userId, int year, int month) {
        log.info("获取用户学习日历数据 - 用户ID: {}, 年月: {}-{}", userId, year, month);

        List<Object[]> calendarData = studyProgressRepository.getUserCalendarData(userId, year, month);

        return calendarData.stream()
                .map(data -> {
                    Map<String, Object> calendar = new HashMap<>();
                    calendar.put("date", data[0]);
                    calendar.put("studyDuration", data[1]);
                    calendar.put("goalAchieved", data[2]);
                    calendar.put("activityScore", data[3]);
                    return calendar;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取用户学习成就统计
     *
     * @param userId 用户ID
     * @return 学习成就统计
     */
    public Map<String, Object> getUserAchievementStats(String userId) {
        log.info("获取用户学习成就统计 - 用户ID: {}", userId);

        Object[] achievementData = studyProgressRepository.getUserAchievementStats(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("goalAchievedDays", achievementData[0]);
        stats.put("maxContinuousDays", achievementData[1]);
        stats.put("avgActivityScore", achievementData[2]);
        stats.put("totalStudyTime", achievementData[3]);
        stats.put("totalCoursesCompleted", achievementData[4]);

        // 计算成就等级
        stats.put("achievements", calculateAchievements(achievementData));

        return stats;
    }

    /**
     * 获取系统整体学习统计
     *
     * @param date 日期
     * @return 系统统计数据
     */
    public Map<String, Object> getSystemStatistics(LocalDate date) {
        log.info("获取系统整体学习统计 - 日期: {}", date);

        Object[] systemData = studyProgressRepository.getSystemStatistics(date, StudyProgress.ProgressType.DAILY);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", systemData[0]);
        stats.put("totalStudyTime", systemData[1]);
        stats.put("totalCoursesCompleted", systemData[2]);
        stats.put("avgActivityScore", systemData[3]);
        stats.put("goalAchievedCount", systemData[4]);

        return stats;
    }

    /**
     * 设置用户学习目标
     *
     * @param userId 用户ID
     * @param goalDuration 目标时长(分钟)
     */
    @Transactional
    public void setUserGoal(String userId, int goalDuration) {
        log.info("设置用户学习目标 - 用户ID: {}, 目标时长: {}分钟", userId, goalDuration);

        LocalDate today = LocalDate.now();
        Optional<StudyProgress> todayProgress = studyProgressRepository
                .findByUserIdAndProgressDateAndProgressType(userId, today, StudyProgress.ProgressType.DAILY);

        if (todayProgress.isPresent()) {
            StudyProgress progress = todayProgress.get();
            progress.setGoalDuration(goalDuration);
            studyProgressRepository.save(progress);
        }
    }

    /**
     * 定时任务：每日凌晨更新所有用户的学习进度
     */
    @Scheduled(cron = "0 0 1 * * ?") // 每日凌晨1点执行
    @Async
    public void scheduledUpdateAllUsersProgress() {
        log.info("开始定时更新所有用户的学习进度");

        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 获取所有有学习记录的用户
        List<String> userIds = studyRecordRepository.findAllUsersWithStudyRecords();

        for (String userId : userIds) {
            try {
                updateDailyProgress(userId, yesterday);

                // 如果是周一，更新上周进度
                if (yesterday.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                    updateWeeklyProgress(userId, yesterday);
                }

                // 如果是月末，更新上月进度
                if (yesterday.equals(yesterday.withDayOfMonth(yesterday.lengthOfMonth()))) {
                    updateMonthlyProgress(userId, yesterday);
                }

            } catch (Exception e) {
                log.error("更新用户学习进度失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            }
        }

        log.info("定时更新所有用户的学习进度完成 - 处理用户数: {}", userIds.size());
    }

    /**
     * 定时任务：清理过期的学习进度记录
     */
    @Scheduled(cron = "0 0 2 1 * ?") // 每月1日凌晨2点执行
    @Async
    public void scheduledCleanExpiredProgress() {
        log.info("开始清理过期的学习进度记录");

        // 删除6个月前的每日记录
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        studyProgressRepository.deleteByProgressDateBefore(sixMonthsAgo);

        log.info("清理过期的学习进度记录完成");
    }

    /**
     * 更新连续学习天数
     *
     * @param userId 用户ID
     * @param date 日期
     * @param dailyProgress 每日进度
     */
    private void updateContinuousDays(String userId, LocalDate date, StudyProgress dailyProgress) {
        // 获取昨天的进度记录
        LocalDate yesterday = date.minusDays(1);
        Optional<StudyProgress> yesterdayProgress = studyProgressRepository
                .findByUserIdAndProgressDateAndProgressType(userId, yesterday, StudyProgress.ProgressType.DAILY);

        if (dailyProgress.getStudyDuration() > 0) {
            // 今天有学习
            if (yesterdayProgress.isPresent() && yesterdayProgress.get().getStudyDuration() > 0) {
                // 昨天也有学习，连续天数+1
                dailyProgress.setContinuousDays(yesterdayProgress.get().getContinuousDays() + 1);
            } else {
                // 昨天没有学习，重新开始计算
                dailyProgress.setContinuousDays(1);
            }
        } else {
            // 今天没有学习，连续天数归零
            dailyProgress.setContinuousDays(0);
        }
    }

    /**
     * 计算用户成就
     *
     * @param achievementData 成就数据
     * @return 成就列表
     */
    private List<Map<String, Object>> calculateAchievements(Object[] achievementData) {
        List<Map<String, Object>> achievements = new ArrayList<>();

        Long goalAchievedDays = (Long) achievementData[0];
        Integer maxContinuousDays = (Integer) achievementData[1];
        Double avgActivityScore = (Double) achievementData[2];
        Long totalStudyTime = (Long) achievementData[3];
        Long totalCoursesCompleted = (Long) achievementData[4];

        // 目标达成成就
        if (goalAchievedDays != null && goalAchievedDays >= 30) {
            achievements.add(createAchievement("目标达人", "连续30天达成学习目标", "gold"));
        } else if (goalAchievedDays != null && goalAchievedDays >= 7) {
            achievements.add(createAchievement("目标新星", "连续7天达成学习目标", "silver"));
        }

        // 连续学习成就
        if (maxContinuousDays != null && maxContinuousDays >= 30) {
            achievements.add(createAchievement("学习达人", "连续学习30天", "gold"));
        } else if (maxContinuousDays != null && maxContinuousDays >= 7) {
            achievements.add(createAchievement("坚持不懈", "连续学习7天", "silver"));
        }

        // 活跃度成就
        if (avgActivityScore != null && avgActivityScore >= 90) {
            achievements.add(createAchievement("超级活跃", "平均活跃度90+", "gold"));
        } else if (avgActivityScore != null && avgActivityScore >= 70) {
            achievements.add(createAchievement("活跃学者", "平均活跃度70+", "silver"));
        }

        // 学习时长成就
        if (totalStudyTime != null && totalStudyTime >= 6000) { // 100小时
            achievements.add(createAchievement("学习之王", "累计学习100小时", "gold"));
        } else if (totalStudyTime != null && totalStudyTime >= 1800) { // 30小时
            achievements.add(createAchievement("学习能手", "累计学习30小时", "silver"));
        }

        // 完成课程成就
        if (totalCoursesCompleted != null && totalCoursesCompleted >= 50) {
            achievements.add(createAchievement("课程大师", "完成50门课程", "gold"));
        } else if (totalCoursesCompleted != null && totalCoursesCompleted >= 10) {
            achievements.add(createAchievement("课程高手", "完成10门课程", "silver"));
        }

        return achievements;
    }

    /**
     * 创建成就对象
     *
     * @param name 成就名称
     * @param description 成就描述
     * @param level 成就等级
     * @return 成就对象
     */
    private Map<String, Object> createAchievement(String name, String description, String level) {
        Map<String, Object> achievement = new HashMap<>();
        achievement.put("name", name);
        achievement.put("description", description);
        achievement.put("level", level);
        achievement.put("achievedAt", LocalDate.now());
        return achievement;
    }
}