package com.example.smarttrainingsystem.service;

import com.example.smarttrainingsystem.entity.Role;
import com.example.smarttrainingsystem.entity.User;
import com.example.smarttrainingsystem.repository.RoleRepository;
import com.example.smarttrainingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 数据初始化服务
 * 系统启动时自动创建默认角色和管理员账号
 * 
 * @author 开发者
 * @version 1.0
 * @since 2025-01-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataInitService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("开始初始化系统数据...");
        
        // 初始化角色
        initRoles();
        
        // 初始化管理员账号
        initAdminUser();
        
        log.info("系统数据初始化完成！");
    }

    /**
     * 初始化系统角色
     */
    private void initRoles() {
        log.info("正在初始化系统角色...");
        
        // 创建三个基础角色
        createRoleIfNotExists("ADMIN", "系统管理员", "拥有系统全部权限，可以管理用户、课程、考试等所有功能", 1);
        createRoleIfNotExists("TEACHER", "讲师", "可以管理课程内容、查看学员学习情况、管理考试等教学相关功能", 2);
        createRoleIfNotExists("STUDENT", "学员", "可以学习课程、参加考试、查看个人学习记录等基础学习功能", 3);
        
        log.info("系统角色初始化完成");
    }

    /**
     * 初始化管理员账号
     */
    private void initAdminUser() {
        log.info("正在初始化管理员账号...");
        
        // 检查是否已存在管理员账号
        if (userRepository.existsByUsername("admin")) {
            log.info("管理员账号已存在，跳过创建");
            return;
        }
        
        // 获取管理员角色
        Role adminRole = roleRepository.findByRoleCode("ADMIN")
                .orElseThrow(() -> new RuntimeException("管理员角色不存在"));
        
        // 创建管理员用户
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456")); // 默认密码：123456
        admin.setRealName("系统管理员");
        admin.setEmail("admin@smarttraining.com");
        admin.setDepartment("信息技术部");
        admin.setPosition("系统管理员");
        admin.setStatus(1);
        
        // 设置管理员角色
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        admin.setRoles(roles);
        
        userRepository.save(admin);
        
        log.info("管理员账号创建成功！");
        log.info("登录信息 - 用户名: admin, 密码: 123456");
        
        // 创建测试账号
        createTestUsers();
    }

    /**
     * 创建测试账号
     */
    private void createTestUsers() {
        log.info("正在创建测试账号...");
        
        // 获取角色
        Role teacherRole = roleRepository.findByRoleCode("TEACHER").orElse(null);
        Role studentRole = roleRepository.findByRoleCode("STUDENT").orElse(null);
        
        // 创建测试讲师账号
        if (teacherRole != null && !userRepository.existsByUsername("teacher01")) {
            User teacher = new User();
            teacher.setUsername("teacher01");
            teacher.setPassword(passwordEncoder.encode("123456"));
            teacher.setRealName("张老师");
            teacher.setEmail("teacher01@smarttraining.com");
            teacher.setDepartment("培训部");
            teacher.setPosition("高级讲师");
            teacher.setStatus(1);
            
            Set<Role> teacherRoles = new HashSet<>();
            teacherRoles.add(teacherRole);
            teacher.setRoles(teacherRoles);
            
            userRepository.save(teacher);
            log.info("测试讲师账号创建成功 - 用户名: teacher01, 密码: 123456");
        }
        
        // 创建测试学员账号
        if (studentRole != null && !userRepository.existsByUsername("student01")) {
            User student = new User();
            student.setUsername("student01");
            student.setPassword(passwordEncoder.encode("123456"));
            student.setRealName("李学员");
            student.setEmail("student01@smarttraining.com");
            student.setDepartment("销售部");
            student.setPosition("销售专员");
            student.setStatus(1);
            
            Set<Role> studentRoles = new HashSet<>();
            studentRoles.add(studentRole);
            student.setRoles(studentRoles);
            
            userRepository.save(student);
            log.info("测试学员账号创建成功 - 用户名: student01, 密码: 123456");
        }
    }

    /**
     * 创建角色（如果不存在）
     */
    private void createRoleIfNotExists(String roleCode, String roleName, String description, int sortOrder) {
        if (!roleRepository.existsByRoleCode(roleCode)) {
            Role role = new Role();
            role.setRoleCode(roleCode);
            role.setRoleName(roleName);
            role.setDescription(description);
            role.setSortOrder(sortOrder);
            role.setStatus(1);
            
            roleRepository.save(role);
            log.info("角色创建成功: {} - {}", roleCode, roleName);
        } else {
            log.info("角色已存在: {} - {}", roleCode, roleName);
        }
    }
}