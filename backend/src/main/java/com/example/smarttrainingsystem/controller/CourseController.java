package com.example.smarttrainingsystem.controller;

import com.example.smarttrainingsystem.common.Result;
import com.example.smarttrainingsystem.dto.CourseDTO;
import com.example.smarttrainingsystem.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 课程API控制器
 * 
 * @author 开发者
 * @version 1.0
 * @since 2025-07-17
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Validated
public class CourseController {

    private final CourseService courseService;

    /**
     * 创建课程 - 需要ADMIN或TEACHER权限
     * 
     * @param request 创建请求
     * @return 课程详情
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<CourseDTO.Response> createCourse(@Valid @RequestBody CourseDTO.CreateRequest request) {
        log.info("接收创建课程请求: {}", request.getTitle());
        CourseDTO.Response response = courseService.createCourse(request);
        return Result.success(response);
    }

    /**
     * 更新课程 - 需要ADMIN或TEACHER权限
     * 
     * @param courseId 课程ID
     * @param request 更新请求
     * @return 课程详情
     */
    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<CourseDTO.Response> updateCourse(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId,
            @Valid @RequestBody CourseDTO.UpdateRequest request) {
        log.info("接收更新课程请求: courseId={}", courseId);
        CourseDTO.Response response = courseService.updateCourse(courseId, request);
        return Result.success(response);
    }

    /**
     * 发布课程 - 需要ADMIN或TEACHER权限
     * 
     * @param courseId 课程ID
     * @return 课程详情
     */
    @PostMapping("/{courseId}/publish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<CourseDTO.Response> publishCourse(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId) {
        log.info("接收发布课程请求: courseId={}", courseId);
        CourseDTO.Response response = courseService.publishCourse(courseId);
        return Result.success(response);
    }

    /**
     * 下架课程 - 需要ADMIN权限
     * 
     * @param courseId 课程ID
     * @return 课程详情
     */
    @PostMapping("/{courseId}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CourseDTO.Response> unpublishCourse(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId) {
        log.info("接收下架课程请求: courseId={}", courseId);
        CourseDTO.Response response = courseService.unpublishCourse(courseId);
        return Result.success(response);
    }

    /**
     * 删除课程 - 需要ADMIN权限
     * 
     * @param courseId 课程ID
     * @return 成功消息
     */
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteCourse(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId) {
        log.info("接收删除课程请求: courseId={}", courseId);
        courseService.deleteCourse(courseId);
        return Result.success("课程删除成功");
    }

    /**
     * 获取课程详情 - 所有已认证用户可访问
     * 
     * @param courseId 课程ID
     * @return 课程详情
     */
    @GetMapping("/{courseId}")
    public Result<CourseDTO.Response> getCourseDetail(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId) {
        CourseDTO.Response response = courseService.getCourseDetail(courseId);
        return Result.success(response);
    }

    /**
     * 搜索课程 - 所有已认证用户可访问
     * 
     * @param keyword 搜索关键词
     * @param category 课程分类
     * @param difficultyLevel 难度级别
     * @param isRequired 是否必修
     * @param sortBy 排序字段
     * @param sortOrder 排序方向
     * @param page 页码
     * @param size 页面大小
     * @return 课程分页列表
     */
    @GetMapping("/search")
    public Result<Page<CourseDTO.ListItem>> searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer difficultyLevel,
            @RequestParam(required = false) Boolean isRequired,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        CourseDTO.SearchRequest request = new CourseDTO.SearchRequest();
        request.setKeyword(keyword);
        request.setCategory(category);
        request.setDifficultyLevel(difficultyLevel);
        request.setIsRequired(isRequired);
        request.setSortBy(sortBy);
        request.setSortOrder(sortOrder);
        request.setPage(page);
        request.setSize(size);
        
        Page<CourseDTO.ListItem> result = courseService.searchCourses(request);
        return Result.success(result);
    }

    /**
     * 获取管理员课程列表 - 需要ADMIN权限
     * 
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 页面大小
     * @return 课程分页列表
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<CourseDTO.ListItem>> getCoursesForAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        CourseDTO.SearchRequest request = new CourseDTO.SearchRequest();
        request.setKeyword(keyword);
        request.setPage(page);
        request.setSize(size);
        
        Page<CourseDTO.ListItem> result = courseService.getCoursesForAdmin(request);
        return Result.success(result);
    }

    /**
     * 获取我的课程 - 需要TEACHER权限
     * 
     * @param instructorId 讲师ID
     * @param page 页码
     * @param size 页面大小
     * @return 课程分页列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<Page<CourseDTO.ListItem>> getMyCourses(
            @RequestParam String instructorId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CourseDTO.ListItem> result = courseService.getMyCourses(instructorId, pageable);
        return Result.success(result);
    }

    /**
     * 获取推荐课程 - 所有已认证用户可访问
     * 
     * @param page 页码
     * @param size 页面大小
     * @return 推荐课程列表
     */
    @GetMapping("/recommended")
    public Result<Page<CourseDTO.ListItem>> getRecommendedCourses(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CourseDTO.ListItem> result = courseService.getRecommendedCourses(pageable);
        return Result.success(result);
    }

    /**
     * 获取热门课程 - 所有已认证用户可访问
     * 
     * @param page 页码
     * @param size 页面大小
     * @return 热门课程列表
     */
    @GetMapping("/popular")
    public Result<Page<CourseDTO.ListItem>> getPopularCourses(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CourseDTO.ListItem> result = courseService.getPopularCourses(pageable);
        return Result.success(result);
    }

    /**
     * 获取最新课程 - 所有已认证用户可访问
     * 
     * @param page 页码
     * @param size 页面大小
     * @return 最新课程列表
     */
    @GetMapping("/latest")
    public Result<Page<CourseDTO.ListItem>> getLatestCourses(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CourseDTO.ListItem> result = courseService.getLatestCourses(pageable);
        return Result.success(result);
    }

    /**
     * 获取课程统计 - 需要ADMIN权限
     * 
     * @return 课程统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CourseDTO.Statistics> getCourseStatistics() {
        CourseDTO.Statistics statistics = courseService.getCourseStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取课程分类列表 - 所有已认证用户可访问
     * 
     * @return 分类列表
     */
    @GetMapping("/categories")
    public Result<List<String>> getCourseCategories() {
        List<String> categories = courseService.getCourseCategories();
        return Result.success(categories);
    }
}