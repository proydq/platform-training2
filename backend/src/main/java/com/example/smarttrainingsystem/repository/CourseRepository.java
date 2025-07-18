// 文件路径: backend/src/main/java/com/example/smarttrainingsystem/repository/CourseRepository.java
package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 课程数据仓储接口
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-07-18
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

    /**
     * 根据状态查找课程列表（分页）
     */
    Page<Course> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);

    /**
     * 根据分类和状态查找课程（分页）
     */
    Page<Course> findByCategoryAndStatusOrderByCreateTimeDesc(String category, Integer status, Pageable pageable);

    /**
     * 根据讲师ID查找课程列表
     */
    Page<Course> findByInstructorIdOrderByCreateTimeDesc(String instructorId, Pageable pageable);

    /**
     * 复合条件搜索课程
     */
    @Query("SELECT c FROM Course c WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR c.title LIKE %:keyword% OR c.description LIKE %:keyword%) AND " +
            "(:category IS NULL OR :category = '' OR c.category = :category) AND " +
            "(:difficultyLevel IS NULL OR c.difficultyLevel = :difficultyLevel) AND " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:isRequired IS NULL OR c.isRequired = :isRequired) AND " +
            "(:instructorId IS NULL OR :instructorId = '' OR c.instructorId = :instructorId)")
    Page<Course> searchCoursesByMultipleConditions(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("difficultyLevel") Integer difficultyLevel,
            @Param("status") Integer status,
            @Param("isRequired") Boolean isRequired,
            @Param("instructorId") String instructorId,
            Pageable pageable);

    /**
     * 根据评分排序查找课程
     */
    Page<Course> findByStatusOrderByRatingDescCreateTimeDesc(Integer status, Pageable pageable);

    /**
     * 根据学习人数排序查找课程
     */
    Page<Course> findByStatusOrderByStudentCountDescCreateTimeDesc(Integer status, Pageable pageable);

    /**
     * 查找最新发布的课程
     */
    Page<Course> findByStatusAndPublishTimeAfterOrderByPublishTimeDesc(Integer status, Long publishTime, Pageable pageable);

    /**
     * 统计不同状态的课程数量
     */
    Long countByStatus(Integer status);

    /**
     * 统计讲师的课程数量
     */
    Long countByInstructorIdAndStatus(String instructorId, Integer status);

    /**
     * 获取所有课程分类
     */
    @Query("SELECT DISTINCT c.category FROM Course c WHERE c.status = :status AND c.category IS NOT NULL")
    List<String> findDistinctCategoriesByStatus(@Param("status") Integer status);

    /**
     * 获取推荐课程
     */
    @Query("SELECT c FROM Course c WHERE c.status = :status ORDER BY (c.rating * 0.4 + c.studentCount * 0.3 + c.viewCount * 0.3) DESC")
    Page<Course> findRecommendedCourses(@Param("status") Integer status, Pageable pageable);

    /**
     * 根据难度级别和状态查找课程
     */
    Page<Course> findByDifficultyLevelAndStatusOrderByCreateTimeDesc(Integer difficultyLevel, Integer status, Pageable pageable);

    /**
     * 查找必修课程
     */
    Page<Course> findByIsRequiredAndStatusOrderByCreateTimeDesc(Boolean isRequired, Integer status, Pageable pageable);

    /**
     * 搜索课程（仅按关键词）
     */
    @Query("SELECT c FROM Course c WHERE (c.title LIKE %:keyword% OR c.description LIKE %:keyword%) AND c.status = :status ORDER BY c.createTime DESC")
    Page<Course> searchByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);

    /**
     * 统计分类下的课程数量
     */
    Long countByCategoryAndStatus(String category, Integer status);
}