package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.Course;
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
 * 课程数据仓储接口
 * 
 * @author 开发者
 * @version 1.0
 * @since 2025-07-17
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

    /**
     * 根据状态查找课程列表（分页）
     * 
     * @param status 课程状态
     * @param pageable 分页参数
     * @return 课程分页列表
     */
    Page<Course> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);

    /**
     * 根据分类查找已发布的课程（分页）
     * 
     * @param category 课程分类
     * @param pageable 分页参数
     * @return 课程分页列表
     */
    Page<Course> findByCategoryAndStatusOrderByCreateTimeDesc(String category, Integer status, Pageable pageable);

    /**
     * 根据讲师ID查找课程列表
     * 
     * @param instructorId 讲师ID
     * @param pageable 分页参数
     * @return 课程分页列表
     */
    Page<Course> findByInstructorIdOrderByCreateTimeDesc(String instructorId, Pageable pageable);

    /**
     * 搜索课程（标题和描述）
     * 
     * @param keyword 搜索关键词
     * @param status 课程状态
     * @param pageable 分页参数
     * @return 课程分页列表
     */
    @Query("SELECT c FROM Course c WHERE (c.title LIKE %:keyword% OR c.description LIKE %:keyword%) AND c.status = :status ORDER BY c.createTime DESC")
    Page<Course> searchByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);

    /**
     * 根据难度级别查找课程
     * 
     * @param difficultyLevel 难度级别
     * @param status 课程状态
     * @param pageable 分页参数
     * @return 课程分页列表
     */
    Page<Course> findByDifficultyLevelAndStatusOrderByCreateTimeDesc(Integer difficultyLevel, Integer status, Pageable pageable);

    /**
     * 查找必修课程
     * 
     * @param isRequired 是否必修
     * @param status 课程状态
     * @param pageable 分页参数
     * @return 课程分页列表
     */
    Page<Course> findByIsRequiredAndStatusOrderByCreateTimeDesc(Boolean isRequired, Integer status, Pageable pageable);

    /**
     * 查找热门课程（按学习人数排序）
     * 
     * @param status 课程状态
     * @param pageable 分页参数
     * @return 课程分页列表
     */
    Page<Course> findByStatusOrderByStudentCountDescCreateTimeDesc(Integer status, Pageable pageable);

    /**
     * 查找评分最高的课程
     * 
     * @param status 课程状态
     * @param pageable 分页参数
     * @return 课程分页列表
     */
    Page<Course> findByStatusOrderByRatingDescCreateTimeDesc(Integer status, Pageable pageable);

    /**
     * 查找最新发布的课程
     * 
     * @param status 课程状态
     * @param publishTime 发布时间
     * @param pageable 分页参数
     * @return 课程分页列表
     */
    Page<Course> findByStatusAndPublishTimeAfterOrderByPublishTimeDesc(Integer status, LocalDateTime publishTime, Pageable pageable);

    /**
     * 统计不同状态的课程数量
     * 
     * @param status 课程状态
     * @return 课程数量
     */
    Long countByStatus(Integer status);

    /**
     * 统计讲师的课程数量
     * 
     * @param instructorId 讲师ID
     * @param status 课程状态
     * @return 课程数量
     */
    Long countByInstructorIdAndStatus(String instructorId, Integer status);

    /**
     * 统计分类下的课程数量
     * 
     * @param category 课程分类
     * @param status 课程状态
     * @return 课程数量
     */
    Long countByCategoryAndStatus(String category, Integer status);

    /**
     * 查找所有课程分类
     * 
     * @param status 课程状态
     * @return 分类列表
     */
    @Query("SELECT DISTINCT c.category FROM Course c WHERE c.status = :status ORDER BY c.category")
    List<String> findDistinctCategoriesByStatus(@Param("status") Integer status);

    /**
     * 复合条件搜索课程
     * 
     * @param keyword 搜索关键词
     * @param category 课程分类
     * @param difficultyLevel 难度级别
     * @param status 课程状态
     * @param pageable 分页参数
     * @return 课程分页列表
     */
    @Query("SELECT c FROM Course c WHERE " +
           "(:keyword IS NULL OR c.title LIKE %:keyword% OR c.description LIKE %:keyword%) AND " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:difficultyLevel IS NULL OR c.difficultyLevel = :difficultyLevel) AND " +
           "c.status = :status " +
           "ORDER BY c.createTime DESC")
    Page<Course> searchCoursesByMultipleConditions(
        @Param("keyword") String keyword,
        @Param("category") String category,
        @Param("difficultyLevel") Integer difficultyLevel,
        @Param("status") Integer status,
        Pageable pageable
    );

    /**
     * 根据标签查找课程
     * 
     * @param tag 标签
     * @param status 课程状态
     * @param pageable 分页参数
     * @return 课程分页列表
     */
    @Query("SELECT c FROM Course c WHERE c.tags LIKE %:tag% AND c.status = :status ORDER BY c.createTime DESC")
    Page<Course> findByTagAndStatus(@Param("tag") String tag, @Param("status") Integer status, Pageable pageable);

    /**
     * 查找推荐课程（综合评分和学习人数）
     * 
     * @param status 课程状态
     * @param pageable 分页参数
     * @return 课程分页列表
     */
    @Query("SELECT c FROM Course c WHERE c.status = :status " +
           "ORDER BY (c.rating * 0.7 + c.studentCount * 0.3) DESC, c.createTime DESC")
    Page<Course> findRecommendedCourses(@Param("status") Integer status, Pageable pageable);
}