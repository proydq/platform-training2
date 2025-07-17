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
     * @return 考试结果
     */
    Optional<ExamResult> findByUserIdAndExamId(String userId, String examId);

    /**
     * 根据用户ID和考试ID查询所有考试结果
     *
     * @param userId 用户ID
     * @param examId 考试ID
     * @return 考试结果列表
     */
    List<ExamResult> findAllByUserIdAndExamId(String userId, String examId);

    /**
     * 根据通过状态查询考试结果
     *
     * @param passStatus 通过状态
     * @param pageable 分页参数
     * @return 考试结果分页数据
     */
    Page<ExamResult> findByPassStatus(ExamResult.PassStatus passStatus, Pageable pageable);

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
     * 查询超时记录
     *
     * @param pageable 分页参数
     * @return 超时记录分页数据
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
}