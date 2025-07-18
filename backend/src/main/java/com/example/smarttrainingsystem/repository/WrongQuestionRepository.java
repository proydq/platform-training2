package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.WrongQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 错题记录数据仓储接口
 * 提供错题记录相关的数据访问操作
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Repository
public interface WrongQuestionRepository extends JpaRepository<WrongQuestion, String> {

    /**
     * 根据用户ID和题目ID查询错题记录
     *
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 错题记录
     */
    Optional<WrongQuestion> findByUserIdAndQuestionId(String userId, String questionId);

    /**
     * 根据用户ID查询错题记录
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 错题记录分页数据
     */
    Page<WrongQuestion> findByUserId(String userId, Pageable pageable);

    /**
     * 根据用户ID和掌握状态查询错题记录
     *
     * @param userId 用户ID
     * @param mastered 是否已掌握
     * @return 错题记录列表
     */
    List<WrongQuestion> findByUserIdAndMastered(String userId, Boolean mastered);

    /**
     * 统计用户错题总数
     *
     * @param userId 用户ID
     * @return 错题总数
     */
    long countByUserId(String userId);

    /**
     * 统计用户已掌握题目数
     *
     * @param userId 用户ID
     * @param mastered 是否已掌握
     * @return 已掌握题目数
     */
    long countByUserIdAndMastered(String userId, Boolean mastered);

    /**
     * 统计用户指定难度错题数
     *
     * @param userId 用户ID
     * @param difficulty 难度等级
     * @return 错题数
     */
    @Query("SELECT COUNT(wq) FROM WrongQuestion wq " +
           "JOIN Question q ON wq.questionId = q.id " +
           "WHERE wq.userId = :userId AND q.difficulty = :difficulty")
    long countByUserIdAndDifficulty(@Param("userId") String userId, 
                                    @Param("difficulty") Integer difficulty);

    /**
     * 复合条件查询用户错题
     *
     * @param userId 用户ID
     * @param difficulty 难度等级
     * @param category 分类
     * @param status 掌握状态
     * @param pageable 分页参数
     * @return 错题记录分页数据
     */
    @Query("SELECT wq FROM WrongQuestion wq " +
           "JOIN Question q ON wq.questionId = q.id " +
           "WHERE wq.userId = :userId " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "AND (:category IS NULL OR q.category = :category) " +
           "AND (:status IS NULL OR " +
           "     (:status = 'mastered' AND wq.mastered = true) OR " +
           "     (:status = 'unmastered' AND wq.mastered = false) OR " +
           "     (:status = 'intensive' AND wq.wrongCount > 2 AND " +
           "      (wq.practiceCount = 0 OR (wq.correctCount * 100.0 / wq.practiceCount) < 60)))")
    Page<WrongQuestion> findUserWrongQuestions(@Param("userId") String userId,
                                               @Param("difficulty") Integer difficulty,
                                               @Param("category") String category,
                                               @Param("status") String status,
                                               Pageable pageable);

    /**
     * 查询练习题目
     *
     * @param userId 用户ID
     * @param difficulty 难度等级
     * @param category 分类
     * @param mastered 是否已掌握
     * @return 错题记录列表
     */
    @Query("SELECT wq FROM WrongQuestion wq " +
           "JOIN Question q ON wq.questionId = q.id " +
           "WHERE wq.userId = :userId " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "AND (:category IS NULL OR q.category = :category) " +
           "AND wq.mastered = :mastered " +
           "ORDER BY wq.wrongCount DESC, wq.lastWrongAt DESC")
    List<WrongQuestion> findPracticeQuestions(@Param("userId") String userId,
                                              @Param("difficulty") Integer difficulty,
                                              @Param("category") String category,
                                              @Param("mastered") Boolean mastered);

    /**
     * 获取用户错题分类统计
     *
     * @param userId 用户ID
     * @return 分类统计Map
     */
    @Query("SELECT q.category as category, COUNT(wq) as count " +
           "FROM WrongQuestion wq " +
           "JOIN Question q ON wq.questionId = q.id " +
           "WHERE wq.userId = :userId AND q.category IS NOT NULL " +
           "GROUP BY q.category")
    List<Object[]> findWrongQuestionCategoryStats(@Param("userId") String userId);

    /**
     * 获取用户错题分类统计（转换为Map）
     *
     * @param userId 用户ID
     * @return 分类统计Map
     */
    default Map<String, Long> getWrongQuestionCategoryStats(String userId) {
        List<Object[]> results = findWrongQuestionCategoryStats(userId);
        return results.stream()
            .collect(java.util.stream.Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1]
            ));
    }

    /**
     * 查询用户最近错误的题目
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 最近错题列表
     */
    @Query("SELECT wq FROM WrongQuestion wq " +
           "WHERE wq.userId = :userId " +
           "ORDER BY wq.lastWrongAt DESC")
    List<WrongQuestion> findRecentWrongQuestions(@Param("userId") String userId, 
                                                 @Param("limit") int limit);

    /**
     * 查询需要重点练习的题目
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 需要重点练习的题目
     */
    @Query("SELECT wq FROM WrongQuestion wq " +
           "WHERE wq.userId = :userId " +
           "AND wq.mastered = false " +
           "AND wq.wrongCount > 2 " +
           "AND (wq.practiceCount = 0 OR (wq.correctCount * 100.0 / wq.practiceCount) < 60) " +
           "ORDER BY wq.wrongCount DESC, wq.lastWrongAt DESC")
    Page<WrongQuestion> findIntensivePracticeQuestions(@Param("userId") String userId, 
                                                       Pageable pageable);

    /**
     * 查询用户在指定考试中的错题
     *
     * @param userId 用户ID
     * @param examId 考试ID
     * @return 错题列表
     */
    List<WrongQuestion> findByUserIdAndExamId(String userId, String examId);

    /**
     * 删除用户的所有错题记录
     *
     * @param userId 用户ID
     */
    void deleteByUserId(String userId);

    /**
     * 删除指定题目的所有错题记录
     *
     * @param questionId 题目ID
     */
    void deleteByQuestionId(String questionId);

    /**
     * 查询用户错题趋势（按月统计）
     *
     * @param userId 用户ID
     * @return 错题趋势数据
     */
    @Query("SELECT DATE_FORMAT(wq.createdAt, '%Y-%m') as month, COUNT(wq) as count " +
           "FROM WrongQuestion wq " +
           "WHERE wq.userId = :userId " +
           "GROUP BY DATE_FORMAT(wq.createdAt, '%Y-%m') " +
           "ORDER BY month DESC")
    List<Object[]> findWrongQuestionTrend(@Param("userId") String userId);

    /**
     * 查询错题掌握率统计
     *
     * @param userId 用户ID
     * @return 掌握率统计
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN wq.mastered = true THEN 1 END) as masteredCount, " +
           "COUNT(CASE WHEN wq.mastered = false THEN 1 END) as unmasteredCount, " +
           "COUNT(*) as totalCount " +
           "FROM WrongQuestion wq " +
           "WHERE wq.userId = :userId")
    Object[] findMasteryStatistics(@Param("userId") String userId);
}