package com.example.smarttrainingsystem.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文档流控制器
 * 提供课程文档的在线预览接口，设置 Content-Disposition 为 inline
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
                               HttpServletResponse response) {
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

        String contentType;
        try {
            contentType = Files.probeContentType(filePath);
        } catch (IOException e) {
            contentType = null;
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        response.setContentType(contentType);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");

        try (ServletOutputStream out = response.getOutputStream()) {
            Files.copy(filePath, out);
            out.flush();
        } catch (IOException e) {
            log.error("文档流输出失败: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
