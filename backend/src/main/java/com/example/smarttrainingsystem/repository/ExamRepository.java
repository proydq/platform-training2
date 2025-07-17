package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 考试数据仓储接口 - 最简版本
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
     */
    Page<Exam> findByCourseId(String courseId, Pageable pageable);
    List<Exam> findByCourseId(String courseId);

    /**
     * 根据创建者ID查询考试
     */
    Page<Exam> findByCreatedBy(String createdBy, Pageable pageable);
    List<Exam> findByCreatedBy(String createdBy);

    /**
     * 根据考试状态查询考试
     */
    Page<Exam> findByStatus(Exam.ExamStatus status, Pageable pageable);
    List<Exam> findByStatus(Exam.ExamStatus status);

    /**
     * 根据考试类型查询考试
     */
    Page<Exam> findByExamType(Exam.ExamType examType, Pageable pageable);
    List<Exam> findByExamType(Exam.ExamType examType);

    /**
     * 搜索考试（按标题）
     */
    Page<Exam> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    List<Exam> findByTitleContainingIgnoreCase(String title);

    /**
     * 统计方法
     */
    long countByStatus(Exam.ExamStatus status);
    long countByExamType(Exam.ExamType examType);
    long countByCreatedBy(String createdBy);
}