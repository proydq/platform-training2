package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.CourseChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 课程章节数据仓储接口
 * 
 * @author 开发者
 * @version 1.0
 * @since 2025-07-17
 */
@Repository
public interface CourseChapterRepository extends JpaRepository<CourseChapter, String> {

    /**
     * 根据课程ID查找所有章节（按排序顺序）
     * 
     * @param courseId 课程ID
     * @return 章节列表
     */
    List<CourseChapter> findByCourseIdOrderBySortOrderAsc(String courseId);

    /**
     * 根据课程ID和状态查找章节
     * 
     * @param courseId 课程ID
     * @param status 章节状态
     * @return 章节列表
     */
    List<CourseChapter> findByCourseIdAndStatusOrderBySortOrderAsc(String courseId, Integer status);

    /**
     * 根据课程ID和章节类型查找章节
     * 
     * @param courseId 课程ID
     * @param chapterType 章节类型
     * @return 章节列表
     */
    List<CourseChapter> findByCourseIdAndChapterTypeOrderBySortOrderAsc(String courseId, String chapterType);

    /**
     * 根据课程ID查找免费章节
     * 
     * @param courseId 课程ID
     * @param isFree 是否免费
     * @param status 章节状态
     * @return 章节列表
     */
    List<CourseChapter> findByCourseIdAndIsFreeAndStatusOrderBySortOrderAsc(String courseId, Boolean isFree, Integer status);

    /**
     * 统计课程的章节数量
     * 
     * @param courseId 课程ID
     * @return 章节数量
     */
    Long countByCourseId(String courseId);

    /**
     * 统计课程中指定状态的章节数量
     * 
     * @param courseId 课程ID
     * @param status 章节状态
     * @return 章节数量
     */
    Long countByCourseIdAndStatus(String courseId, Integer status);

    /**
     * 统计课程中指定类型的章节数量
     * 
     * @param courseId 课程ID
     * @param chapterType 章节类型
     * @return 章节数量
     */
    Long countByCourseIdAndChapterType(String courseId, String chapterType);

    /**
     * 获取课程中最大的排序序号
     * 
     * @param courseId 课程ID
     * @return 最大排序序号
     */
    @Query("SELECT MAX(c.sortOrder) FROM CourseChapter c WHERE c.courseId = :courseId")
    Optional<Integer> findMaxSortOrderByCourseId(@Param("courseId") String courseId);

    /**
     * 查找课程的第一个章节
     * 
     * @param courseId 课程ID
     * @param status 章节状态
     * @return 第一个章节
     */
    Optional<CourseChapter> findFirstByCourseIdAndStatusOrderBySortOrderAsc(String courseId, Integer status);

    /**
     * 查找课程的最后一个章节
     * 
     * @param courseId 课程ID
     * @param status 章节状态
     * @return 最后一个章节
     */
    Optional<CourseChapter> findFirstByCourseIdAndStatusOrderBySortOrderDesc(String courseId, Integer status);

    /**
     * 查找指定章节的下一个章节
     * 
     * @param courseId 课程ID
     * @param sortOrder 当前章节排序号
     * @param status 章节状态
     * @return 下一个章节
     */
    Optional<CourseChapter> findFirstByCourseIdAndSortOrderGreaterThanAndStatusOrderBySortOrderAsc(
        String courseId, Integer sortOrder, Integer status);

    /**
     * 查找指定章节的上一个章节
     * 
     * @param courseId 课程ID
     * @param sortOrder 当前章节排序号
     * @param status 章节状态
     * @return 上一个章节
     */
    Optional<CourseChapter> findFirstByCourseIdAndSortOrderLessThanAndStatusOrderBySortOrderDesc(
        String courseId, Integer sortOrder, Integer status);

    /**
     * 计算课程总时长
     * 
     * @param courseId 课程ID
     * @param status 章节状态
     * @return 总时长（分钟）
     */
    @Query("SELECT SUM(c.duration) FROM CourseChapter c WHERE c.courseId = :courseId AND c.status = :status")
    Optional<Long> calculateTotalDurationByCourseId(@Param("courseId") String courseId, @Param("status") Integer status);

    /**
     * 查找所有章节类型
     * 
     * @return 章节类型列表
     */
    @Query("SELECT DISTINCT c.chapterType FROM CourseChapter c ORDER BY c.chapterType")
    List<String> findDistinctChapterTypes();

    /**
     * 根据标题搜索章节
     * 
     * @param courseId 课程ID
     * @param title 章节标题关键词
     * @param status 章节状态
     * @return 章节列表
     */
    @Query("SELECT c FROM CourseChapter c WHERE c.courseId = :courseId AND c.title LIKE %:title% AND c.status = :status ORDER BY c.sortOrder ASC")
    List<CourseChapter> searchChaptersByTitle(@Param("courseId") String courseId, @Param("title") String title, @Param("status") Integer status);

    /**
     * 批量更新章节排序
     * 
     * @param courseId 课程ID
     * @param startOrder 起始排序号
     */
    @Query("UPDATE CourseChapter c SET c.sortOrder = c.sortOrder + 1 WHERE c.courseId = :courseId AND c.sortOrder >= :startOrder")
    void increaseSortOrderFrom(@Param("courseId") String courseId, @Param("startOrder") Integer startOrder);

    /**
     * 删除课程的所有章节
     * 
     * @param courseId 课程ID
     */
    void deleteByCourseId(String courseId);
}