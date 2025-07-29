package com.example.smarttrainingsystem.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文档流控制器 - 增强版
 * 提供课程文档的在线预览接口，支持多种文档类型
 *
 * @author 开发者
 * @version 2.0
 * @since 2025-07-29
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/media")
public class DocumentStreamController {

    @Value("${app.upload.path:/uploads}")
    private String uploadPath;

    /**
     * 文档流式访问接口
     * 路径: /api/v1/media/document/{userId}/{year}/{month}/{filename}
     */
    @GetMapping("/document/{userId}/{year}/{month}/{filename}")
    public void streamDocument(@PathVariable String userId,
                               @PathVariable String year,
                               @PathVariable String month,
                               @PathVariable String filename,
                               HttpServletRequest request,
                               HttpServletResponse response) {

        log.info("文档访问请求: userId={}, year={}, month={}, filename={}", userId, year, month, filename);

        Path filePath = Paths.get(uploadPath)
                .resolve("course/documents")
                .resolve(userId)
                .resolve(year)
                .resolve(month)
                .resolve(filename);

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            log.warn("请求的文档文件不存在或不可读: {}", filePath);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 获取文件MIME类型
        String contentType = getContentType(filename);
        long fileSize = 0;

        try {
            fileSize = Files.size(filePath);
        } catch (IOException e) {
            log.error("获取文件大小失败: {}", e.getMessage());
        }

        // 设置响应头
        response.setContentType(contentType);
        response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize));

        // 设置适当的Content-Disposition
        String disposition = getContentDisposition(filename, contentType);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, disposition);

        // 添加缓存控制
        response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=3600");

        // 支持范围请求（用于大文件）
        response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

        // 添加CORS支持
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, OPTIONS");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition");

        try (ServletOutputStream out = response.getOutputStream()) {
            Files.copy(filePath, out);
            out.flush();
            log.info("文档流输出成功: {}", filename);
        } catch (IOException e) {
            log.error("文档流输出失败: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 简化的文档访问接口（兼容旧版本）
     * 路径: /api/v1/media/document/{filename}
     */
    @GetMapping("/document/{filename}")
    public ResponseEntity<byte[]> streamSimpleDocument(@PathVariable String filename,
                                                       HttpServletRequest request) {
        log.info("简化文档访问请求: filename={}", filename);

        // 在uploads目录下搜索文件
        Path filePath = findFile(filename);

        if (filePath == null || !Files.exists(filePath) || !Files.isReadable(filePath)) {
            log.warn("请求的文档文件不存在: {}", filename);
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] content = Files.readAllBytes(filePath);
            String contentType = getContentType(filename);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDisposition(
                    org.springframework.http.ContentDisposition
                            .inline()
                            .filename(filename)
                            .build()
            );
            headers.setCacheControl("public, max-age=3600");

            return new ResponseEntity<>(content, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("读取文档失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 预检请求处理
     */
    @RequestMapping(value = "/document/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions() {
        return ResponseEntity.ok()
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, OPTIONS")
                .header(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600")
                .build();
    }

    /**
     * 根据文件名获取MIME类型
     */
    private String getContentType(String filename) {
        if (filename == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        switch (ext) {
            // PDF文档
            case "pdf":
                return MediaType.APPLICATION_PDF_VALUE;

            // Microsoft Word
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            // Microsoft Excel
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

            // Microsoft PowerPoint
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";

            // 文本文件
            case "txt":
                return MediaType.TEXT_PLAIN_VALUE;
            case "csv":
                return "text/csv";
            case "xml":
                return MediaType.APPLICATION_XML_VALUE;
            case "json":
                return MediaType.APPLICATION_JSON_VALUE;
            case "md":
                return "text/markdown";

            // 图片文件
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG_VALUE;
            case "png":
                return MediaType.IMAGE_PNG_VALUE;
            case "gif":
                return "image/gif";
            case "svg":
                return "image/svg+xml";

            // 默认
            default:
                return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    /**
     * 获取Content-Disposition头
     */
    private String getContentDisposition(String filename, String contentType) {
        // 对于可以直接在浏览器中显示的类型，使用inline
        if (contentType.equals(MediaType.APPLICATION_PDF_VALUE) ||
                contentType.startsWith("text/") ||
                contentType.startsWith("image/") ||
                contentType.equals(MediaType.APPLICATION_JSON_VALUE) ||
                contentType.equals(MediaType.APPLICATION_XML_VALUE)) {
            return String.format("inline; filename=\"%s\"", filename);
        }

        // 对于Office文档等，使用attachment以触发下载
        return String.format("attachment; filename=\"%s\"", filename);
    }

    /**
     * 在uploads目录下递归查找文件
     */
    private Path findFile(String filename) {
        Path uploadsPath = Paths.get(uploadPath);

        try {
            return Files.walk(uploadsPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(filename))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            log.error("搜索文件时出错: {}", e.getMessage());
            return null;
        }
    }
}