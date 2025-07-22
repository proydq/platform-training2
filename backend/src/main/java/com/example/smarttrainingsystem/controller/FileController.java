package com.example.smarttrainingsystem.controller;

import com.example.smarttrainingsystem.common.Result;
import com.example.smarttrainingsystem.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器 - 修复版
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-07-17
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService fileUploadService;

    @Value("${app.upload.path:/uploads}")
    private String uploadPath;

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
     * 🔧 通用文件上传接口 - 增强返回信息
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public Result<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "temp") String category,
            @RequestParam(value = "type", required = false) String type,
            HttpServletRequest request) {

        String userId = getCurrentUserId(request);

        log.info("接收文件上传请求: fileName={}, category={}, type={}, userId={}",
                file.getOriginalFilename(), category, type, userId);

        String fileUrl;

        // 根据类型选择合适的上传方法
        if ("cover".equals(type)) {
            fileUrl = fileUploadService.uploadCourseCover(file, userId);
        } else if ("material".equals(type)) {
            fileUrl = fileUploadService.uploadCourseDocument(file, userId);
        } else if ("video".equals(type)) {
            fileUrl = fileUploadService.uploadCourseVideo(file, userId);
        } else {
            fileUrl = fileUploadService.uploadFile(file, category, userId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("originalName", file.getOriginalFilename());
        result.put("name", file.getOriginalFilename());
        result.put("size", file.getSize());
        result.put("type", file.getContentType());

        return Result.success(result);
    }

    /**
     * 上传课程封面图片
     */
    @PostMapping("/upload/course-cover")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<Map<String, Object>> uploadCourseCover(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        String userId = getCurrentUserId(request);

        log.info("接收课程封面上传请求: fileName={}, userId={}", file.getOriginalFilename(), userId);

        String fileUrl = fileUploadService.uploadCourseCover(file, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("originalName", file.getOriginalFilename());
        result.put("size", file.getSize());

        return Result.success(result);
    }

    /**
     * 上传课程视频
     */
    @PostMapping("/upload/course-video")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<Map<String, Object>> uploadCourseVideo(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        String userId = getCurrentUserId(request);

        log.info("接收课程视频上传请求: fileName={}, userId={}", file.getOriginalFilename(), userId);

        String fileUrl = fileUploadService.uploadCourseVideo(file, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("originalName", file.getOriginalFilename());
        result.put("size", file.getSize());

        return Result.success(result);
    }

    /**
     * 上传课程文档
     */
    @PostMapping("/upload/course-document")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<Map<String, Object>> uploadCourseDocument(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        String userId = getCurrentUserId(request);

        log.info("接收课程文档上传请求: fileName={}, userId={}", file.getOriginalFilename(), userId);

        String fileUrl = fileUploadService.uploadCourseDocument(file, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("originalName", file.getOriginalFilename());
        result.put("name", file.getOriginalFilename());
        result.put("size", file.getSize());

        return Result.success(result);
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/upload/avatar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public Result<Map<String, Object>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        String userId = getCurrentUserId(request);

        log.info("接收头像上传请求: fileName={}, userId={}", file.getOriginalFilename(), userId);

        String fileUrl = fileUploadService.uploadAvatar(file, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("originalName", file.getOriginalFilename());
        result.put("size", file.getSize());

        return Result.success(result);
    }

    // ==================== 🔧 修复的文件访问接口 ====================

    /**
     * 🔧 修复：支持多级分类的文件访问接口
     * 路径格式: /files/course/covers/{userId}/{year}/{month}/{filename}
     */
    @GetMapping("/files/course/covers/{userId}/{year}/{month}/{filename}")
    public ResponseEntity<Resource> downloadCourseCover(
            @PathVariable String userId,
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String filename,
            HttpServletRequest request) {

        return downloadFileInternal("course/covers", userId, year, month, filename, request);
    }

    /**
     * 🔧 修复：支持课程视频访问
     * 路径格式: /files/course/videos/{userId}/{year}/{month}/{filename}
     */
    @GetMapping("/files/course/videos/{userId}/{year}/{month}/{filename}")
    public ResponseEntity<Resource> downloadCourseVideo(
            @PathVariable String userId,
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String filename,
            HttpServletRequest request) {

        return downloadFileInternal("course/videos", userId, year, month, filename, request);
    }

    /**
     * 🔧 修复：支持课程文档访问
     * 路径格式: /files/course/documents/{userId}/{year}/{month}/{filename}
     */
    @GetMapping("/files/course/documents/{userId}/{year}/{month}/{filename}")
    public ResponseEntity<Resource> downloadCourseDocument(
            @PathVariable String userId,
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String filename,
            HttpServletRequest request) {

        return downloadFileInternal("course/documents", userId, year, month, filename, request);
    }

    /**
     * 🔧 修复：支持用户头像访问
     * 路径格式: /files/avatars/{userId}/{year}/{month}/{filename}
     */
    @GetMapping("/files/avatars/{userId}/{year}/{month}/{filename}")
    public ResponseEntity<Resource> downloadAvatar(
            @PathVariable String userId,
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String filename,
            HttpServletRequest request) {

        return downloadFileInternal("avatars", userId, year, month, filename, request);
    }

    /**
     * 🔧 原有的单级分类文件访问接口（保持兼容性）
     * 路径格式: /files/{category}/{userId}/{year}/{month}/{filename}
     */
    @GetMapping("/files/{category}/{userId}/{year}/{month}/{filename}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String category,
            @PathVariable String userId,
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String filename,
            HttpServletRequest request) {

        return downloadFileInternal(category, userId, year, month, filename, request);
    }

    /**
     * 🔧 内部方法：统一的文件下载处理逻辑
     */
    private ResponseEntity<Resource> downloadFileInternal(
            String category,
            String userId,
            String year,
            String month,
            String filename,
            HttpServletRequest request) {

        try {
            // 构建文件路径
            Path filePath = Paths.get(uploadPath)
                    .resolve(category)
                    .resolve(userId)
                    .resolve(year)
                    .resolve(month)
                    .resolve(filename);

            log.info("尝试访问文件: {}", filePath.toAbsolutePath());

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                log.warn("请求的文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            if (!resource.isReadable()) {
                log.warn("请求的文件不可读: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 尝试确定文件的内容类型
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                log.debug("无法确定文件类型");
            }

            // 如果无法确定内容类型，则使用默认值
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            log.info("文件访问成功: {}", filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException ex) {
            log.error("文件路径格式错误: {}", ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 简化的文件访问接口（兼容性）
     */
    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> downloadSimpleFile(
            @PathVariable String filename,
            HttpServletRequest request) {

        try {
            // 在uploads目录下搜索文件
            Path filePath = Paths.get(uploadPath).resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                log.warn("请求的文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                log.debug("无法确定文件类型");
            }

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            log.info("文件访问成功: {}", filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (MalformedURLException ex) {
            log.error("文件路径格式错误: {}", ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}