// 文件路径: backend/src/main/java/com/example/smarttrainingsystem/controller/CourseChapterController.java
package com.example.smarttrainingsystem.controller;

import com.example.smarttrainingsystem.common.Result;
import com.example.smarttrainingsystem.dto.CourseChapterDTO;
import com.example.smarttrainingsystem.service.CourseChapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 课程章节API控制器
 * 
 * @author 开发者
 * @version 1.0
 * @since 2025-07-18
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/courses/{courseId}/chapters")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class CourseChapterController {

    private final CourseChapterService chapterService;

    /**
     * 创建章节 - 需要ADMIN或TEACHER权限
     * 
     * @param courseId 课程ID
     * @param request 创建请求
     * @return 章节详情
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<CourseChapterDTO.Response> createChapter(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId,
            @Valid @RequestBody CourseChapterDTO.CreateRequest request) {
        log.info("创建章节: courseId={}, title={}", courseId, request.getTitle());
        request.setCourseId(courseId);
        CourseChapterDTO.Response response = chapterService.createChapter(request);
        return Result.success(response);
    }

    /**
     * 更新章节 - 需要ADMIN或TEACHER权限
     * 
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @param request 更新请求
     * @return 章节详情
     */
    @PutMapping("/{chapterId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<CourseChapterDTO.Response> updateChapter(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId,
            @PathVariable @NotBlank(message = "章节ID不能为空") String chapterId,
            @Valid @RequestBody CourseChapterDTO.UpdateRequest request) {
        log.info("更新章节: chapterId={}", chapterId);
        CourseChapterDTO.Response response = chapterService.updateChapter(chapterId, request);
        return Result.success(response);
    }

    /**
     * 删除章节 - 需要ADMIN或TEACHER权限
     * 
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @return 成功消息
     */
    @DeleteMapping("/{chapterId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<String> deleteChapter(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId,
            @PathVariable @NotBlank(message = "章节ID不能为空") String chapterId) {
        log.info("删除章节: chapterId={}", chapterId);
        chapterService.deleteChapter(chapterId);
        return Result.success("章节删除成功");
    }

    /**
     * 获取课程章节列表 - 所有已认证用户可访问
     * 
     * @param courseId 课程ID
     * @param status 章节状态（可选）
     * @return 章节列表
     */
    @GetMapping
    public Result<List<CourseChapterDTO.Response>> getCourseChapters(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId,
            @RequestParam(required = false) Integer status) {
        log.info("获取课程章节列表: courseId={}", courseId);
        List<CourseChapterDTO.Response> chapters = chapterService.getCourseChapters(courseId, status);
        return Result.success(chapters);
    }

    /**
     * 获取章节详情 - 所有已认证用户可访问
     * 
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @return 章节详情
     */
    @GetMapping("/{chapterId}")
    public Result<CourseChapterDTO.Response> getChapterDetail(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId,
            @PathVariable @NotBlank(message = "章节ID不能为空") String chapterId) {
        log.info("获取章节详情: chapterId={}", chapterId);
        CourseChapterDTO.Response response = chapterService.getChapterDetail(chapterId);
        return Result.success(response);
    }

    /**
     * 批量调整章节顺序 - 需要ADMIN或TEACHER权限
     * 
     * @param courseId 课程ID
     * @param request 批量排序请求
     * @return 成功消息
     */
    @PostMapping("/sort")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<String> sortChapters(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId,
            @Valid @RequestBody CourseChapterDTO.BatchSortRequest request) {
        log.info("批量调整章节顺序: courseId={}", courseId);
        request.setCourseId(courseId);
        chapterService.batchSortChapters(request);
        return Result.success("章节顺序调整成功");
    }

    /**
     * 发布章节 - 需要ADMIN或TEACHER权限
     * 
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @return 章节详情
     */
    @PostMapping("/{chapterId}/publish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<CourseChapterDTO.Response> publishChapter(
            @PathVariable @NotBlank(message = "课程ID不能为空") String courseId,
            @PathVariable @NotBlank(message = "章节ID不能为空") String chapterId) {
        log.info("发布章节: chapterId={}", chapterId);
        CourseChapterDTO.Response response = chapterService.publishChapter(chapterId);
        return Result.success(response);
    }
}