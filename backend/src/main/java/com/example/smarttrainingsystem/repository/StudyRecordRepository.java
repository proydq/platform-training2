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
     * 统计用户的总学习时长
     *
     * @param userId 用户ID
     * @return 学习时长（分钟）
     */
    @Query("SELECT COALESCE(SUM(sr.studyTime), 0) FROM StudyRecord sr WHERE sr.userId = :userId")
    Integer sumStudyTimeByUserId(@Param("userId") String userId);

    /**
     * 统计活跃学员数量（指定时间之后有学习记录）
     *
     * @param sinceDate 起始时间
     * @return 活跃学员数量
     */
    @Query("SELECT COUNT(DISTINCT sr.userId) FROM StudyRecord sr WHERE sr.lastStudyAt >= :sinceDate")
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
     * @param limit 限制数量
     * @return 学习记录列表
     */
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.userId = :userId ORDER BY sr.lastStudyAt DESC")
    List<StudyRecord> findRecentStudyRecords(@Param("userId") String userId, @Param("limit") int limit);

    /**
     * 查询用户正在学习的课程
     *
     * @param userId 用户ID
     * @return 学习记录列表
     */
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.userId = :userId AND sr.status = 'IN_PROGRESS' ORDER BY sr.lastStudyAt DESC")
    List<StudyRecord> findInProgressByUserId(@Param("userId") String userId);

    /**
     * 查询用户已完成的课程
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 学习记录分页
     */
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.userId = :userId AND sr.status = 'COMPLETED' ORDER BY sr.completedAt DESC")
    Page<StudyRecord> findCompletedByUserId(@Param("userId") String userId, Pageable pageable);

    /**
     * 查询课程的学习统计
     *
     * @param courseId 课程ID
     * @return 统计数据 [总学员数, 完成学员数, 平均进度]
     */
    @Query("SELECT " +
            "COUNT(*) as totalStudents, " +
            "SUM(CASE WHEN sr.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedStudents, " +
            "AVG(sr.progress) as averageProgress " +
            "FROM StudyRecord sr WHERE sr.courseId = :courseId")
    Object[] getCourseStudyStatistics(@Param("courseId") String courseId);
}