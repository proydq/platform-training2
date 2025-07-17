package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.StudyProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 学习进度数据仓储接口
 * 提供学习进度统计的数据访问操作
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Repository
public interface StudyProgressRepository extends JpaRepository<StudyProgress, String> {

    /**
     * 根据用户ID和日期查询学习进度
     *
     * @param userId 用户ID
     * @param progressDate 进度日期
     * @param progressType 进度类型
     * @return 学习进度
     */
    Optional<StudyProgress> findByUserIdAndProgressDateAndProgressType(
            String userId, LocalDate progressDate, StudyProgress.ProgressType progressType);

    /**
     * 根据用户ID查询学习进度
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 学习进度分页
     */
    Page<StudyProgress> findByUserId(String userId, Pageable pageable);

    /**
     * 根据用户ID和进度类型查询学习进度
     *
     * @param userId 用户ID
     * @param progressType 进度类型
     * @param pageable 分页参数
     * @return 学习进度分页
     */
    Page<StudyProgress> findByUserIdAndProgressType(String userId, StudyProgress.ProgressType progressType, Pageable pageable);

    /**
     * 查询用户指定日期范围内的学习进度
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param progressType 进度类型
     * @return 学习进度列表
     */
    @Query("SELECT sp FROM StudyProgress sp WHERE sp.userId = :userId AND sp.progressDate BETWEEN :startDate AND :endDate AND sp.progressType = :progressType ORDER BY sp.progressDate DESC")
    List<StudyProgress> findByUserIdAndDateRange(@Param("userId") String userId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate,
                                                 @Param("progressType") StudyProgress.ProgressType progressType);

    /**
     * 查询用户最近的学习进度
     *
     * @param userId 用户ID
     * @param progressType 进度类型
     *
     * @return 最近的学习进度列表
     */
    @Query("SELECT sp FROM StudyProgress sp WHERE sp.userId = :userId AND sp.progressType = :progressType ORDER BY sp.progressDate DESC")
    List<StudyProgress> findRecentByUserId(@Param("userId") String userId,
                                           @Param("progressType") StudyProgress.ProgressType progressType,
                                           Pageable pageable);

    /**
     * 统计用户总学习天数
     *
     * @param userId 用户ID
     * @return 总学习天数
     */
    @Query("SELECT COUNT(sp) FROM StudyProgress sp WHERE sp.userId = :userId AND sp.progressType = 'DAILY' AND sp.studyDuration > 0")
    long countStudyDaysByUserId(@Param("userId") String userId);

    /**
     * 统计用户目标达成天数
     *
     * @param userId 用户ID
     * @return 目标达成天数
     */
    @Query("SELECT COUNT(sp) FROM StudyProgress sp WHERE sp.userId = :userId AND sp.progressType = 'DAILY' AND sp.goalAchieved = true")
    long countGoalAchievedDaysByUserId(@Param("userId") String userId);

    /**
     * 计算用户平均学习时长
     *
     * @param userId 用户ID
     * @param progressType 进度类型
     * @return 平均学习时长
     */
    @Query("SELECT AVG(sp.studyDuration) FROM StudyProgress sp WHERE sp.userId = :userId AND sp.progressType = :progressType AND sp.studyDuration > 0")
    Double getAverageStudyDurationByUserId(@Param("userId") String userId,
                                           @Param("progressType") StudyProgress.ProgressType progressType);

    /**
     * 计算用户平均活跃度分数
     *
     * @param userId 用户ID
     * @param progressType 进度类型
     * @return 平均活跃度分数
     */
    @Query("SELECT AVG(sp.activityScore) FROM StudyProgress sp WHERE sp.userId = :userId AND sp.progressType = :progressType")
    Double getAverageActivityScoreByUserId(@Param("userId") String userId,
                                           @Param("progressType") StudyProgress.ProgressType progressType);

    /**
     * 查询用户最高连续学习天数
     *
     * @param userId 用户ID
     * @return 最高连续学习天数
     */
    @Query("SELECT MAX(sp.continuousDays) FROM StudyProgress sp WHERE sp.userId = :userId AND sp.progressType = 'DAILY'")
    Integer getMaxContinuousDaysByUserId(@Param("userId") String userId);

    /**
     * 查询用户当前连续学习天数
     *
     * @param userId 用户ID
     * @param pageable 分页参数(用于限制结果数量)
     * @return 当前连续学习天数
     */
    @Query("SELECT sp.continuousDays FROM StudyProgress sp WHERE sp.userId = :userId AND sp.progressType = 'DAILY' ORDER BY sp.progressDate DESC")
    List<Integer> getCurrentContinuousDaysByUserIdWithPageable(@Param("userId") String userId, Pageable pageable);

    /**
     * 查询用户当前连续学习天数 (默认实现)
     *
     * @param userId 用户ID
     * @return 当前连续学习天数
     */
    default Integer getCurrentContinuousDaysByUserId(String userId) {
        List<Integer> results = getCurrentContinuousDaysByUserIdWithPageable(userId, PageRequest.of(0, 1));
        return results.isEmpty() ? 0 : results.get(0);
    }

    /**
     * 查询用户学习进度趋势
     *
     * @param userId 用户ID
     *
     * @return 学习进度趋势数据
     */
    @Query("SELECT sp.progressDate, sp.studyDuration, sp.coursesCompleted, sp.activityScore, sp.goalAchieved " +
            "FROM StudyProgress sp WHERE sp.userId = :userId AND sp.progressType = 'DAILY' " +
            "AND sp.progressDate >= :startDate " +
            "ORDER BY sp.progressDate DESC")
    List<Object[]> getUserProgressTrend(@Param("userId") String userId,
                                        @Param("startDate") LocalDate startDate);

    /**
     * 查询活跃度排行榜
     *
     * @param progressType 进度类型
     * @param date 指定日期
     * @return 活跃度排行榜
     */
    @Query("SELECT sp.userId, u.username, u.realName, sp.activityScore, sp.studyDuration, sp.coursesCompleted " +
            "FROM StudyProgress sp JOIN User u ON sp.userId = u.id " +
            "WHERE sp.progressType = :progressType AND sp.progressDate = :date " +
            "ORDER BY sp.activityScore DESC, sp.studyDuration DESC")
    List<Object[]> getActivityRanking(@Param("progressType") StudyProgress.ProgressType progressType,
                                      @Param("date") LocalDate date,
                                      Pageable pageable);

    /**
     * 查询学习时长排行榜
     *
     * @param progressType 进度类型
     * @param date 指定日期
     * @return 学习时长排行榜
     */
    @Query("SELECT sp.userId, u.username, u.realName, sp.studyDuration, sp.coursesCompleted, sp.activityScore " +
            "FROM StudyProgress sp JOIN User u ON sp.userId = u.id " +
            "WHERE sp.progressType = :progressType AND sp.progressDate = :date " +
            "ORDER BY sp.studyDuration DESC")
    List<Object[]> getStudyTimeRanking(@Param("progressType") StudyProgress.ProgressType progressType,
                                       @Param("date") LocalDate date,
                                       Pageable pageable);

    /**
     * 查询系统整体学习统计
     *
     * @param date 指定日期
     * @param progressType 进度类型
     * @return 系统统计数据
     */
    @Query("SELECT COUNT(sp) as totalUsers, " +
            "SUM(sp.studyDuration) as totalStudyTime, " +
            "SUM(sp.coursesCompleted) as totalCoursesCompleted, " +
            "AVG(sp.activityScore) as avgActivityScore, " +
            "COUNT(CASE WHEN sp.goalAchieved = true THEN 1 END) as goalAchievedCount " +
            "FROM StudyProgress sp WHERE sp.progressDate = :date AND sp.progressType = :progressType")
    Object[] getSystemStatistics(@Param("date") LocalDate date,
                                 @Param("progressType") StudyProgress.ProgressType progressType);

    /**
     * 查询用户学习成就统计
     *
     * @param userId 用户ID
     * @return 学习成就统计
     */
    @Query("SELECT COUNT(CASE WHEN sp.goalAchieved = true THEN 1 END) as goalAchievedDays, " +
            "MAX(sp.continuousDays) as maxContinuousDays, " +
            "AVG(sp.activityScore) as avgActivityScore, " +
            "SUM(sp.studyDuration) as totalStudyTime, " +
            "SUM(sp.coursesCompleted) as totalCoursesCompleted " +
            "FROM StudyProgress sp WHERE sp.userId = :userId AND sp.progressType = 'DAILY'")
    Object[] getUserAchievementStats(@Param("userId") String userId);

    /**
     * 查询用户学习日历数据
     *
     * @param userId 用户ID
     * @param year 年份
     * @param month 月份
     * @return 学习日历数据
     */
    @Query("SELECT sp.progressDate, sp.studyDuration, sp.goalAchieved, sp.activityScore " +
            "FROM StudyProgress sp WHERE sp.userId = :userId AND sp.progressType = 'DAILY' " +
            "AND YEAR(sp.progressDate) = :year AND MONTH(sp.progressDate) = :month " +
            "ORDER BY sp.progressDate")
    List<Object[]> getUserCalendarData(@Param("userId") String userId,
                                       @Param("year") int year,
                                       @Param("month") int month);

    /**
     * 删除用户的学习进度记录
     *
     * @param userId 用户ID
     */
    void deleteByUserId(String userId);

    /**
     * 删除指定日期之前的学习进度记录
     *
     * @param date 日期
     */
    void deleteByProgressDateBefore(LocalDate date);
}