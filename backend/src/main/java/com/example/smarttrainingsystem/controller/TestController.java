package com.example.smarttrainingsystem.controller;

import com.example.smarttrainingsystem.common.Result;
import com.example.smarttrainingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试接口控制器
 * 用于系统健康检查和环境验证
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-07-17
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

    private final DataSource dataSource;
    private final UserRepository userRepository;

    /**
     * 健康检查接口
     *
     * @return 系统状态信息
     */
    @GetMapping("/hello")
    public Result<Map<String, Object>> hello() {
        log.info("收到健康检查请求");

        Map<String, Object> data = new HashMap<>();
        data.put("message", "智能培训系统运行正常");
        data.put("timestamp", LocalDateTime.now());
        data.put("version", "Phase 3.0 - 课程管理模块");
        data.put("status", "healthy");

        return Result.success("系统运行正常", data);
    }

    /**
     * 数据库连接测试
     *
     * @return 数据库连接状态
     */
    @GetMapping("/db")
    public Result<Map<String, Object>> testDatabase() {
        log.info("收到数据库连接测试请求");

        Map<String, Object> data = new HashMap<>();

        try {
            // 测试数据库连接
            try (Connection connection = dataSource.getConnection()) {
                boolean isValid = connection.isValid(5);
                data.put("connectionStatus", isValid ? "connected" : "disconnected");
                data.put("databaseUrl", connection.getMetaData().getURL());
                data.put("databaseProduct", connection.getMetaData().getDatabaseProductName());
                data.put("databaseVersion", connection.getMetaData().getDatabaseProductVersion());
            }

            // 测试数据库表
            long userCount = userRepository.count();
            data.put("userTableExists", true);
            data.put("userCount", userCount);
            data.put("testTime", LocalDateTime.now());

            log.info("数据库连接测试成功，用户数量: {}", userCount);
            return Result.success("数据库连接正常", data);

        } catch (Exception e) {
            log.error("数据库连接测试失败: {}", e.getMessage(), e);
            data.put("connectionStatus", "failed");
            data.put("errorMessage", e.getMessage());
            data.put("testTime", LocalDateTime.now());

            return Result.error(9001, "数据库连接失败: " + e.getMessage());
        }
    }

    /**
     * 系统信息接口
     *
     * @return 系统详细信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> systemInfo() {
        log.info("收到系统信息查询请求");

        Map<String, Object> data = new HashMap<>();

        // JVM信息
        Runtime runtime = Runtime.getRuntime();
        data.put("javaVersion", System.getProperty("java.version"));
        data.put("javaVendor", System.getProperty("java.vendor"));
        data.put("osName", System.getProperty("os.name"));
        data.put("osVersion", System.getProperty("os.version"));

        // 内存信息
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        data.put("totalMemoryMB", totalMemory / 1024 / 1024);
        data.put("usedMemoryMB", usedMemory / 1024 / 1024);
        data.put("freeMemoryMB", freeMemory / 1024 / 1024);

        // 应用信息
        data.put("applicationName", "智能培训系统");
        data.put("phase", "Phase 3 - 课程管理模块");
        data.put("serverTime", LocalDateTime.now());

        return Result.success("系统信息获取成功", data);
    }

    /**
     * 接口列表 - 显示已实现的API
     *
     * @return API列表
     */
    @GetMapping("/apis")
    public Result<Map<String, Object>> apiList() {
        log.info("收到API列表查询请求");

        Map<String, Object> data = new HashMap<>();

        // 认证接口
        Map<String, String> authApis = new HashMap<>();
        authApis.put("POST /api/v1/auth/login", "用户登录");
        authApis.put("GET /api/v1/auth/userinfo", "获取用户信息");
        authApis.put("POST /api/v1/auth/logout", "用户退出");
        data.put("认证接口", authApis);

        // 课程管理接口
        Map<String, String> courseApis = new HashMap<>();
        courseApis.put("POST /api/v1/courses", "创建课程");
        courseApis.put("GET /api/v1/courses/{id}", "获取课程详情");
        courseApis.put("PUT /api/v1/courses/{id}", "更新课程");
        courseApis.put("DELETE /api/v1/courses/{id}", "删除课程");
        courseApis.put("POST /api/v1/courses/{id}/publish", "发布课程");
        courseApis.put("POST /api/v1/courses/{id}/unpublish", "下架课程");
        courseApis.put("GET /api/v1/courses/search", "搜索课程");
        courseApis.put("GET /api/v1/courses/recommended", "推荐课程");
        courseApis.put("GET /api/v1/courses/popular", "热门课程");
        courseApis.put("GET /api/v1/courses/categories", "课程分类");
        data.put("课程管理接口", courseApis);

        // 文件上传接口
        Map<String, String> fileApis = new HashMap<>();
        fileApis.put("POST /api/v1/upload", "通用文件上传");
        fileApis.put("POST /api/v1/upload/course-cover", "课程封面上传");
        fileApis.put("POST /api/v1/upload/course-video", "课程视频上传");
        fileApis.put("GET /api/v1/files/{path}", "文件访问下载");
        data.put("文件管理接口", fileApis);

        // 测试接口
        Map<String, String> testApis = new HashMap<>();
        testApis.put("GET /api/v1/test/hello", "健康检查");
        testApis.put("GET /api/v1/test/db", "数据库连接测试");
        testApis.put("GET /api/v1/test/info", "系统信息");
        testApis.put("GET /api/v1/test/apis", "API列表");
        data.put("测试接口", testApis);

        data.put("totalApis", authApis.size() + courseApis.size() + fileApis.size() + testApis.size());
        data.put("implementationStatus", "Phase 3 课程管理模块已完成");

        return Result.success("API列表获取成功", data);
    }
}