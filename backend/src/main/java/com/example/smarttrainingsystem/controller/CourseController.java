// 文件路径: backend/src/main/java/com/example/smarttrainingsystem/controller/CourseController.java
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
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 课程管理API控制器
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-07-18
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class CourseController {

    private final CourseService courseService;

    // ==================== 课程CRUD操作 ====================

    /**
     * 创建课程 - 需要ADMIN或TEACHER权限
     *
     * @param request 创建请求
     * @param authentication 认证信息
     * @return 课程详情
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<CourseDTO.Response> createCourse(
            @Valid @RequestBody CourseDTO.CreateRequest request,
            Authentication authentication) {
        log.info("接收创建课程请求: {} - 操作用户: {}", request.getTitle(), authentication.getName());

        // 如果是讲师，自动设置为当前用户
        if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER"))) {
            // TODO: 从认证信息中获取用户ID
            // request.setInstructorId(getCurrentUserId(authentication));
        }

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
     * 获取课程详情 - 所有已认证用户可访问
     *
     * @param courseId 课程ID
     * @return 课程详情
     */
    @GetMapping("/{courseId}")
    public Result<CourseDTO.Response> getCourseDetail(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId) {
        log.info("获取课程详情: courseId={}", courseId);
        CourseDTO.Response response = courseService.getCourseDetail(courseId);
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

    // ==================== 课程状态管理 ====================

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

    // ==================== 课程查询接口 ====================

    /**
     * 搜索课程 - 所有已认证用户可访问
     * 支持前端页面的多条件筛选
     *
     * @param keyword 搜索关键词
     * @param category 课程分类
     * @param difficultyLevel 难度级别
     * @param status 课程状态
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
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Boolean isRequired,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        log.info("搜索课程: keyword={}, category={}, status={}", keyword, category, status);

        CourseDTO.SearchRequest request = new CourseDTO.SearchRequest();
        request.setKeyword(keyword);
        request.setCategory(category);
        request.setDifficultyLevel(difficultyLevel);
        request.setStatus(status);
        request.setIsRequired(isRequired);
        request.setSortBy(sortBy);
        request.setSortOrder(sortOrder);
        request.setPage(page);
        request.setSize(size);

        Page<CourseDTO.ListItem> result = courseService.searchCourses(request);
        return Result.success(result);
    }

    /**
     * 获取我的课程（讲师） - 需要TEACHER权限
     *
     * @param page 页码
     * @param size 页面大小
     * @param authentication 认证信息
     * @return 课程分页列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<Page<CourseDTO.ListItem>> getMyCourses(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            Authentication authentication) {

        log.info("获取我的课程: 讲师={}", authentication.getName());

        // TODO: 从认证信息中获取讲师ID
        String instructorId = "current-instructor-id"; // 临时hardcode

        Pageable pageable = PageRequest.of(page, size);
        Page<CourseDTO.ListItem> result = courseService.getMyCourses(instructorId, pageable);
        return Result.success(result);
    }

    /**
     * 获取课程列表（管理员） - 需要ADMIN权限
     *
     * @param keyword 搜索关键词
     * @param category 课程分类
     * @param status 课程状态
     * @param page 页码
     * @param size 页面大小
     * @return 课程分页列表
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<CourseDTO.ListItem>> getCoursesForAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        log.info("管理员获取课程列表");

        CourseDTO.SearchRequest request = new CourseDTO.SearchRequest();
        request.setKeyword(keyword);
        request.setCategory(category);
        request.setStatus(status);
        request.setPage(page);
        request.setSize(size);

        Page<CourseDTO.ListItem> result = courseService.getCoursesForAdmin(request);
        return Result.success(result);
    }

    // ==================== 推荐和热门课程 ====================

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

        log.info("获取推荐课程");
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

        log.info("获取热门课程");
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

        log.info("获取最新课程");
        Pageable pageable = PageRequest.of(page, size);
        Page<CourseDTO.ListItem> result = courseService.getLatestCourses(pageable);
        return Result.success(result);
    }

    // ==================== 统计和辅助接口 ====================

    /**
     * 获取课程统计 - 需要ADMIN权限
     *
     * @return 课程统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CourseDTO.Statistics> getCourseStatistics() {
        log.info("获取课程统计");
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
        log.info("获取课程分类列表");
        List<String> categories = courseService.getCourseCategories();
        return Result.success(categories);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 从认证信息中获取当前用户ID
     * TODO: 根据实际的认证实现来完善此方法
     */
    private String getCurrentUserId(Authentication authentication) {
        // 这里需要根据实际的JWT实现来获取用户ID
        return authentication.getName(); // 临时实现
    }
}