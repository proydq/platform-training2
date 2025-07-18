package com.example.smarttrainingsystem.service;

import com.example.smarttrainingsystem.dto.StudentDTO;
import com.example.smarttrainingsystem.entity.*;
import com.example.smarttrainingsystem.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学员管理服务类
 * 提供学员管理相关的业务逻辑
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Service
@Slf4j
public class StudentManagementService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private StudyRecordRepository studyRecordRepository;

    @Autowired
    private ExamResultRepository examResultRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取学员列表
     *
     * @param keyword 搜索关键词
     * @param status 状态筛选
     * @param role 角色筛选
     * @param pageable 分页参数
     * @return 学员分页列表
     */
    public Page<StudentDTO.ListItem> getStudents(String keyword, String status, String role, Pageable pageable) {
        log.info("获取学员列表 - 关键词: {}, 状态: {}, 角色: {}", keyword, status, role);

        // 查询学员用户
        Page<User> users = userRepository.searchUsers(keyword, status, role, pageable);

        // 转换为DTO
        List<StudentDTO.ListItem> items = users.getContent().stream()
                .map(this::convertToListItem)
                .collect(Collectors.toList());

        return new PageImpl<>(items, pageable, users.getTotalElements());
    }

    /**
     * 获取学员详情
     *
     * @param studentId 学员ID
     * @return 学员详情
     */
    public StudentDTO.Detail getStudentDetail(String studentId) {
        log.info("获取学员详情 - 学员ID: {}", studentId);

        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学员不存在"));

        return convertToDetail(user);
    }

    /**
     * 创建学员
     *
     * @param request 创建请求
     * @param createdBy 创建者ID
     * @return 学员详情
     */
    @Transactional
    public StudentDTO.Detail createStudent(StudentDTO.CreateRequest request, String createdBy) {
        log.info("创建学员 - 用户名: {}, 创建者: {}", request.getUsername(), createdBy);

        // 检查用户名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (request.getEmail() != null && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("邮箱已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        // 设置性别
        if (request.getGender() != null) {
            user.setGender(User.Gender.valueOf(request.getGender()));
        }

        user.setAvatar(request.getAvatar());
        user.setActive(true);
        user.setCreatedBy(createdBy);

        user = userRepository.save(user);

        // 分配学员角色
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RuntimeException("学员角色不存在"));

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(studentRole.getId());
        userRole.setAssignedBy(createdBy);
        userRole.setAssignedAt(LocalDateTime.now());
        userRoleRepository.save(userRole);

        return convertToDetail(user);
    }

    /**
     * 更新学员信息
     *
     * @param studentId 学员ID
     * @param request 更新请求
     * @return 学员详情
     */
    @Transactional
    public StudentDTO.Detail updateStudent(String studentId, StudentDTO.UpdateRequest request) {
        log.info("更新学员信息 - 学员ID: {}", studentId);

        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学员不存在"));

        // 更新基本信息
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            // 检查邮箱唯一性
            userRepository.findByEmail(request.getEmail())
                    .filter(u -> !u.getId().equals(studentId))
                    .ifPresent(u -> { throw new RuntimeException("邮箱已存在"); });
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            user.setGender(User.Gender.valueOf(request.getGender()));
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        user = userRepository.save(user);
        return convertToDetail(user);
    }

    /**
     * 删除学员
     *
     * @param studentId 学员ID
     */
    @Transactional
    public void deleteStudent(String studentId) {
        log.info("删除学员 - 学员ID: {}", studentId);

        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学员不存在"));

        // 软删除，将状态设为不活跃
        user.setActive(false);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * 批量操作学员
     *
     * @param request 批量操作请求
     * @return 操作结果
     */
    @Transactional
    public Map<String, Object> batchOperation(StudentDTO.BatchOperationRequest request) {
        log.info("批量操作学员 - 操作类型: {}, 学员数量: {}",
                request.getOperation(), request.getStudentIds().size());

        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (String studentId : request.getStudentIds()) {
            try {
                switch (request.getOperation()) {
                    case "activate":
                        changeStudentStatus(studentId, true, "批量激活");
                        break;
                    case "deactivate":
                        changeStudentStatus(studentId, false, "批量禁用");
                        break;
                    case "delete":
                        deleteStudent(studentId);
                        break;
                    case "reset_password":
                        resetStudentPassword(studentId, null);
                        break;
                    default:
                        throw new RuntimeException("不支持的操作类型");
                }
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add(String.format("学员ID %s: %s", studentId, e.getMessage()));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);

        return result;
    }

    /**
     * 获取学员学习统计
     *
     * @param studentId 学员ID
     * @return 学习统计
     */
    public StudentDTO.StudyStatistics getStudentStatistics(String studentId) {
        log.info("获取学员统计 - 学员ID: {}", studentId);

        StudentDTO.StudyStatistics statistics = new StudentDTO.StudyStatistics();

        // 课程统计
        long enrolledCourses = studyRecordRepository.countByUserId(studentId);
        long completedCourses = studyRecordRepository.countByUserIdAndStatus(studentId, StudyRecord.Status.COMPLETED);
        statistics.setEnrolledCourses((int) enrolledCourses);
        statistics.setCompletedCourses((int) completedCourses);

        // 考试统计
        long totalExams = examResultRepository.countByUserId(studentId);
        long passedExams = examResultRepository.countByUserIdAndPassStatus(studentId, ExamResult.PassStatus.PASS);
        statistics.setTotalExams((int) totalExams);
        statistics.setPassedExams((int) passedExams);

        // 学习时长统计 - 进行安全的类型转换
        Long totalStudyMinutesLong = studyRecordRepository.sumStudyTimeByUserId(studentId);
        int totalStudyMinutes = totalStudyMinutesLong != null ?
                (totalStudyMinutesLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : totalStudyMinutesLong.intValue()) : 0;
        statistics.setTotalStudyHours(totalStudyMinutes / 60);

        // 平均分
        Double averageScore = examResultRepository.getAverageScoreByUserId(studentId);
        statistics.setAverageScore(averageScore != null ? averageScore : 0.0);

        // 计算完成率
        if (enrolledCourses > 0) {
            statistics.setCompletionRate((double) completedCourses / enrolledCourses * 100);
        } else {
            statistics.setCompletionRate(0.0);
        }

        // 计算通过率
        if (totalExams > 0) {
            statistics.setPassRate((double) passedExams / totalExams * 100);
        } else {
            statistics.setPassRate(0.0);
        }

        return statistics;
    }

    /**
     * 获取学员学习进度
     *
     * @param studentId 学员ID
     * @return 学习进度列表
     */
    public List<StudentDTO.CourseProgress> getStudentProgress(String studentId) {
        log.info("获取学员学习进度 - 学员ID: {}", studentId);

        List<StudyRecord> records = studyRecordRepository.findByUserId(studentId);

        return records.stream()
                .map(this::convertToCourseProgress)
                .collect(Collectors.toList());
    }

    /**
     * 获取学员考试记录
     *
     * @param studentId 学员ID
     * @param pageable 分页参数
     * @return 考试记录分页
     */
    public Page<StudentDTO.ExamRecord> getStudentExams(String studentId, Pageable pageable) {
        log.info("获取学员考试记录 - 学员ID: {}", studentId);

        Page<ExamResult> examResults = examResultRepository.findByUserId(studentId, pageable);

        List<StudentDTO.ExamRecord> records = examResults.getContent().stream()
                .map(this::convertToExamRecord)
                .collect(Collectors.toList());

        return new PageImpl<>(records, pageable, examResults.getTotalElements());
    }

    /**
     * 重置学员密码
     *
     * @param studentId 学员ID
     * @param newPassword 新密码（为空则生成随机密码）
     * @return 新密码
     */
    @Transactional
    public String resetStudentPassword(String studentId, String newPassword) {
        log.info("重置学员密码 - 学员ID: {}", studentId);

        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学员不存在"));

        // 生成新密码
        if (newPassword == null || newPassword.isEmpty()) {
            newPassword = generateRandomPassword();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return newPassword;
    }

    /**
     * 更改学员状态
     *
     * @param studentId 学员ID
     * @param active 是否激活
     * @param reason 原因
     */
    @Transactional
    public void changeStudentStatus(String studentId, Boolean active, String reason) {
        log.info("更改学员状态 - 学员ID: {}, 激活: {}, 原因: {}", studentId, active, reason);

        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学员不存在"));

        user.setActive(active);
        if (!active) {
            user.setDeletedAt(LocalDateTime.now());
        }
        userRepository.save(user);
    }

    /**
     * 发送通知给学员
     *
     * @param studentId 学员ID
     * @param request 通知请求
     * @param senderId 发送者ID
     */
    @Transactional
    public void sendNotificationToStudent(String studentId, StudentDTO.NotificationRequest request, String senderId) {
        log.info("发送通知给学员 - 学员ID: {}, 发送者: {}", studentId, senderId);

        Notification notification = new Notification();
        notification.setUserId(studentId);
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setType(Notification.Type.valueOf(request.getType()));
        notification.setSenderId(senderId);
        notification.setStatus(Notification.Status.UNREAD);

        notificationRepository.save(notification);
    }

    /**
     * 批量发送通知
     *
     * @param request 批量通知请求
     * @param senderId 发送者ID
     * @return 发送结果
     */
    @Transactional
    public Map<String, Object> sendBatchNotification(StudentDTO.BatchNotificationRequest request, String senderId) {
        log.info("批量发送通知 - 发送者: {}, 接收者数量: {}", senderId, request.getStudentIds().size());

        int successCount = 0;
        List<String> errors = new ArrayList<>();

        for (String studentId : request.getStudentIds()) {
            try {
                StudentDTO.NotificationRequest notificationRequest = new StudentDTO.NotificationRequest();
                notificationRequest.setTitle(request.getTitle());
                notificationRequest.setContent(request.getContent());
                notificationRequest.setType(request.getType());

                sendNotificationToStudent(studentId, notificationRequest, senderId);
                successCount++;
            } catch (Exception e) {
                errors.add(String.format("学员ID %s: %s", studentId, e.getMessage()));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", errors.size());
        result.put("errors", errors);

        return result;
    }

    /**
     * 导出学员数据
     *
     * @param format 导出格式
     * @param keyword 搜索关键词
     * @param status 状态筛选
     * @param role 角色筛选
     * @return 导出文件名
     */
    public String exportStudents(String format, String keyword, String status, String role) {
        log.info("导出学员数据 - 格式: {}, 关键词: {}, 状态: {}, 角色: {}", format, keyword, status, role);

        // 简化实现，实际应该生成文件
        String fileName = String.format("students_%d.%s", System.currentTimeMillis(), format);

        // TODO: 实际的文件生成逻辑
        log.info("学员数据导出完成: {}", fileName);

        return fileName;
    }

    /**
     * 导入学员数据
     *
     * @param request 导入请求
     * @param createdBy 创建者ID
     * @return 导入结果
     */
    @Transactional
    public Map<String, Object> importStudents(StudentDTO.ImportRequest request, String createdBy) {
        log.info("导入学员数据 - 创建者: {}", createdBy);

        // 简化实现，实际应该解析文件
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", 0);
        result.put("successCount", 0);
        result.put("failCount", 0);
        result.put("errors", new ArrayList<>());

        return result;
    }

    /**
     * 获取学员统计概览
     *
     * @return 统计概览
     */
    public StudentDTO.OverviewStatistics getStudentsOverview() {
        log.info("获取学员统计概览");

        StudentDTO.OverviewStatistics overview = new StudentDTO.OverviewStatistics();

        // 总学员数
        overview.setTotalStudents((int) userRepository.countByActiveTrue());

        // 活跃学员数（最近30天有学习记录）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        overview.setActiveStudents((int) studyRecordRepository.countActiveStudents(thirtyDaysAgo));

        // 新注册学员数（最近7天）
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        overview.setNewStudents((int) userRepository.countByCreatedAtAfter(sevenDaysAgo));

        // 完成课程学员数
        overview.setCompletedStudents((int) studyRecordRepository.countStudentsWithCompletedCourses());

        return overview;
    }

    /**
     * 获取学员活跃度报告
     *
     * @param days 统计天数
     * @return 活跃度报告
     */
    public List<StudentDTO.ActivityReport> getStudentsActivity(int days) {
        log.info("获取学员活跃度报告 - 天数: {}", days);

        // 简化实现，实际应该查询真实数据
        List<StudentDTO.ActivityReport> reports = new ArrayList<>();

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        for (int i = 0; i < days; i++) {
            StudentDTO.ActivityReport report = new StudentDTO.ActivityReport();
            report.setDate(startDate.plusDays(i).toLocalDate());
            report.setActiveCount(0);
            report.setStudyHours(0);
            report.setCompletedLessons(0);
            reports.add(report);
        }

        return reports;
    }

    // ============ 私有方法 ============

    private StudentDTO.ListItem convertToListItem(User user) {
        StudentDTO.ListItem item = new StudentDTO.ListItem();
        item.setUserId(user.getId());
        item.setUsername(user.getUsername());
        item.setNickname(user.getNickname());
        item.setEmail(user.getEmail());
        item.setPhone(user.getPhone());
        item.setActive(user.getActive());
        item.setCreatedAt(user.getCreatedAt());
        item.setLastLoginAt(user.getEffectiveLastLoginTime());

        // 获取学习统计
        item.setEnrolledCourses((int) studyRecordRepository.countByUserId(user.getId()));
        item.setCompletedCourses((int) studyRecordRepository.countByUserIdAndStatus(
                user.getId(), StudyRecord.Status.COMPLETED));

        return item;
    }

    private StudentDTO.Detail convertToDetail(User user) {
        StudentDTO.Detail detail = new StudentDTO.Detail();
        detail.setUserId(user.getId());
        detail.setUsername(user.getUsername());
        detail.setNickname(user.getNickname());
        detail.setEmail(user.getEmail());
        detail.setPhone(user.getPhone());
        detail.setGender(user.getGender() != null ? user.getGender().name() : null);
        detail.setAvatar(user.getEffectiveAvatar());
        detail.setActive(user.getActive());
        detail.setCreatedAt(user.getCreatedAt());
        detail.setUpdatedAt(user.getUpdatedAt());
        detail.setLastLoginAt(user.getEffectiveLastLoginTime());

        // 获取详细统计
        detail.setStatistics(getStudentStatistics(user.getId()));

        return detail;
    }

    private StudentDTO.CourseProgress convertToCourseProgress(StudyRecord record) {
        StudentDTO.CourseProgress progress = new StudentDTO.CourseProgress();
        progress.setCourseId(record.getCourseId());
        progress.setProgress(record.getProgress());
        progress.setStatus(record.getStatus().name());
        progress.setStudyTime(record.getStudyTime());
        progress.setLastStudyAt(record.getLastStudyAt());

        // 获取课程信息
        courseRepository.findById(record.getCourseId())
                .ifPresent(course -> {
                    progress.setCourseTitle(course.getTitle());
                    progress.setCourseCover(course.getCoverImageUrl());
                });

        return progress;
    }

    private StudentDTO.ExamRecord convertToExamRecord(ExamResult result) {
        StudentDTO.ExamRecord record = new StudentDTO.ExamRecord();
        record.setExamId(result.getExamId());
        record.setScore(result.getScore());
        record.setTotalScore(result.getTotalScore());
        record.setPassStatus(result.getPassStatus().name());
        record.setDuration(result.getDuration());
        record.setExamTime(result.getStartTime());

        return record;
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }
}