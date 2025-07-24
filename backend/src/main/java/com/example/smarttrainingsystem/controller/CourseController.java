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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

/**
 * 课程API控制器
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

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null || userId.trim().isEmpty()) {
            log.error("无法从请求中获取用户ID");
            throw new RuntimeException("用户认证信息缺失");
        }
        return userId;
    }

    /**
     * 创建课程 - 需要ADMIN或TEACHER权限
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<CourseDTO.Response> createCourse(
            @Valid @RequestBody CourseDTO.CreateRequest request,
            HttpServletRequest httpRequest) {
        String userId = getCurrentUserId(httpRequest);
        log.info("创建课程: title={}, userId={}", request.getTitle(), userId);

        CourseDTO.Response response = courseService.createCourse(request, userId);
        return Result.success(response);
    }

    /**
     * 更新课程 - 需要ADMIN或TEACHER权限
     */
    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<CourseDTO.Response> updateCourse(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId,
            @Valid @RequestBody CourseDTO.UpdateRequest request,
            HttpServletRequest httpRequest) {
        String userId = getCurrentUserId(httpRequest);
        log.info("更新课程: courseId={}, userId={}", courseId, userId);

        CourseDTO.Response response = courseService.updateCourse(courseId, request, userId);
        return Result.success(response);
    }

    /**
     * 删除课程 - 需要ADMIN或TEACHER权限
     */
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<String> deleteCourse(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId,
            HttpServletRequest httpRequest) {
        String userId = getCurrentUserId(httpRequest);
        log.info("删除课程: courseId={}, userId={}", courseId, userId);

        courseService.deleteCourse(courseId, userId);
        return Result.success("课程删除成功");
    }

    /**
     * 获取课程详情 - 所有已认证用户可访问
     */
    @GetMapping("/{courseId}")
    public Result<CourseDTO.Response> getCourseDetail(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId) {
        log.info("获取课程详情: courseId={}", courseId);

        CourseDTO.Response response = courseService.getCourseDetail(courseId);
        return Result.success(response);
    }

    /**
     * 获取课程列表 - 所有已认证用户可访问
     */
    @GetMapping
    public Result<Page<CourseDTO.ListItem>> getCourseList(CourseDTO.SearchRequest searchRequest) {
        log.info("获取课程列表: {}", searchRequest);

        // 🔧 修正：使用 PageRequest.of 创建 Pageable
        Pageable pageable = PageRequest.of(
                searchRequest.getPage() != null ? searchRequest.getPage() : 0,
                searchRequest.getSize() != null ? searchRequest.getSize() : 10
        );

        Page<CourseDTO.ListItem> result = courseService.getCourseList(searchRequest, pageable);
        return Result.success(result);
    }

    /**
     * 发布课程 - 需要ADMIN或TEACHER权限
     */
    @PostMapping("/{courseId}/publish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<CourseDTO.Response> publishCourse(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId,
            HttpServletRequest httpRequest) {
        String userId = getCurrentUserId(httpRequest);
        log.info("发布课程: courseId={}, userId={}", courseId, userId);

        CourseDTO.Response response = courseService.publishCourse(courseId, userId);
        return Result.success(response);
    }

    /**
     * 发布课程 (新接口) - 提供给前端PUT方式
     */
    @PutMapping("/{courseId}/publish")
    public ResponseEntity<?> publishCourseByPut(@PathVariable String courseId) {
        try {
            boolean success = courseService.publishCourse(courseId);
            if (success) {
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            log.error("发布课程失败", e);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("发布失败");
    }

    /**
     * 下架课程 - 需要ADMIN或TEACHER权限
     */
    @PostMapping("/{courseId}/unpublish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<CourseDTO.Response> unpublishCourse(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId,
            HttpServletRequest httpRequest) {
        String userId = getCurrentUserId(httpRequest);
        log.info("下架课程: courseId={}, userId={}", courseId, userId);

        CourseDTO.Response response = courseService.unpublishCourse(courseId, userId);
        return Result.success(response);
    }

    /**
     * 下架课程 (新接口) - 提供给前端PUT方式
     */
    @PutMapping("/{courseId}/unpublish")
    public ResponseEntity<?> unpublishCourseByPut(@PathVariable String courseId) {
        try {
            boolean success = courseService.unpublishCourse(courseId);
            if (success) {
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            log.error("下架课程失败", e);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("下架失败");
    }

    /**
     * 搜索课程 - 所有已认证用户可访问
     */
    @GetMapping("/search")
    public Result<Page<CourseDTO.ListItem>> searchCourses(CourseDTO.SearchRequest searchRequest) {
        log.info("搜索课程: {}", searchRequest);

        // 🔧 修正：使用 PageRequest.of 创建 Pageable
        Pageable pageable = PageRequest.of(
                searchRequest.getPage() != null ? searchRequest.getPage() : 0,
                searchRequest.getSize() != null ? searchRequest.getSize() : 10
        );

        Page<CourseDTO.ListItem> result = courseService.searchCourses(searchRequest, pageable);
        return Result.success(result);
    }

    /**
     * 获取推荐课程 - 所有已认证用户可访问
     */
    @GetMapping("/recommended")
    public Result<Page<CourseDTO.ListItem>> getRecommendedCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("获取推荐课程: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<CourseDTO.ListItem> result = courseService.getRecommendedCourses(pageable);
        return Result.success(result);
    }

    /**
     * 获取热门课程 - 所有已认证用户可访问
     */
    @GetMapping("/popular")
    public Result<Page<CourseDTO.ListItem>> getPopularCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("获取热门课程: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<CourseDTO.ListItem> result = courseService.getPopularCourses(pageable);
        return Result.success(result);
    }

    /**
     * 获取最新课程 - 所有已认证用户可访问
     */
    @GetMapping("/latest")
    public Result<Page<CourseDTO.ListItem>> getLatestCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("获取最新课程: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<CourseDTO.ListItem> result = courseService.getLatestCourses(pageable);
        return Result.success(result);
    }

    /**
     * 获取课程统计 - 需要ADMIN权限
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
     */
    @GetMapping("/categories")
    public Result<List<String>> getCourseCategories() {
        log.info("获取课程分类列表");

        List<String> categories = courseService.getCourseCategories();
        return Result.success(categories);
    }
}