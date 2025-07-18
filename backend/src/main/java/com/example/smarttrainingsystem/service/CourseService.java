// 文件路径: backend/src/main/java/com/example/smarttrainingsystem/service/CourseService.java
package com.example.smarttrainingsystem.service;

import com.example.smarttrainingsystem.dto.CourseDTO;
import com.example.smarttrainingsystem.dto.CourseChapterDTO;
import com.example.smarttrainingsystem.entity.Course;
import com.example.smarttrainingsystem.entity.CourseChapter;
import com.example.smarttrainingsystem.exception.BusinessException;
import com.example.smarttrainingsystem.repository.CourseRepository;
import com.example.smarttrainingsystem.repository.CourseChapterRepository;
import com.example.smarttrainingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程业务服务类
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-07-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseChapterRepository courseChapterRepository;
    private final UserRepository userRepository;

    /**
     * 创建课程
     */
    @Transactional
    public CourseDTO.Response createCourse(CourseDTO.CreateRequest request) {
        log.info("创建课程: {}", request.getTitle());

        // 验证讲师是否存在
        if (!userRepository.existsById(request.getInstructorId())) {
            throw new BusinessException(2001, "讲师不存在");
        }

        // 创建课程实体
        Course course = new Course();
        BeanUtils.copyProperties(request, course);
        course.setStatus(0); // 默认草稿状态

        // 保存课程
        course = courseRepository.save(course);

        // 创建章节（如果有的话）
        if (request.getChapters() != null && !request.getChapters().isEmpty()) {
            createChaptersForCourse(course.getId(), request.getChapters());
        }

        log.info("课程创建成功: courseId={}", course.getId());
        return getCourseDetail(course.getId());
    }

    /**
     * 更新课程
     */
    @Transactional
    public CourseDTO.Response updateCourse(String courseId, CourseDTO.UpdateRequest request) {
        log.info("更新课程: courseId={}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(2002, "课程不存在"));

        // 更新课程信息（只更新非空字段）
        if (StringUtils.hasText(request.getTitle())) {
            course.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getCategory())) {
            course.setCategory(request.getCategory());
        }
        if (StringUtils.hasText(request.getInstructorName())) {
            course.setInstructorName(request.getInstructorName());
        }
        if (request.getPrice() != null) {
            course.setPrice(request.getPrice());
        }
        if (request.getDifficultyLevel() != null) {
            course.setDifficultyLevel(request.getDifficultyLevel());
        }
        if (request.getEstimatedDuration() != null) {
            course.setEstimatedDuration(request.getEstimatedDuration());
        }
        if (request.getIsRequired() != null) {
            course.setIsRequired(request.getIsRequired());
        }
        if (request.getLearningObjectives() != null) {
            course.setLearningObjectives(request.getLearningObjectives());
        }
        if (request.getPrerequisites() != null) {
            course.setPrerequisites(request.getPrerequisites());
        }
        if (request.getTags() != null) {
            course.setTags(request.getTags());
        }
        if (request.getCoverImageUrl() != null) {
            course.setCoverImageUrl(request.getCoverImageUrl());
        }
        if (request.getMaterialUrls() != null) {
            course.setMaterialUrls(request.getMaterialUrls());
        }
        if (request.getVideoUrls() != null) {
            course.setVideoUrls(request.getVideoUrls());
        }

        courseRepository.save(course);

        log.info("课程更新成功: courseId={}", courseId);
        return getCourseDetail(courseId);
    }

    /**
     * 发布课程
     */
    @Transactional
    public CourseDTO.Response publishCourse(String courseId) {
        log.info("发布课程: courseId={}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(2002, "课程不存在"));

        // 验证课程是否可以发布
        validateCourseForPublish(course);

        // 发布课程
        course.publish();
        courseRepository.save(course);

        log.info("课程发布成功: courseId={}", courseId);
        return getCourseDetail(courseId);
    }

    /**
     * 下架课程
     */
    @Transactional
    public CourseDTO.Response unpublishCourse(String courseId) {
        log.info("下架课程: courseId={}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(2002, "课程不存在"));

        // 下架课程
        course.unpublish();
        courseRepository.save(course);

        log.info("课程下架成功: courseId={}", courseId);
        return getCourseDetail(courseId);
    }

    /**
     * 删除课程
     */
    @Transactional
    public void deleteCourse(String courseId) {
        log.info("删除课程: courseId={}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(2002, "课程不存在"));

        // 检查课程是否可以删除（已发布的课程不能删除）
        if (course.isPublished()) {
            throw new BusinessException(2004, "已发布的课程不能删除，请先下架");
        }

        // 删除课程章节
        courseChapterRepository.deleteByCourseId(courseId);

        // 删除课程
        courseRepository.delete(course);

        log.info("课程删除成功: courseId={}", courseId);
    }

    /**
     * 获取课程详情
     */
    public CourseDTO.Response getCourseDetail(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(2002, "课程不存在"));

        // 增加浏览次数
        course.incrementViewCount();
        courseRepository.save(course);

        // 转换为响应DTO
        CourseDTO.Response response = convertToResponse(course);

        // 获取章节信息
        List<CourseChapter> chapters = courseChapterRepository.findByCourseIdOrderBySortOrderAsc(courseId);
        response.setChapters(chapters.stream()
                .map(this::convertChapterToResponse)
                .collect(Collectors.toList()));

        // 设置章节统计
        response.setChapterCount(chapters.size());
        long publishedCount = chapters.stream()
                .filter(CourseChapter::isPublished)
                .count();
        response.setPublishedChapterCount((int) publishedCount);

        return response;
    }

    /**
     * 搜索课程（支持多条件）
     */
    public Page<CourseDTO.ListItem> searchCourses(CourseDTO.SearchRequest request) {
        log.info("搜索课程: keyword={}, category={}", request.getKeyword(), request.getCategory());

        // 构建分页参数
        Sort sort = buildSort(request.getSortBy(), request.getSortOrder());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // 搜索课程
        Page<Course> coursePage = courseRepository.searchCoursesByMultipleConditions(
                request.getKeyword(),
                request.getCategory(),
                request.getDifficultyLevel(),
                request.getStatus(),
                request.getIsRequired(),
                request.getInstructorId(),
                pageable
        );

        // 转换为DTO并设置章节数量
        return coursePage.map(course -> {
            CourseDTO.ListItem item = convertToListItem(course);
            // 设置章节数量
            Long chapterCount = courseChapterRepository.countByCourseId(course.getId());
            item.setChapterCount(chapterCount.intValue());
            return item;
        });
    }

    /**
     * 获取我的课程（讲师）
     */
    public Page<CourseDTO.ListItem> getMyCourses(String instructorId, Pageable pageable) {
        log.info("获取讲师课程: instructorId={}", instructorId);

        Page<Course> coursePage = courseRepository.findByInstructorIdOrderByCreateTimeDesc(instructorId, pageable);
        return coursePage.map(course -> {
            CourseDTO.ListItem item = convertToListItem(course);
            Long chapterCount = courseChapterRepository.countByCourseId(course.getId());
            item.setChapterCount(chapterCount.intValue());
            return item;
        });
    }

    /**
     * 获取课程列表（管理员用）
     */
    public Page<CourseDTO.ListItem> getCoursesForAdmin(CourseDTO.SearchRequest request) {
        log.info("管理员获取课程列表");
        return searchCourses(request);
    }

    /**
     * 获取推荐课程
     */
    public Page<CourseDTO.ListItem> getRecommendedCourses(Pageable pageable) {
        log.info("获取推荐课程");

        Page<Course> coursePage = courseRepository.findRecommendedCourses(1, pageable);
        return coursePage.map(this::convertToListItem);
    }

    /**
     * 获取热门课程
     */
    public Page<CourseDTO.ListItem> getPopularCourses(Pageable pageable) {
        log.info("获取热门课程");

        Page<Course> coursePage = courseRepository.findByStatusOrderByStudentCountDescCreateTimeDesc(1, pageable);
        return coursePage.map(this::convertToListItem);
    }

    /**
     * 获取最新课程
     */
    public Page<CourseDTO.ListItem> getLatestCourses(Pageable pageable) {
        log.info("获取最新课程");

        Long oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
        Page<Course> coursePage = courseRepository.findByStatusAndPublishTimeAfterOrderByPublishTimeDesc(1, oneWeekAgo, pageable);
        return coursePage.map(this::convertToListItem);
    }

    /**
     * 获取课程统计
     */
    public CourseDTO.Statistics getCourseStatistics() {
        log.info("获取课程统计");

        CourseDTO.Statistics statistics = new CourseDTO.Statistics();

        // 基础统计
        statistics.setTotalCourses(courseRepository.count());
        statistics.setPublishedCourses(courseRepository.countByStatus(1));
        statistics.setDraftCourses(courseRepository.countByStatus(0));
        statistics.setUnpublishedCourses(courseRepository.countByStatus(2));

        return statistics;
    }

    /**
     * 获取课程分类列表
     */
    public List<String> getCourseCategories() {
        return courseRepository.findDistinctCategoriesByStatus(1);
    }

    // ==================== 章节管理方法 ====================

    /**
     * 为课程创建章节
     */
    @Transactional
    public void createChaptersForCourse(String courseId, List<CourseChapterDTO.CreateRequest> chapterRequests) {
        for (CourseChapterDTO.CreateRequest chapterRequest : chapterRequests) {
            CourseChapter chapter = new CourseChapter();
            BeanUtils.copyProperties(chapterRequest, chapter);
            chapter.setCourseId(courseId);

            // 如果没有指定排序序号，自动设置
            if (chapter.getSortOrder() == null) {
                Integer maxOrder = courseChapterRepository.findMaxSortOrderByCourseId(courseId);
                chapter.setSortOrder(maxOrder == null ? 1 : maxOrder + 1);
            }

            courseChapterRepository.save(chapter);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 验证课程是否可以发布
     */
    private void validateCourseForPublish(Course course) {
        if (!StringUtils.hasText(course.getTitle())) {
            throw new BusinessException(2003, "课程标题不能为空");
        }
        if (!StringUtils.hasText(course.getDescription())) {
            throw new BusinessException(2003, "课程描述不能为空");
        }
        if (!StringUtils.hasText(course.getCategory())) {
            throw new BusinessException(2003, "课程分类不能为空");
        }

        // 检查是否有章节
        Long chapterCount = courseChapterRepository.countByCourseId(course.getId());
        if (chapterCount == 0) {
            throw new BusinessException(2003, "课程至少需要一个章节才能发布");
        }
    }

    /**
     * 转换为响应DTO
     */
    private CourseDTO.Response convertToResponse(Course course) {
        CourseDTO.Response response = new CourseDTO.Response();
        BeanUtils.copyProperties(course, response);
        response.setStatusText(course.getStatusText());
        response.setDifficultyText(course.getDifficultyText());
        return response;
    }

    /**
     * 转换为列表项DTO
     */
    private CourseDTO.ListItem convertToListItem(Course course) {
        CourseDTO.ListItem item = new CourseDTO.ListItem();
        BeanUtils.copyProperties(course, item);
        item.setStatusText(course.getStatusText());
        item.setDifficultyText(course.getDifficultyText());
        return item;
    }

    /**
     * 转换章节为响应DTO
     */
    private CourseChapterDTO.Response convertChapterToResponse(CourseChapter chapter) {
        CourseChapterDTO.Response response = new CourseChapterDTO.Response();
        BeanUtils.copyProperties(chapter, response);
        response.setChapterTypeText(chapter.getChapterTypeText());
        response.setStatusText(chapter.getStatusText());
        return response;
    }

    /**
     * 构建排序参数
     */
    private Sort buildSort(String sortBy, String sortOrder) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        switch (sortBy) {
            case "rating":
                return Sort.by(direction, "rating", "createTime");
            case "studentCount":
                return Sort.by(direction, "studentCount", "createTime");
            case "publishTime":
                return Sort.by(direction, "publishTime", "createTime");
            case "title":
                return Sort.by(direction, "title");
            default:
                return Sort.by(direction, "createTime");
        }
    }
}