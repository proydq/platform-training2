package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 考试题目关联数据仓储接口
 * 提供考试题目关联的数据访问操作
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, String> {

    /**
     * 根据考试ID查询题目
     *
     * @param examId 考试ID
     * @return 题目列表
     */
    List<ExamQuestion> findByExamIdOrderByQuestionOrder(String examId);

    /**
     * 根据考试ID和题目ID查询关联
     *
     * @param examId 考试ID
     * @param questionId 题目ID
     * @return 考试题目关联
     */
    ExamQuestion findByExamIdAndQuestionId(String examId, String questionId);

    /**
     * 根据考试ID删除所有题目
     *
     * @param examId 考试ID
     */
    void deleteByExamId(String examId);

    /**
     * 根据题目ID查询相关考试
     *
     * @param questionId 题目ID
     * @return 考试题目关联列表
     */
    List<ExamQuestion> findByQuestionId(String questionId);

    /**
     * 统计考试题目数量
     *
     * @param examId 考试ID
     * @return 题目数量
     */
    long countByExamId(String examId);

    /**
     * 查询考试的总分
     *
     * @param examId 考试ID
     * @return 总分
     */
    @Query("SELECT SUM(eq.score) FROM ExamQuestion eq WHERE eq.examId = :examId")
    Integer sumScoreByExamId(@Param("examId") String examId);

    /**
     * 查询考试的题目ID列表
     *
     * @param examId 考试ID
     * @return 题目ID列表
     */
    @Query("SELECT eq.questionId FROM ExamQuestion eq WHERE eq.examId = :examId ORDER BY eq.questionOrder")
    List<String> findQuestionIdsByExamId(@Param("examId") String examId);
}