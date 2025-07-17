package com.example.smarttrainingsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密配置类
 * 
 * @author 开发者
 * @version 1.0
 * @since 2025-01-18
 */
@Configuration
public class PasswordConfig {

    /**
     * 密码加密器配置
     * 使用BCrypt算法，安全性高，适合生产环境
     * 
     * @return BCrypt密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}