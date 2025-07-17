package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 题目数据仓储接口
 * 提供题目相关的数据访问操作
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {

    /**
     * 根据题目类型查询题目
     *
     * @param questionType 题目类型
     * @param pageable 分页参数
     * @return 题目分页数据
     */
    Page<Question> findByQuestionType(Question.QuestionType questionType, Pageable pageable);

    /**
     * 根据题目状态查询题目
     *
     * @param status 题目状态
     * @param pageable 分页参数
     * @return 题目分页数据
     */
    Page<Question> findByStatus(Question.QuestionStatus status, Pageable pageable);

    /**
     * 根据难度等级查询题目
     *
     * @param difficulty 难度等级
     * @param pageable 分页参数
     * @return 题目分页数据
     */
    Page<Question> findByDifficulty(Integer difficulty, Pageable pageable);

    /**
     * 根据分类查询题目
     *
     * @param category 分类
     * @param pageable 分页参数
     * @return 题目分页数据
     */
    Page<Question> findByCategory(String category, Pageable pageable);

    /**
     * 根据创建者查询题目
     *
     * @param createdBy 创建者ID
     * @param pageable 分页参数
     * @return 题目分页数据
     */
    Page<Question> findByCreatedBy(String createdBy, Pageable pageable);

    /**
     * 搜索题目（按内容）
     *
     * @param content 内容关键词
     * @param pageable 分页参数
     * @return 题目分页数据
     */
    Page<Question> findByContentContainingIgnoreCase(String content, Pageable pageable);

    /**
     * 根据标签搜索题目
     *
     * @param tag 标签
     * @param pageable 分页参数
     * @return 题目分页数据
     */
    Page<Question> findByTagsContainingIgnoreCase(String tag, Pageable pageable);

    /**
     * 查询启用的题目
     *
     * @param pageable 分页参数
     * @return 启用的题目分页数据
     */
    Page<Question> findByStatusAndQuestionType(Question.QuestionStatus status, 
                                               Question.QuestionType questionType, 
                                               Pageable pageable);

    /**
     * 随机查询题目
     *
     * @param questionType 题目类型
     * @param difficulty 难度等级
     * @param limit 数量限制
     * @return 随机题目列表
     */
    @Query(value = "SELECT * FROM t_question WHERE question_type = :questionType AND " +
                   "difficulty = :difficulty AND status = 'ACTIVE' ORDER BY RAND() LIMIT :limit",
           nativeQuery = true)
    List<Question> findRandomQuestions(@Param("questionType") String questionType,
                                       @Param("difficulty") Integer difficulty,
                                       @Param("limit") Integer limit);

    /**
     * 按分类和难度随机查询题目
     *
     * @param category 分类
     * @param difficulty 难度等级
     * @param limit 数量限制
     * @return 随机题目列表
     */
    @Query(value = "SELECT * FROM t_question WHERE category = :category AND " +
                   "difficulty = :difficulty AND status = 'ACTIVE' ORDER BY RAND() LIMIT :limit",
           nativeQuery = true)
    List<Question> findRandomQuestionsByCategory(@Param("category") String category,
                                                 @Param("difficulty") Integer difficulty,
                                                 @Param("limit") Integer limit);

    /**
     * 统计题目数量
     *
     * @param questionType 题目类型
     * @param status 题目状态
     * @return 题目数量
     */
    long countByQuestionTypeAndStatus(Question.QuestionType questionType, 
                                      Question.QuestionStatus status);

    /**
     * 统计分类题目数量
     *
     * @param category 分类
     * @return 题目数量
     */
    long countByCategory(String category);

    /**
     * 查询所有分类
     *
     * @return 分类列表
     */
    @Query("SELECT DISTINCT q.category FROM Question q WHERE q.category IS NOT NULL AND q.status = 'ACTIVE'")
    List<String> findAllCategories();

    /**
     * 查询所有标签
     *
     * @return 标签列表
     */
    @Query("SELECT DISTINCT q.tags FROM Question q WHERE q.tags IS NOT NULL AND q.status = 'ACTIVE'")
    List<String> findAllTags();

    /**
     * 复合搜索题目
     *
     * @param content 内容关键词
     * @param questionType 题目类型
     * @param difficulty 难度等级
     * @param category 分类
     * @param status 题目状态
     * @param pageable 分页参数
     * @return 题目分页数据
     */
    @Query("SELECT q FROM Question q WHERE " +
           "(:content IS NULL OR q.content LIKE %:content%) AND " +
           "(:questionType IS NULL OR q.questionType = :questionType) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty) AND " +
           "(:category IS NULL OR q.category = :category) AND " +
           "(:status IS NULL OR q.status = :status)")
    Page<Question> searchQuestions(@Param("content") String content,
                                   @Param("questionType") Question.QuestionType questionType,
                                   @Param("difficulty") Integer difficulty,
                                   @Param("category") String category,
                                   @Param("status") Question.QuestionStatus status,
                                   Pageable pageable);
}