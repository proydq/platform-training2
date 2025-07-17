package com.example.smarttrainingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 智能培训系统启动类
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-01-18
 */
@SpringBootApplication
@RestController
public class SmartTrainingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartTrainingSystemApplication.class, args);
        System.out.println("=================================");
        System.out.println("🎓 智能培训系统启动成功！");
        System.out.println("📍 访问地址: http://localhost:8080");
        System.out.println("📍 测试接口: http://localhost:8080/health");
        System.out.println("=================================");
    }

    /**
     * 健康检查接口
     * 用于验证后端服务是否正常运行
     */
    @GetMapping("/health")
    public String health() {
        return "智能培训系统后端服务运行正常！当前时间: " +
                java.time.LocalDateTime.now().toString();
    }

    /**
     * 欢迎接口
     * 用于前后端通信测试
     */
    @GetMapping("/")
    public String welcome() {
        return "🎓 欢迎使用智能培训系统！";
    }
}