package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
public interface CourseRepository extends JpaRepository<Course, String>, JpaSpecificationExecutor<Course> {

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
     * 根据讲师ID和状态查找课程列表
     */
    Page<Course> findByInstructorIdAndStatusOrderByCreateTimeDesc(String instructorId, Integer status, Pageable pageable);

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
     * 🔧 查找推荐课程（根据评分和学习人数）
     */
    @Query("SELECT c FROM Course c WHERE c.status = :status ORDER BY c.rating DESC, c.studentCount DESC, c.createTime DESC")
    Page<Course> findRecommendedCourses(@Param("status") Integer status, Pageable pageable);

    /**
     * 统计不同状态的课程数量
     */
    Long countByStatus(Integer status);

    /**
     * 统计讲师的课程数量
     */
    Long countByInstructorId(String instructorId);

    /**
     * 统计讲师不同状态的课程数量
     */
    Long countByInstructorIdAndStatus(String instructorId, Integer status);

    /**
     * 🔧 查找不同分类的课程列表
     */
    @Query("SELECT DISTINCT c.category FROM Course c WHERE c.status = :status AND c.category IS NOT NULL")
    List<String> findDistinctCategoriesByStatus(@Param("status") Integer status);

    /**
     * 根据标签查找课程
     */
    @Query("SELECT c FROM Course c WHERE c.status = :status AND c.tags LIKE %:tag%")
    Page<Course> findByStatusAndTagsContaining(@Param("status") Integer status, @Param("tag") String tag, Pageable pageable);

    /**
     * 查找热门课程（按学习人数排序）
     */
    @Query("SELECT c FROM Course c WHERE c.status = :status AND c.studentCount > 0 ORDER BY c.studentCount DESC, c.rating DESC")
    Page<Course> findPopularCourses(@Param("status") Integer status, Pageable pageable);

    /**
     * 查找高评分课程
     */
    @Query("SELECT c FROM Course c WHERE c.status = :status AND c.rating >= :minRating ORDER BY c.rating DESC, c.ratingCount DESC")
    Page<Course> findHighRatedCourses(@Param("status") Integer status, @Param("minRating") Double minRating, Pageable pageable);

    /**
     * 根据价格范围查找课程
     */
    @Query("SELECT c FROM Course c WHERE c.status = :status AND c.price BETWEEN :minPrice AND :maxPrice ORDER BY c.createTime DESC")
    Page<Course> findByStatusAndPriceBetween(@Param("status") Integer status, @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, Pageable pageable);

    /**
     * 查找免费课程
     */
    @Query("SELECT c FROM Course c WHERE c.status = :status AND c.price = 0 ORDER BY c.createTime DESC")
    Page<Course> findFreeCourses(@Param("status") Integer status, Pageable pageable);

    /**
     * 查找必修课程
     */
    Page<Course> findByStatusAndIsRequiredOrderByCreateTimeDesc(Integer status, Boolean isRequired, Pageable pageable);

    /**
     * 统计课程总浏览次数
     */
    @Query("SELECT SUM(c.viewCount) FROM Course c WHERE c.status = :status")
    Long sumViewCountByStatus(@Param("status") Integer status);

    /**
     * 统计课程总学习人数
     */
    @Query("SELECT SUM(c.studentCount) FROM Course c WHERE c.status = :status")
    Long sumStudentCountByStatus(@Param("status") Integer status);

    /**
     * 查找指定讲师的最新课程
     */
    @Query("SELECT c FROM Course c WHERE c.instructorId = :instructorId AND c.status = :status ORDER BY c.createTime DESC")
    List<Course> findLatestCoursesByInstructor(@Param("instructorId") String instructorId, @Param("status") Integer status, Pageable pageable);
}