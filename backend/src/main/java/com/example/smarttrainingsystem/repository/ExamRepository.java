package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.Exam;
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
 * 考试数据仓储接口
 * 提供考试相关的数据访问操作
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Repository
public interface ExamRepository extends JpaRepository<Exam, String> {

    /**
     * 根据课程ID查询考试
     *
     * @param courseId 课程ID
     * @param pageable 分页参数
     * @return 考试分页数据
     */
    Page<Exam> findByCourseId(String courseId, Pageable pageable);

    /**
     * 根据创建者ID查询考试
     *
     * @param createdBy 创建者ID
     * @param pageable 分页参数
     * @return 考试分页数据
     */
    Page<Exam> findByCreatedBy(String createdBy, Pageable pageable);

    /**
     * 根据考试状态查询考试
     *
     * @param status 考试状态
     * @param pageable 分页参数
     * @return 考试分页数据
     */
    Page<Exam> findByStatus(Exam.ExamStatus status, Pageable pageable);

    /**
     * 根据考试类型查询考试
     *
     * @param examType 考试类型
     * @param pageable 分页参数
     * @return 考试分页数据
     */
    Page<Exam> findByExamType(Exam.ExamType examType, Pageable pageable);

    /**
     * 搜索考试（按标题）
     *
     * @param title 标题关键词
     * @param pageable 分页参数
     * @return 考试分页数据
     */
    Page<Exam> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * 查询正在进行的考试
     *
     * @param now 当前时间
     * @return 正在进行的考试列表
     */
    @Query("SELECT e FROM Exam e WHERE e.status = 'PUBLISHED' AND " +
           "(e.startTime IS NULL OR e.startTime <= :now) AND " +
           "(e.endTime IS NULL OR e.endTime >= :now)")
    List<Exam> findActiveExams(@Param("now") LocalDateTime now);

    /**
     * 查询即将开始的考试
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 即将开始的考试列表
     */
    @Query("SELECT e FROM Exam e WHERE e.status = 'PUBLISHED' AND " +
           "e.startTime BETWEEN :startTime AND :endTime")
    List<Exam> findUpcomingExams(@Param("startTime") LocalDateTime startTime, 
                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 查询已过期的考试
     *
     * @param now 当前时间
     * @return 已过期的考试列表
     */
    @Query("SELECT e FROM Exam e WHERE e.status = 'PUBLISHED' AND " +
           "e.endTime IS NOT NULL AND e.endTime < :now")
    List<Exam> findExpiredExams(@Param("now") LocalDateTime now);

    /**
     * 统计考试数量
     *
     * @param status 考试状态
     * @return 考试数量
     */
    long countByStatus(Exam.ExamStatus status);

    /**
     * 统计课程的考试数量
     *
     * @param courseId 课程ID
     * @return 考试数量
     */
    long countByCourseId(String courseId);

    /**
     * 查询用户可参加的考试
     *
     * @param now 当前时间
     * @param pageable 分页参数
     * @return 可参加的考试分页数据
     */
    @Query("SELECT e FROM Exam e WHERE e.status = 'PUBLISHED' AND " +
           "(e.startTime IS NULL OR e.startTime <= :now) AND " +
           "(e.endTime IS NULL OR e.endTime >= :now)")
    Page<Exam> findAvailableExams(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * 复合搜索考试
     *
     * @param title 标题关键词
     * @param examType 考试类型
     * @param status 考试状态
     * @param courseId 课程ID
     * @param pageable 分页参数
     * @return 考试分页数据
     */
    @Query("SELECT e FROM Exam e WHERE " +
           "(:title IS NULL OR e.title LIKE %:title%) AND " +
           "(:examType IS NULL OR e.examType = :examType) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:courseId IS NULL OR e.courseId = :courseId)")
    Page<Exam> searchExams(@Param("title") String title,
                           @Param("examType") Exam.ExamType examType,
                           @Param("status") Exam.ExamStatus status,
                           @Param("courseId") String courseId,
                           Pageable pageable);
}