package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.StudyRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 学习记录数据仓储接口
 * 提供学习记录的数据访问操作
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
     * 根据用户ID和章节ID查询学习记录
     *
     * @param userId 用户ID
     * @param chapterId 章节ID
     * @return 学习记录
     */
    Optional<StudyRecord> findByUserIdAndChapterId(String userId, String chapterId);

    /**
     * 根据用户ID和学习状态查询学习记录
     *
     * @param userId 用户ID
     * @param status 学习状态
     * @param pageable 分页参数
     * @return 学习记录分页
     */
    Page<StudyRecord> findByUserIdAndStudyStatus(String userId, StudyRecord.StudyStatus status, Pageable pageable);

    /**
     * 查询用户正在学习的课程
     *
     * @param userId 用户ID
     * @return 正在学习的记录列表
     */
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.userId = :userId AND sr.studyStatus = 'IN_PROGRESS' ORDER BY sr.lastStudyTime DESC")
    List<StudyRecord> findInProgressByUserId(@Param("userId") String userId);

    /**
     * 查询用户已完成的课程
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 已完成的记录分页
     */
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.userId = :userId AND sr.studyStatus = 'COMPLETED' ORDER BY sr.completionTime DESC")
    Page<StudyRecord> findCompletedByUserId(@Param("userId") String userId, Pageable pageable);

    /**
     * 查询用户收藏的课程
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 收藏的记录分页
     */
    Page<StudyRecord> findByUserIdAndIsFavorited(String userId, Boolean isFavorited, Pageable pageable);

    /**
     * 统计用户学习记录数量
     *
     * @param userId 用户ID
     * @return 学习记录总数
     */
    long countByUserId(String userId);

    /**
     * 统计用户已完成的课程数量
     *
     * @param userId 用户ID
     * @return 已完成课程数量
     */
    @Query("SELECT COUNT(sr) FROM StudyRecord sr WHERE sr.userId = :userId AND sr.studyStatus = 'COMPLETED'")
    long countCompletedByUserId(@Param("userId") String userId);

    /**
     * 统计用户正在学习的课程数量
     *
     * @param userId 用户ID
     * @return 正在学习课程数量
     */
    @Query("SELECT COUNT(sr) FROM StudyRecord sr WHERE sr.userId = :userId AND sr.studyStatus = 'IN_PROGRESS'")
    long countInProgressByUserId(@Param("userId") String userId);

    /**
     * 统计用户总学习时长
     *
     * @param userId 用户ID
     * @return 总学习时长(分钟)
     */
    @Query("SELECT COALESCE(SUM(sr.studyDuration), 0) FROM StudyRecord sr WHERE sr.userId = :userId")
    long sumStudyDurationByUserId(@Param("userId") String userId);

    /**
     * 统计用户在指定时间范围内的学习时长
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 学习时长(分钟)
     */
    @Query("SELECT COALESCE(SUM(sr.studyDuration), 0) FROM StudyRecord sr WHERE sr.userId = :userId AND sr.lastStudyTime BETWEEN :startTime AND :endTime")
    long sumStudyDurationByUserIdAndTimeRange(@Param("userId") String userId,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 查询用户最近的学习记录
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 最近的学习记录列表
     */
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.userId = :userId ORDER BY sr.lastStudyTime DESC")
    List<StudyRecord> findRecentByUserId(@Param("userId") String userId, Pageable pageable);

    /**
     * 查询课程的学习记录统计
     *
     * @param courseId 课程ID
     * @return 学习记录统计
     */
    @Query("SELECT COUNT(sr) as totalStudents, " +
            "COUNT(CASE WHEN sr.studyStatus = 'COMPLETED' THEN 1 END) as completedStudents, " +
            "AVG(sr.progressPercent) as avgProgress, " +
            "SUM(sr.studyDuration) as totalDuration " +
            "FROM StudyRecord sr WHERE sr.courseId = :courseId")
    Object[] getCourseStudyStatistics(@Param("courseId") String courseId);

    /**
     * 查询用户的学习热点时间
     *
     * @param userId 用户ID
     * @return 学习时间统计
     */
    @Query("SELECT HOUR(sr.lastStudyTime) as hour, COUNT(sr) as studyCount " +
            "FROM StudyRecord sr WHERE sr.userId = :userId " +
            "GROUP BY HOUR(sr.lastStudyTime) " +
            "ORDER BY studyCount DESC")
    List<Object[]> getUserStudyHours(@Param("userId") String userId);

    /**
     * 查询用户的学习趋势
     *
     * @param userId 用户ID
     * @param days 统计天数
     * @return 学习趋势数据
     */
    @Query("SELECT DATE(sr.lastStudyTime) as studyDate, " +
            "COUNT(sr) as studyCount, " +
            "SUM(sr.studyDuration) as totalDuration " +
            "FROM StudyRecord sr WHERE sr.userId = :userId " +
            "AND sr.lastStudyTime >= :startDate " +
            "GROUP BY DATE(sr.lastStudyTime) " +
            "ORDER BY studyDate DESC")
    List<Object[]> getUserStudyTrend(@Param("userId") String userId,
                                     @Param("startDate") LocalDateTime startDate);

    /**
     * 查询用户的学习排名
     *
     * @param userId 用户ID
     * @return 排名信息
     */
    @Query("SELECT COUNT(DISTINCT sr2.userId) + 1 as ranking " +
            "FROM StudyRecord sr1, StudyRecord sr2 " +
            "WHERE sr1.userId = :userId " +
            "AND sr2.studyDuration > sr1.studyDuration")
    Long getUserRanking(@Param("userId") String userId);

    /**
     * 查询学习时长排行榜
     *
     * @param limit 限制数量
     * @return 排行榜数据
     */
    @Query("SELECT sr.userId, u.username, u.realName, SUM(sr.studyDuration) as totalDuration " +
            "FROM StudyRecord sr JOIN User u ON sr.userId = u.id " +
            "GROUP BY sr.userId, u.username, u.realName " +
            "ORDER BY totalDuration DESC")
    List<Object[]> getStudyTimeRanking(Pageable pageable);

    /**
     * 查询完成课程数排行榜
     *
     * @param limit 限制数量
     * @return 排行榜数据
     */
    @Query("SELECT sr.userId, u.username, u.realName, COUNT(sr) as completedCount " +
            "FROM StudyRecord sr JOIN User u ON sr.userId = u.id " +
            "WHERE sr.studyStatus = 'COMPLETED' " +
            "GROUP BY sr.userId, u.username, u.realName " +
            "ORDER BY completedCount DESC")
    List<Object[]> getCompletionRanking(Pageable pageable);

    /**
     * 获取所有有学习记录的用户
     *
     * @return 用户ID列表
     */
    @Query("SELECT DISTINCT sr.userId FROM StudyRecord sr")
    List<String> findAllUsersWithStudyRecords();

    /**
     * 统计用户收藏的课程数量
     *
     * @param userId 用户ID
     * @param isFavorited 是否收藏
     * @return 收藏课程数量
     */
    long countByUserIdAndIsFavorited(String userId, Boolean isFavorited);

    /**
     * 删除用户的学习记录
     *
     * @param userId 用户ID
     */
    void deleteByUserId(String userId);

    /**
     * 删除课程的学习记录
     *
     * @param courseId 课程ID
     */
    void deleteByCourseId(String courseId);
}