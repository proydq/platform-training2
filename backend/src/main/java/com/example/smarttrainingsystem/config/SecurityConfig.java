package com.example.smarttrainingsystem.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 修复版安全配置（带JWT验证）
 * 保持宽松权限但启用JWT认证
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-07-17
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF
                .csrf().disable()

                // 配置CORS
                .cors().configurationSource(corsConfigurationSource())

                .and()
                // 配置会话管理
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                // 宽松的权限配置，但会通过JWT过滤器
                .authorizeHttpRequests(authz -> authz
                        // 登录相关接口无需认证
                        .antMatchers("/api/v1/auth/login").permitAll()
                        .antMatchers("/api/v1/auth/check-username").permitAll()
                        .antMatchers("/api/v1/test/**").permitAll()
                        .antMatchers("/api/v1/debug/public").permitAll()
                        .antMatchers("/api/v1/files/**").permitAll()  // 允许文件访问
                        // 其他接口需要认证，但暂时放宽要求
                        .anyRequest().permitAll()  // 暂时允许所有请求，但JWT过滤器仍会处理
                )

                // 添加JWT认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}