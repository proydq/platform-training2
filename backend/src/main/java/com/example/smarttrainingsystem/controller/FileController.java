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
 * 文件上传控制器
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
     * 通用文件上传接口
     * 
     * @param file 上传的文件
     * @param category 文件分类
     * @param userId 用户ID
     * @return 文件访问URL
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public Result<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "temp") String category,
            @RequestParam String userId) {
        
        log.info("接收文件上传请求: fileName={}, category={}, userId={}", 
                file.getOriginalFilename(), category, userId);
        
        String fileUrl = fileUploadService.uploadFile(file, category, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("originalName", file.getOriginalFilename());
        result.put("size", file.getSize());
        result.put("contentType", file.getContentType());
        
        return Result.success(result);
    }

    /**
     * 上传课程封面图片
     * 
     * @param file 图片文件
     * @param userId 用户ID
     * @return 文件访问URL
     */
    @PostMapping("/upload/course-cover")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<Map<String, Object>> uploadCourseCover(
            @RequestParam("file") MultipartFile file,
            @RequestParam String userId) {
        
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
     * 
     * @param file 视频文件
     * @param userId 用户ID
     * @return 文件访问URL
     */
    @PostMapping("/upload/course-video")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<Map<String, Object>> uploadCourseVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam String userId) {
        
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
     * 
     * @param file 文档文件
     * @param userId 用户ID
     * @return 文件访问URL
     */
    @PostMapping("/upload/course-document")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<Map<String, Object>> uploadCourseDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam String userId) {
        
        log.info("接收课程文档上传请求: fileName={}, userId={}", file.getOriginalFilename(), userId);
        
        String fileUrl = fileUploadService.uploadCourseDocument(file, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("originalName", file.getOriginalFilename());
        result.put("size", file.getSize());
        
        return Result.success(result);
    }

    /**
     * 上传用户头像
     * 
     * @param file 头像文件
     * @param userId 用户ID
     * @return 文件访问URL
     */
    @PostMapping("/upload/avatar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public Result<Map<String, Object>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam String userId) {
        
        log.info("接收头像上传请求: fileName={}, userId={}", file.getOriginalFilename(), userId);
        
        String fileUrl = fileUploadService.uploadAvatar(file, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("originalName", file.getOriginalFilename());
        result.put("size", file.getSize());
        
        return Result.success(result);
    }

    /**
     * 文件下载/访问接口
     * 
     * @param category 文件分类
     * @param userId 用户ID
     * @param year 年份
     * @param month 月份
     * @param filename 文件名
     * @param request HTTP请求
     * @return 文件资源
     */
    @GetMapping("/files/{category}/{userId}/{year}/{month}/{filename}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String category,
            @PathVariable String userId,
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String filename,
            HttpServletRequest request) {
        
        try {
            // 构建文件路径
            Path filePath = Paths.get(uploadPath)
                    .resolve(category)
                    .resolve(userId)
                    .resolve(year)
                    .resolve(month)
                    .resolve(filename);
            
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists()) {
                log.warn("请求的文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // 确定文件的内容类型
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                log.info("无法确定文件类型");
            }
            
            // 如果无法确定内容类型，使用默认值
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
                    
        } catch (MalformedURLException ex) {
            log.error("文件下载失败: {}", ex.getMessage(), ex);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除文件
     * 
     * @param fileUrl 文件URL
     * @return 删除结果
     */
    @DeleteMapping("/files")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<String> deleteFile(@RequestParam String fileUrl) {
        log.info("接收文件删除请求: fileUrl={}", fileUrl);
        
        boolean success = fileUploadService.deleteFile(fileUrl);
        
        if (success) {
            return Result.success("文件删除成功");
        } else {
            return Result.error(4008, "文件删除失败");
        }
    }

    /**
     * 获取文件信息
     * 
     * @param fileUrl 文件URL
     * @return 文件信息
     */
    @GetMapping("/files/info")
    public Result<FileUploadService.FileInfo> getFileInfo(@RequestParam String fileUrl) {
        FileUploadService.FileInfo fileInfo = fileUploadService.getFileInfo(fileUrl);
        
        if (fileInfo != null) {
            return Result.success(fileInfo);
        } else {
            return Result.error(4009, "文件不存在或获取信息失败");
        }
    }
}