package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.StudyRecord;
import com.example.smarttrainingsystem.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 学习记录数据仓储接口
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Repository
public interface StudyRecordRepository extends JpaRepository<StudyRecord, String> {

    /**
     * 根据用户ID查询学习记录
     *
     * @param userId 用户ID
     * @return 学习记录列表
     */
    List<StudyRecord> findByUserId(String userId);

    /**
     * 根据用户ID查询学习记录（分页）
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 学习记录分页
     */
    Page<StudyRecord> findByUserId(String userId, Pageable pageable);

    /**
     * 根据用户ID和课程ID查询学习记录
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 学习记录
     */
    Optional<StudyRecord> findByUserIdAndCourseId(String userId, String courseId);

    /**
     * 根据课程ID查询学习记录
     *
     * @param courseId 课程ID
     * @return 学习记录列表
     */
    List<StudyRecord> findByCourseId(String courseId);

    /**
     * 统计用户的学习记录数量
     *
     * @param userId 用户ID
     * @return 记录数量
     */
    long countByUserId(String userId);

    /**
     * 根据用户ID和状态统计学习记录
     *
     * @param userId 用户ID
     * @param status 学习状态
     * @return 记录数量
     */
    long countByUserIdAndStatus(String userId, StudyRecord.Status status);

    /**
     * 根据用户ID和学习状态查询学习记录
     *
     * @param userId 用户ID
     * @param status 学习状态
     * @return 学习记录列表
     */
    List<StudyRecord> findByUserIdAndStatus(String userId, StudyRecord.Status status);

    /**
     * 根据用户ID和学习状态查询学习记录（分页）
     *
     * @param userId 用户ID
     * @param status 学习状态
     * @param pageable 分页参数
     * @return 学习记录分页
     */
    Page<StudyRecord> findByUserIdAndStatus(String userId, StudyRecord.Status status, Pageable pageable);

    /**
     * 根据用户ID和学习状态查询学习记录（兼容旧方法名）
     *
     * @param userId 用户ID
     * @param status 学习状态
     * @param pageable 分页参数
     * @return 学习记录分页
     */
    default Page<StudyRecord> findByUserIdAndStudyStatus(String userId, StudyRecord.Status status, Pageable pageable) {
        return findByUserIdAndStatus(userId, status, pageable);
    }

    /**
     * 根据用户ID和收藏状态查询学习记录
     *
     * @param userId 用户ID
     * @param isFavorited 是否收藏
     * @param pageable 分页参数
     * @return 学习记录分页
     */
    Page<StudyRecord> findByUserIdAndIsFavorited(String userId, Boolean isFavorited, Pageable pageable);

    /**
     * 统计用户已完成的课程数量
     *
     * @param userId 用户ID
     * @return 完成课程数量
     */
    @Query("SELECT COUNT(*) FROM StudyRecord sr WHERE sr.userId = :userId AND sr.status = 'COMPLETED'")
    long countCompletedByUserId(@Param("userId") String userId);

    /**
     * 统计用户学习总时长(秒)
     */
    @Query("SELECT COALESCE(SUM(COALESCE(sr.studyDuration, sr.studyTime, 0)),0) FROM StudyRecord sr WHERE sr.userId = :userId")
    Long sumDurationByUserId(@Param("userId") String userId);

    /**
     * 查询用户学习过的课程
     */
    @Query("SELECT sr.course FROM StudyRecord sr WHERE sr.userId = :userId")
    List<Course> findCoursesByUserId(@Param("userId") String userId);

    /**
     * 统计用户正在学习的课程数量
     *
     * @param userId 用户ID
     * @return 正在学习课程数量
     */
    @Query("SELECT COUNT(*) FROM StudyRecord sr WHERE sr.userId = :userId AND sr.status = 'IN_PROGRESS'")
    long countInProgressByUserId(@Param("userId") String userId);

    /**
     * 统计用户收藏的课程数量
     *
     * @param userId 用户ID
     * @param isFavorited 是否收藏
     * @return 收藏课程数量
     */
    long countByUserIdAndIsFavorited(String userId, Boolean isFavorited);

    /**
     * 统计用户的总学习时长（兼容多个字段名）
     *
     * @param userId 用户ID
     * @return 学习时长（分钟）
     */
    @Query("SELECT COALESCE(SUM(COALESCE(sr.studyTime, 0)), 0) + COALESCE(SUM(COALESCE(sr.studyDuration, 0)), 0) FROM StudyRecord sr WHERE sr.userId = :userId")
    Long sumStudyTimeByUserId(@Param("userId") String userId);

    /**
     * 统计用户的总学习时长（兼容方法名）
     *
     * @param userId 用户ID
     * @return 学习时长（分钟）
     */
    default Long sumStudyDurationByUserId(String userId) {
        return sumStudyTimeByUserId(userId);
    }

    /**
     * 统计指定时间范围内的学习时长
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 学习时长（分钟）
     */
    @Query("SELECT COALESCE(SUM(COALESCE(sr.studyTime, 0)), 0) + COALESCE(SUM(COALESCE(sr.studyDuration, 0)), 0) " +
            "FROM StudyRecord sr WHERE sr.userId = :userId " +
            "AND (sr.lastStudyAt BETWEEN :startTime AND :endTime OR sr.lastStudyTime BETWEEN :startTime AND :endTime)")
    long sumStudyDurationByUserIdAndTimeRange(@Param("userId") String userId,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 统计活跃学员数量（指定时间之后有学习记录）
     *
     * @param sinceDate 起始时间
     * @return 活跃学员数量
     */
    @Query("SELECT COUNT(DISTINCT sr.userId) FROM StudyRecord sr WHERE " +
            "(sr.lastStudyAt >= :sinceDate OR sr.lastStudyTime >= :sinceDate)")
    long countActiveStudents(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * 统计有完成课程的学员数量
     *
     * @return 学员数量
     */
    @Query("SELECT COUNT(DISTINCT sr.userId) FROM StudyRecord sr WHERE sr.status = 'COMPLETED'")
    long countStudentsWithCompletedCourses();

    /**
     * 查询用户最近的学习记录
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 学习记录列表
     */
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.userId = :userId " +
            "ORDER BY COALESCE(sr.lastStudyAt, sr.lastStudyTime, sr.createdAt) DESC")
    List<StudyRecord> findRecentByUserId(@Param("userId") String userId, Pageable pageable);

    /**
     * 查询用户最近的学习记录（兼容方法名）
     *
     * @param userId 用户ID
     * @return 学习记录列表
     */
    @Query(value = "SELECT sr FROM StudyRecord sr WHERE sr.userId = :userId " +
            "ORDER BY COALESCE(sr.lastStudyAt, sr.lastStudyTime, sr.createdAt) DESC")
    List<StudyRecord> findRecentStudyRecords(@Param("userId") String userId, Pageable pageable);

    /**
     * 查询用户最近的学习记录（方法重载，使用limit参数）
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 学习记录列表
     */
    default List<StudyRecord> findRecentStudyRecords(String userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return findRecentStudyRecords(userId, pageable);
    }

    /**
     * 查询用户正在学习的课程
     *
     * @param userId 用户ID
     * @return 学习记录列表
     */
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.userId = :userId AND sr.status = 'IN_PROGRESS' " +
            "ORDER BY COALESCE(sr.lastStudyAt, sr.lastStudyTime, sr.createdAt) DESC")
    List<StudyRecord> findInProgressByUserId(@Param("userId") String userId);

    /**
     * 查询用户已完成的课程
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 学习记录分页
     */
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.userId = :userId AND sr.status = 'COMPLETED' " +
            "ORDER BY COALESCE(sr.completedAt, sr.completionTime, sr.updatedAt) DESC")
    Page<StudyRecord> findCompletedByUserId(@Param("userId") String userId, Pageable pageable);

    /**
     * 获取用户学习趋势数据
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @return 趋势数据 [日期, 学习课程数, 学习时长]
     */
    @Query("SELECT CAST(COALESCE(sr.lastStudyAt, sr.lastStudyTime, sr.createdAt) AS date) as studyDate, " +
            "COUNT(DISTINCT sr.courseId) as coursesStudied, " +
            "SUM(COALESCE(sr.studyTime, 0) + COALESCE(sr.studyDuration, 0)) as totalStudyTime " +
            "FROM StudyRecord sr WHERE sr.userId = :userId " +
            "AND COALESCE(sr.lastStudyAt, sr.lastStudyTime, sr.createdAt) >= :startDate " +
            "GROUP BY CAST(COALESCE(sr.lastStudyAt, sr.lastStudyTime, sr.createdAt) AS date) " +
            "ORDER BY CAST(COALESCE(sr.lastStudyAt, sr.lastStudyTime, sr.createdAt) AS date) DESC")
    List<Object[]> getUserStudyTrend(@Param("userId") String userId, @Param("startDate") LocalDateTime startDate);

    /**
     * 根据日期范围查询学习记录
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 学习记录列表
     */
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.userId = :userId " +
            "AND COALESCE(sr.lastStudyAt, sr.lastStudyTime, sr.createdAt) BETWEEN :startDate AND :endDate " +
            "ORDER BY COALESCE(sr.lastStudyAt, sr.lastStudyTime, sr.createdAt) DESC")
    List<StudyRecord> findByUserIdAndDateRange(@Param("userId") String userId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * 根据日期范围查询学习记录（分页）
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageable 分页参数
     * @return 学习记录分页
     */
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.userId = :userId " +
            "AND COALESCE(sr.lastStudyAt, sr.lastStudyTime, sr.createdAt) BETWEEN :startDate AND :endDate " +
            "ORDER BY COALESCE(sr.lastStudyAt, sr.lastStudyTime, sr.createdAt) DESC")
    Page<StudyRecord> findByUserIdAndDateRange(@Param("userId") String userId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               Pageable pageable);

    /**
     * 获取学习时长排行榜
     *
     * @param pageable 分页参数
     * @return 排行榜数据 [用户ID, 总学习时长]
     */
    @Query("SELECT sr.userId, SUM(COALESCE(sr.studyTime, 0) + COALESCE(sr.studyDuration, 0)) as totalTime " +
            "FROM StudyRecord sr " +
            "GROUP BY sr.userId " +
            "ORDER BY totalTime DESC")
    List<Object[]> getStudyTimeRanking(Pageable pageable);

    /**
     * 获取完成课程数排行榜
     *
     * @param pageable 分页参数
     * @return 排行榜数据 [用户ID, 完成课程数]
     */
    @Query("SELECT sr.userId, COUNT(*) as completedCount " +
            "FROM StudyRecord sr WHERE sr.status = 'COMPLETED' " +
            "GROUP BY sr.userId " +
            "ORDER BY completedCount DESC")
    List<Object[]> getCompletionRanking(Pageable pageable);

    /**
     * 查询课程的学习统计
     *
     * @param courseId 课程ID
     * @return 统计数据 [总学员数, 完成学员数, 平均进度]
     */
    @Query("SELECT " +
            "COUNT(*) as totalStudents, " +
            "SUM(CASE WHEN sr.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedStudents, " +
            "AVG(COALESCE(sr.progress, sr.progressPercent, 0)) as averageProgress " +
            "FROM StudyRecord sr WHERE sr.courseId = :courseId")
    Object[] getCourseStudyStatistics(@Param("courseId") String courseId);

    /**
     * 删除学习记录（软删除或硬删除）
     *
     * @param userId 用户ID
     * @param recordId 记录ID
     */
    @Modifying
    @Query("DELETE FROM StudyRecord sr WHERE sr.id = :recordId AND sr.userId = :userId")
    void deleteByIdAndUserId(@Param("recordId") String recordId, @Param("userId") String userId);

    /**
     * 根据用户ID和日期查询当日学习记录
     *
     * @param userId 用户ID
     * @param startOfDay 当日开始时间
     * @param endOfDay 当日结束时间
     * @return 学习记录列表
     */
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.userId = :userId " +
            "AND (sr.lastStudyAt BETWEEN :startOfDay AND :endOfDay " +
            "OR sr.lastStudyTime BETWEEN :startOfDay AND :endOfDay " +
            "OR sr.createdAt BETWEEN :startOfDay AND :endOfDay)")
    List<StudyRecord> findByUserIdAndDay(@Param("userId") String userId,
                                         @Param("startOfDay") LocalDateTime startOfDay,
                                         @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 统计用户在指定日期的学习课程数
     *
     * @param userId 用户ID
     * @param startOfDay 当日开始时间
     * @param endOfDay 当日结束时间
     * @return 学习课程数
     */
    @Query("SELECT COUNT(DISTINCT sr.courseId) FROM StudyRecord sr WHERE sr.userId = :userId " +
            "AND (sr.lastStudyAt BETWEEN :startOfDay AND :endOfDay " +
            "OR sr.lastStudyTime BETWEEN :startOfDay AND :endOfDay " +
            "OR sr.createdAt BETWEEN :startOfDay AND :endOfDay)")
    long countDistinctCoursesByUserIdAndDay(@Param("userId") String userId,
                                            @Param("startOfDay") LocalDateTime startOfDay,
                                            @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 统计用户在指定日期完成的课程数
     *
     * @param userId 用户ID
     * @param startOfDay 当日开始时间
     * @param endOfDay 当日结束时间
     * @return 完成课程数
     */
    @Query("SELECT COUNT(DISTINCT sr.courseId) FROM StudyRecord sr WHERE sr.userId = :userId " +
            "AND sr.status = 'COMPLETED' " +
            "AND (sr.completedAt BETWEEN :startOfDay AND :endOfDay " +
            "OR sr.completionTime BETWEEN :startOfDay AND :endOfDay)")
    long countCompletedCoursesByUserIdAndDay(@Param("userId") String userId,
                                             @Param("startOfDay") LocalDateTime startOfDay,
                                             @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 统计用户在指定日期完成的章节数
     *
     * @param userId 用户ID
     * @param startOfDay 当日开始时间
     * @param endOfDay 当日结束时间
     * @return 完成章节数
     */
    @Query("SELECT COUNT(*) FROM StudyRecord sr WHERE sr.userId = :userId " +
            "AND sr.status = 'COMPLETED' " +
            "AND (sr.completedAt BETWEEN :startOfDay AND :endOfDay " +
            "OR sr.completionTime BETWEEN :startOfDay AND :endOfDay)")
    long countCompletedChaptersByUserIdAndDay(@Param("userId") String userId,
                                              @Param("startOfDay") LocalDateTime startOfDay,
                                              @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 查找所有有学习记录的用户ID
     *
     * @return 用户ID列表
     */
    @Query("SELECT DISTINCT sr.userId FROM StudyRecord sr")
    List<String> findAllUsersWithStudyRecords();

    /**
     * 查找所有有学习记录的用户ID（分页）
     *
     * @param pageable 分页参数
     * @return 用户ID分页列表
     */
    @Query("SELECT DISTINCT sr.userId FROM StudyRecord sr")
    Page<String> findAllUsersWithStudyRecords(Pageable pageable);

    /**
     * 查找在指定时间后有学习活动的用户ID
     *
     * @param sinceDate 起始时间
     * @return 用户ID列表
     */
    @Query("SELECT DISTINCT sr.userId FROM StudyRecord sr WHERE " +
            "(sr.lastStudyAt >= :sinceDate OR sr.lastStudyTime >= :sinceDate OR sr.createdAt >= :sinceDate)")
    List<String> findActiveUsersWithStudyRecords(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * 查找在指定时间后有学习活动的用户ID（分页）
     *
     * @param sinceDate 起始时间
     * @param pageable 分页参数
     * @return 用户ID分页列表
     */
    @Query("SELECT DISTINCT sr.userId FROM StudyRecord sr WHERE " +
            "(sr.lastStudyAt >= :sinceDate OR sr.lastStudyTime >= :sinceDate OR sr.createdAt >= :sinceDate)")
    Page<String> findActiveUsersWithStudyRecords(@Param("sinceDate") LocalDateTime sinceDate, Pageable pageable);
}