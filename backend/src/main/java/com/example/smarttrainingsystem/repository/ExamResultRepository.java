package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.ExamResult;
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
 * 考试结果数据仓储接口
 * 提供考试结果相关的数据访问操作
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, String> {

    /**
     * 根据用户ID查询考试结果
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 考试结果分页数据
     */
    Page<ExamResult> findByUserId(String userId, Pageable pageable);

    /**
     * 根据考试ID查询考试结果
     *
     * @param examId 考试ID
     * @param pageable 分页参数
     * @return 考试结果分页数据
     */
    Page<ExamResult> findByExamId(String examId, Pageable pageable);

    /**
     * 根据用户ID和考试ID查询考试结果
     *
     * @param userId 用户ID
     * @param examId 考试ID
     * @return 考试结果列表
     */
    List<ExamResult> findByUserIdAndExamId(String userId, String examId);

    /**
     * 根据用户ID和考试ID查询最新考试结果
     *
     * @param userId 用户ID
     * @param examId 考试ID
     * @return 最新考试结果
     */
    @Query("SELECT er FROM ExamResult er WHERE er.userId = :userId AND er.examId = :examId " +
            "ORDER BY er.createdAt DESC")
    Optional<ExamResult> findLatestByUserIdAndExamId(@Param("userId") String userId,
                                                     @Param("examId") String examId);

    /**
     * 查询用户的最佳成绩
     *
     * @param userId 用户ID
     * @param examId 考试ID
     * @return 最佳成绩
     */
    @Query("SELECT er FROM ExamResult er WHERE er.userId = :userId AND er.examId = :examId " +
            "ORDER BY er.score DESC")
    List<ExamResult> findBestResultByUserIdAndExamId(@Param("userId") String userId,
                                                     @Param("examId") String examId);

    /**
     * 查询用户的最新成绩
     *
     * @param userId 用户ID
     * @param examId 考试ID
     * @return 最新成绩
     */
    @Query("SELECT er FROM ExamResult er WHERE er.userId = :userId AND er.examId = :examId " +
            "ORDER BY er.createdAt DESC")
    List<ExamResult> findLatestResultByUserIdAndExamId(@Param("userId") String userId,
                                                       @Param("examId") String examId);

    /**
     * 统计用户参加考试次数
     *
     * @param userId 用户ID
     * @param examId 考试ID
     * @return 参加次数
     */
    long countByUserIdAndExamId(String userId, String examId);

    /**
     * 统计考试的参加人数
     *
     * @param examId 考试ID
     * @return 参加人数
     */
    @Query("SELECT COUNT(DISTINCT er.userId) FROM ExamResult er WHERE er.examId = :examId")
    long countDistinctUsersByExamId(@Param("examId") String examId);

    /**
     * 统计考试的通过人数
     *
     * @param examId 考试ID
     * @return 通过人数
     */
    @Query("SELECT COUNT(DISTINCT er.userId) FROM ExamResult er WHERE er.examId = :examId " +
            "AND er.passStatus = 'PASS'")
    long countPassedUsersByExamId(@Param("examId") String examId);

    /**
     * 计算考试的平均分
     *
     * @param examId 考试ID
     * @return 平均分
     */
    @Query("SELECT AVG(er.score) FROM ExamResult er WHERE er.examId = :examId " +
            "AND er.passStatus IN ('PASS', 'FAIL')")
    Double calculateAverageScoreByExamId(@Param("examId") String examId);

    /**
     * 查询考试成绩排行榜
     *
     * @param examId 考试ID
     * @param pageable 分页参数
     * @return 成绩排行榜
     */
    @Query("SELECT er FROM ExamResult er WHERE er.examId = :examId " +
            "AND er.passStatus IN ('PASS', 'FAIL') ORDER BY er.score DESC, er.duration ASC")
    Page<ExamResult> findRankingByExamId(@Param("examId") String examId, Pageable pageable);

    /**
     * 查询时间段内的考试结果
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 考试结果分页数据
     */
    @Query("SELECT er FROM ExamResult er WHERE er.startTime BETWEEN :startTime AND :endTime")
    Page<ExamResult> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime,
                                     Pageable pageable);

    /**
     * 查询作弊记录
     *
     * @param pageable 分页参数
     * @return 作弊记录分页数据
     */
    Page<ExamResult> findByIsCheatingTrue(Pageable pageable);

    /**
     * 根据通过状态查询考试结果
     *
     * @param status 通过状态
     * @param pageable 分页参数
     * @return 考试结果分页数据
     */
    Page<ExamResult> findByPassStatus(ExamResult.PassStatus status, Pageable pageable);

    /**
     * 复合搜索考试结果
     *
     * @param userId 用户ID
     * @param examId 考试ID
     * @param passStatus 通过状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 考试结果分页数据
     */
    @Query("SELECT er FROM ExamResult er WHERE " +
            "(:userId IS NULL OR er.userId = :userId) AND " +
            "(:examId IS NULL OR er.examId = :examId) AND " +
            "(:passStatus IS NULL OR er.passStatus = :passStatus) AND " +
            "(:startTime IS NULL OR er.startTime >= :startTime) AND " +
            "(:endTime IS NULL OR er.endTime <= :endTime)")
    Page<ExamResult> searchExamResults(@Param("userId") String userId,
                                       @Param("examId") String examId,
                                       @Param("passStatus") ExamResult.PassStatus passStatus,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime,
                                       Pageable pageable);

    /**
     * 查询用户的错题记录
     *
     * @param userId 用户ID
     * @return 错题记录列表
     */
    @Query("SELECT er FROM ExamResult er WHERE er.userId = :userId AND er.wrongCount > 0")
    List<ExamResult> findWrongQuestionsByUserId(@Param("userId") String userId);

    /**
     * 查询考试的最高分
     *
     * @param examId 考试ID
     * @return 最高分
     */
    @Query("SELECT MAX(er.score) FROM ExamResult er WHERE er.examId = :examId")
    Integer findMaxScoreByExamId(@Param("examId") String examId);

    /**
     * 查询考试的最低分
     *
     * @param examId 考试ID
     * @return 最低分
     */
    @Query("SELECT MIN(er.score) FROM ExamResult er WHERE er.examId = :examId")
    Integer findMinScoreByExamId(@Param("examId") String examId);

    /**
     * 查询用户最近的考试记录
     *
     * @param userId 用户ID
     * @param limit 限制条数
     * @return 最近的考试记录
     */
    @Query(value = "SELECT * FROM t_exam_result WHERE user_id = :userId " +
            "ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<ExamResult> findRecentByUserId(@Param("userId") String userId, @Param("limit") int limit);

    /**
     * 统计用户总考试次数
     *
     * @param userId 用户ID
     * @return 总考试次数
     */
    long countByUserId(String userId);

    /**
     * 统计用户通过的考试次数
     *
     * @param userId 用户ID
     * @return 通过的考试次数
     */
    long countByUserIdAndPassStatus(String userId, ExamResult.PassStatus passStatus);

    /**
     * 查询正在进行的考试
     *
     * @param userId 用户ID
     * @return 正在进行的考试列表
     */
    List<ExamResult> findByUserIdAndPassStatus(String userId, ExamResult.PassStatus passStatus);
}