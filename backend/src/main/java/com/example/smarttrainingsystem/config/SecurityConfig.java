package com.example.smarttrainingsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 安全配置
 * 
 * @author 开发者
 * @version 1.0
 * @since 2025-01-18
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 安全过滤器链配置
     * 暂时禁用认证，便于开发测试
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（开发阶段）
            .csrf().disable()
            
            // 配置CORS
            .cors().configurationSource(corsConfigurationSource())
            
            .and()
            // 授权配置
            .authorizeHttpRequests(authz -> authz
                // 允许所有请求访问（开发阶段）
                .anyRequest().permitAll()
            );

        return http.build();
    }

    /**
     * CORS跨域配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",  // 前端开发服务器
            "http://127.0.0.1:3000"
        ));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 允许携带凭证
        configuration.setAllowCredentials(true);
        
        // 预检请求的缓存时间
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}