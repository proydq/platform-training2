package com.example.smarttrainingsystem.service;

import com.example.smarttrainingsystem.dto.CourseDTO;
import com.example.smarttrainingsystem.entity.Course;
import com.example.smarttrainingsystem.exception.BusinessException;
import com.example.smarttrainingsystem.repository.CourseRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程业务服务类
 * 
 * @author 开发者
 * @version 1.0
 * @since 2025-07-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    /**
     * 创建课程
     * 
     * @param request 创建请求
     * @return 课程详情
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
        course.setRating(BigDecimal.ZERO);
        course.setRatingCount(0);
        course.setStudentCount(0);
        
        // 保存课程
        course = courseRepository.save(course);
        
        log.info("课程创建成功: courseId={}", course.getId());
        return convertToResponse(course);
    }

    /**
     * 更新课程
     * 
     * @param courseId 课程ID
     * @param request 更新请求
     * @return 课程详情
     */
    @Transactional
    public CourseDTO.Response updateCourse(String courseId, CourseDTO.UpdateRequest request) {
        log.info("更新课程: courseId={}", courseId);
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BusinessException(2002, "课程不存在"));
        
        // 只更新非空字段
        if (StringUtils.hasText(request.getTitle())) {
            course.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getDescription())) {
            course.setDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getCoverImageUrl())) {
            course.setCoverImageUrl(request.getCoverImageUrl());
        }
        if (StringUtils.hasText(request.getCategory())) {
            course.setCategory(request.getCategory());
        }
        if (StringUtils.hasText(request.getTags())) {
            course.setTags(request.getTags());
        }
        if (request.getDifficultyLevel() != null) {
            course.setDifficultyLevel(request.getDifficultyLevel());
        }
        if (request.getEstimatedDuration() != null) {
            course.setEstimatedDuration(request.getEstimatedDuration());
        }
        if (StringUtils.hasText(request.getInstructorName())) {
            course.setInstructorName(request.getInstructorName());
        }
        if (StringUtils.hasText(request.getLearningObjectives())) {
            course.setLearningObjectives(request.getLearningObjectives());
        }
        if (StringUtils.hasText(request.getPrerequisites())) {
            course.setPrerequisites(request.getPrerequisites());
        }
        if (request.getIsRequired() != null) {
            course.setIsRequired(request.getIsRequired());
        }
        if (request.getValidityDays() != null) {
            course.setValidityDays(request.getValidityDays());
        }
        
        course = courseRepository.save(course);
        
        log.info("课程更新成功: courseId={}", courseId);
        return convertToResponse(course);
    }

    /**
     * 发布课程
     * 
     * @param courseId 课程ID
     * @return 课程详情
     */
    @Transactional
    public CourseDTO.Response publishCourse(String courseId) {
        log.info("发布课程: courseId={}", courseId);
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BusinessException(2002, "课程不存在"));
        
        // 检查课程是否可以发布
        if (course.isPublished()) {
            throw new BusinessException(2003, "课程已经发布");
        }
        
        // 发布课程
        course.publish();
        course = courseRepository.save(course);
        
        log.info("课程发布成功: courseId={}", courseId);
        return convertToResponse(course);
    }

    /**
     * 下架课程
     * 
     * @param courseId 课程ID
     * @return 课程详情
     */
    @Transactional
    public CourseDTO.Response unpublishCourse(String courseId) {
        log.info("下架课程: courseId={}", courseId);
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BusinessException(2002, "课程不存在"));
        
        // 下架课程
        course.unpublish();
        course = courseRepository.save(course);
        
        log.info("课程下架成功: courseId={}", courseId);
        return convertToResponse(course);
    }

    /**
     * 删除课程
     * 
     * @param courseId 课程ID
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
        
        courseRepository.delete(course);
        
        log.info("课程删除成功: courseId={}", courseId);
    }

    /**
     * 获取课程详情
     * 
     * @param courseId 课程ID
     * @return 课程详情
     */
    public CourseDTO.Response getCourseDetail(String courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BusinessException(2002, "课程不存在"));
        
        return convertToResponse(course);
    }

    /**
     * 搜索课程
     * 
     * @param request 搜索请求
     * @return 课程分页列表
     */
    public Page<CourseDTO.ListItem> searchCourses(CourseDTO.SearchRequest request) {
        log.info("搜索课程: keyword={}, category={}", request.getKeyword(), request.getCategory());
        
        // 构建分页参数
        Sort sort = buildSort(request.getSortBy(), request.getSortOrder());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        // 搜索课程
        Page<Course> coursePage;
        if (StringUtils.hasText(request.getKeyword()) || 
            StringUtils.hasText(request.getCategory()) || 
            request.getDifficultyLevel() != null) {
            // 复合条件搜索
            coursePage = courseRepository.searchCoursesByMultipleConditions(
                request.getKeyword(), 
                request.getCategory(), 
                request.getDifficultyLevel(), 
                1, // 只搜索已发布的课程
                pageable
            );
        } else {
            // 获取所有已发布课程
            coursePage = courseRepository.findByStatusOrderByCreateTimeDesc(1, pageable);
        }
        
        // 转换为DTO
        return coursePage.map(this::convertToListItem);
    }

    /**
     * 获取课程列表（管理员使用）
     * 
     * @param request 搜索请求
     * @return 课程分页列表
     */
    public Page<CourseDTO.ListItem> getCoursesForAdmin(CourseDTO.SearchRequest request) {
        log.info("管理员获取课程列表: keyword={}", request.getKeyword());
        
        // 构建分页参数
        Sort sort = buildSort(request.getSortBy(), request.getSortOrder());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        // 搜索课程（包含所有状态）
        Page<Course> coursePage;
        if (StringUtils.hasText(request.getKeyword())) {
            // 这里需要创建一个不限制状态的搜索方法
            coursePage = courseRepository.findAll(pageable);
        } else {
            coursePage = courseRepository.findAll(pageable);
        }
        
        return coursePage.map(this::convertToListItem);
    }

    /**
     * 获取我的课程（讲师）
     * 
     * @param instructorId 讲师ID
     * @param pageable 分页参数
     * @return 课程分页列表
     */
    public Page<CourseDTO.ListItem> getMyCourses(String instructorId, Pageable pageable) {
        log.info("获取讲师课程: instructorId={}", instructorId);
        
        Page<Course> coursePage = courseRepository.findByInstructorIdOrderByCreateTimeDesc(instructorId, pageable);
        return coursePage.map(this::convertToListItem);
    }

    /**
     * 获取推荐课程
     * 
     * @param pageable 分页参数
     * @return 推荐课程列表
     */
    public Page<CourseDTO.ListItem> getRecommendedCourses(Pageable pageable) {
        log.info("获取推荐课程");
        
        Page<Course> coursePage = courseRepository.findRecommendedCourses(1, pageable);
        return coursePage.map(this::convertToListItem);
    }

    /**
     * 获取热门课程
     * 
     * @param pageable 分页参数
     * @return 热门课程列表
     */
    public Page<CourseDTO.ListItem> getPopularCourses(Pageable pageable) {
        log.info("获取热门课程");
        
        Page<Course> coursePage = courseRepository.findByStatusOrderByStudentCountDescCreateTimeDesc(1, pageable);
        return coursePage.map(this::convertToListItem);
    }

    /**
     * 获取最新课程
     * 
     * @param pageable 分页参数
     * @return 最新课程列表
     */
    public Page<CourseDTO.ListItem> getLatestCourses(Pageable pageable) {
        log.info("获取最新课程");
        
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        Page<Course> coursePage = courseRepository.findByStatusAndPublishTimeAfterOrderByPublishTimeDesc(1, oneWeekAgo, pageable);
        return coursePage.map(this::convertToListItem);
    }

    /**
     * 获取课程统计
     * 
     * @return 课程统计信息
     */
    public CourseDTO.Statistics getCourseStatistics() {
        log.info("获取课程统计");
        
        CourseDTO.Statistics statistics = new CourseDTO.Statistics();
        
        // 基础统计
        statistics.setTotalCourses(courseRepository.count());
        statistics.setPublishedCourses(courseRepository.countByStatus(1));
        statistics.setDraftCourses(courseRepository.countByStatus(0));
        statistics.setUnpublishedCourses(courseRepository.countByStatus(2));
        
        // TODO: 计算平均评分和总学习人数
        // statistics.setAverageRating(...);
        // statistics.setTotalStudents(...);
        
        // TODO: 分类统计
        // statistics.setCategoryStatistics(...);
        
        return statistics;
    }

    /**
     * 获取课程分类列表
     * 
     * @return 分类列表
     */
    public List<String> getCourseCategories() {
        return courseRepository.findDistinctCategoriesByStatus(1);
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为响应DTO
     */
    private CourseDTO.Response convertToResponse(Course course) {
        CourseDTO.Response response = new CourseDTO.Response();
        BeanUtils.copyProperties(course, response);
        
        // TODO: 设置章节数量
        // response.setChapterCount(...);
        // response.setPublishedChapterCount(...);
        
        return response;
    }

    /**
     * 转换为列表项DTO
     */
    private CourseDTO.ListItem convertToListItem(Course course) {
        CourseDTO.ListItem item = new CourseDTO.ListItem();
        BeanUtils.copyProperties(course, item);
        
        // TODO: 设置章节数量
        // item.setChapterCount(...);
        
        return item;
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
            default:
                return Sort.by(direction, "createTime");
        }
    }
}