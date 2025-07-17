package com.example.smarttrainingsystem.service;

import com.example.smarttrainingsystem.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务
 * 
 * @author 开发者
 * @version 1.0
 * @since 2025-07-17
 */
@Slf4j
@Service
public class FileUploadService {

    @Value("${app.upload.path:/uploads}")
    private String uploadPath;

    @Value("${app.upload.maxFileSize:209715200}") // 默认200MB
    private long maxFileSize;

    @Value("${app.upload.allowedTypes:jpg,jpeg,png,gif,pdf,doc,docx,mp4,avi,mov}")
    private String allowedTypes;

    // 支持的文件类型
    private List<String> allowedTypeList;
    
    // 文件存储根目录
    private Path uploadDirectory;

    @PostConstruct
    public void init() {
        // 初始化允许的文件类型列表
        allowedTypeList = Arrays.asList(allowedTypes.toLowerCase().split(","));
        
        // 创建上传目录
        uploadDirectory = Paths.get(uploadPath);
        try {
            Files.createDirectories(uploadDirectory);
            log.info("文件上传目录初始化完成: {}", uploadDirectory.toAbsolutePath());
        } catch (IOException e) {
            log.error("创建上传目录失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传服务初始化失败", e);
        }
    }

    /**
     * 上传文件
     * 
     * @param file 上传的文件
     * @param category 文件分类 (avatar, course, exam, temp)
     * @param userId 用户ID
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String category, String userId) {
        log.info("开始上传文件: originalName={}, category={}, userId={}", 
                file.getOriginalFilename(), category, userId);
        
        // 验证文件
        validateFile(file);
        
        try {
            // 生成文件存储路径
            String storedFileName = generateStoredFileName(file.getOriginalFilename());
            Path categoryPath = createCategoryPath(category, userId);
            Path filePath = categoryPath.resolve(storedFileName);
            
            // 保存文件
            Files.copy(file.getInputStream(), filePath);
            
            // 生成访问URL
            String fileUrl = generateFileUrl(category, userId, storedFileName);
            
            log.info("文件上传成功: originalName={}, storedPath={}, url={}", 
                    file.getOriginalFilename(), filePath, fileUrl);
            
            return fileUrl;
            
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException(4001, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传课程封面图片
     * 
     * @param file 图片文件
     * @param userId 用户ID
     * @return 文件访问URL
     */
    public String uploadCourseCover(MultipartFile file, String userId) {
        // 验证是否为图片文件
        if (!isImageFile(file)) {
            throw new BusinessException(4002, "只能上传图片文件");
        }
        
        return uploadFile(file, "course/covers", userId);
    }

    /**
     * 上传课程视频
     * 
     * @param file 视频文件
     * @param userId 用户ID
     * @return 文件访问URL
     */
    public String uploadCourseVideo(MultipartFile file, String userId) {
        // 验证是否为视频文件
        if (!isVideoFile(file)) {
            throw new BusinessException(4003, "只能上传视频文件");
        }
        
        return uploadFile(file, "course/videos", userId);
    }

    /**
     * 上传课程文档
     * 
     * @param file 文档文件
     * @param userId 用户ID
     * @return 文件访问URL
     */
    public String uploadCourseDocument(MultipartFile file, String userId) {
        // 验证是否为文档文件
        if (!isDocumentFile(file)) {
            throw new BusinessException(4004, "只能上传文档文件");
        }
        
        return uploadFile(file, "course/documents", userId);
    }

    /**
     * 上传用户头像
     * 
     * @param file 头像文件
     * @param userId 用户ID
     * @return 文件访问URL
     */
    public String uploadAvatar(MultipartFile file, String userId) {
        // 验证是否为图片文件
        if (!isImageFile(file)) {
            throw new BusinessException(4002, "头像只能上传图片文件");
        }
        
        return uploadFile(file, "avatar", userId);
    }

    /**
     * 删除文件
     * 
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return false;
        }
        
        try {
            // 从URL解析出文件路径
            Path filePath = parseFilePathFromUrl(fileUrl);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("文件删除成功: {}", filePath);
                return true;
            } else {
                log.warn("要删除的文件不存在: {}", filePath);
                return false;
            }
            
        } catch (IOException e) {
            log.error("文件删除失败: url={}, error={}", fileUrl, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取文件信息
     * 
     * @param fileUrl 文件URL
     * @return 文件信息
     */
    public FileInfo getFileInfo(String fileUrl) {
        try {
            Path filePath = parseFilePathFromUrl(fileUrl);
            
            if (Files.exists(filePath)) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFileName(filePath.getFileName().toString());
                fileInfo.setFileSize(Files.size(filePath));
                fileInfo.setLastModified(Files.getLastModifiedTime(filePath).toInstant());
                fileInfo.setContentType(Files.probeContentType(filePath));
                
                return fileInfo;
            }
            
        } catch (IOException e) {
            log.error("获取文件信息失败: url={}, error={}", fileUrl, e.getMessage(), e);
        }
        
        return null;
    }

    // ==================== 私有方法 ====================

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException(4005, "上传文件不能为空");
        }
        
        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(4006, String.format("文件大小不能超过%dMB", maxFileSize / 1024 / 1024));
        }
        
        // 检查文件类型
        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (!allowedTypeList.contains(fileExtension.toLowerCase())) {
            throw new BusinessException(4007, "不支持的文件类型: " + fileExtension);
        }
    }

    /**
     * 生成存储文件名
     */
    private String generateStoredFileName(String originalFilename) {
        String fileExtension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        return String.format("%s_%s.%s", timestamp, uuid.substring(0, 8), fileExtension);
    }

    /**
     * 创建分类目录
     */
    private Path createCategoryPath(String category, String userId) throws IOException {
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        Path categoryPath = uploadDirectory.resolve(category).resolve(userId).resolve(datePath);
        
        Files.createDirectories(categoryPath);
        
        return categoryPath;
    }

    /**
     * 生成文件访问URL
     */
    private String generateFileUrl(String category, String userId, String fileName) {
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        return String.format("/api/v1/files/%s/%s/%s/%s", category, userId, datePath, fileName);
    }

    /**
     * 从URL解析文件路径
     */
    private Path parseFilePathFromUrl(String fileUrl) {
        // 移除URL前缀 /api/v1/files/
        String relativePath = fileUrl.replace("/api/v1/files/", "");
        return uploadDirectory.resolve(relativePath);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * 检查是否为图片文件
     */
    private boolean isImageFile(MultipartFile file) {
        String fileExtension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        List<String> imageTypes = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
        return imageTypes.contains(fileExtension);
    }

    /**
     * 检查是否为视频文件
     */
    private boolean isVideoFile(MultipartFile file) {
        String fileExtension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        List<String> videoTypes = Arrays.asList("mp4", "avi", "mov", "wmv", "flv", "webm", "mkv");
        return videoTypes.contains(fileExtension);
    }

    /**
     * 检查是否为文档文件
     */
    private boolean isDocumentFile(MultipartFile file) {
        String fileExtension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        List<String> documentTypes = Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt");
        return documentTypes.contains(fileExtension);
    }

    /**
     * 文件信息类
     */
    public static class FileInfo {
        private String fileName;
        private Long fileSize;
        private java.time.Instant lastModified;
        private String contentType;

        // Getters and Setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        
        public java.time.Instant getLastModified() { return lastModified; }
        public void setLastModified(java.time.Instant lastModified) { this.lastModified = lastModified; }
        
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
    }
}