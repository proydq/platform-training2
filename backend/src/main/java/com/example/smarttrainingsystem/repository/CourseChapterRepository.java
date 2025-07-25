// 文件路径: backend/src/main/java/com/example/smarttrainingsystem/repository/CourseChapterRepository.java
package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.CourseChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 课程章节数据仓储接口
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-07-18
 */
@Repository
public interface CourseChapterRepository extends JpaRepository<CourseChapter, String> {

    /**
     * 根据课程ID查找章节列表（按排序顺序）
     */
    List<CourseChapter> findByCourseIdOrderBySortOrderAsc(String courseId);

    /**
     * 根据课程ID查找所有章节
     */
    List<CourseChapter> findByCourseId(String courseId);

    /**
     * 根据课程ID和状态查找章节列表
     */
    List<CourseChapter> findByCourseIdAndStatusOrderBySortOrderAsc(String courseId, Integer status);

    /**
     * 统计课程章节数量
     */
    Long countByCourseId(String courseId);

    /**
     * 统计课程已发布章节数量
     */
    Long countByCourseIdAndStatus(String courseId, Integer status);

    /**
     * 查找课程中最大的排序序号
     */
    @Query("SELECT MAX(c.sortOrder) FROM CourseChapter c WHERE c.courseId = :courseId")
    Integer findMaxSortOrderByCourseId(@Param("courseId") String courseId);

    /**
     * 查找指定排序序号及之后的章节
     */
    List<CourseChapter> findByCourseIdAndSortOrderGreaterThanEqualOrderBySortOrderAsc(String courseId, Integer sortOrder);

    /**
     * 查找指定排序序号之前的章节
     */
    List<CourseChapter> findByCourseIdAndSortOrderLessThanOrderBySortOrderAsc(String courseId, Integer sortOrder);

    /**
     * 删除课程的所有章节
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM CourseChapter c WHERE c.courseId = :courseId")
    void deleteByCourseId(@Param("courseId") String courseId);

    /**
     * 根据课程ID和排序范围查找章节
     */
    @Query("SELECT c FROM CourseChapter c WHERE c.courseId = :courseId AND c.sortOrder BETWEEN :startOrder AND :endOrder ORDER BY c.sortOrder ASC")
    List<CourseChapter> findByCourseIdAndSortOrderBetween(
            @Param("courseId") String courseId,
            @Param("startOrder") Integer startOrder,
            @Param("endOrder") Integer endOrder);

    /**
     * 批量更新章节排序
     */
    @Modifying
    @Transactional
    @Query("UPDATE CourseChapter c SET c.sortOrder = :newOrder WHERE c.id = :chapterId")
    void updateSortOrder(@Param("chapterId") String chapterId, @Param("newOrder") Integer newOrder);

    /**
     * 查找指定章节在课程中的位置
     */
    @Query("SELECT COUNT(c) FROM CourseChapter c WHERE c.courseId = :courseId AND c.sortOrder < :sortOrder")
    Long findPositionInCourse(@Param("courseId") String courseId, @Param("sortOrder") Integer sortOrder);
}