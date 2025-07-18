package com.example.smarttrainingsystem.controller;

import com.example.smarttrainingsystem.common.Result;
import com.example.smarttrainingsystem.dto.StudentDTO;
import com.example.smarttrainingsystem.service.StudentManagementService;
import com.example.smarttrainingsystem.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 学员管理控制器
 * 提供学员管理相关的RESTful API接口
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@RestController
@RequestMapping("/api/v1/students")
@Slf4j
public class StudentManagementController {

    @Autowired
    private StudentManagementService studentManagementService;

    /**
     * 获取学员列表
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sort 排序字段
     * @param order 排序方向（asc/desc）
     * @param keyword 搜索关键词
     * @param status 学员状态
     * @param role 角色筛选
     * @return 学员分页列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<Page<StudentDTO.ListItem>> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role) {

        try {
            // 构建分页参数
            Sort.Direction direction = "desc".equalsIgnoreCase(order) ?
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

            Page<StudentDTO.ListItem> result = studentManagementService.getStudents(
                    keyword, status, role, pageable);

            return Result.success("获取学员列表成功", result);
        } catch (Exception e) {
            log.error("获取学员列表失败", e);
            return Result.error("获取学员列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取学员详情
     *
     * @param studentId 学员ID
     * @return 学员详情
     */
    @GetMapping("/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<StudentDTO.Detail> getStudentDetail(@PathVariable String studentId) {
        try {
            StudentDTO.Detail detail = studentManagementService.getStudentDetail(studentId);
            return Result.success("获取学员详情成功", detail);
        } catch (Exception e) {
            log.error("获取学员详情失败", e);
            return Result.error("获取学员详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建学员
     *
     * @param request 创建请求
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<StudentDTO.Detail> createStudent(@Valid @RequestBody StudentDTO.CreateRequest request) {
        try {
            String createdBy = SecurityUtils.getCurrentUserId();
            StudentDTO.Detail student = studentManagementService.createStudent(request, createdBy);
            return Result.success("创建学员成功", student);
        } catch (Exception e) {
            log.error("创建学员失败", e);
            return Result.error("创建学员失败: " + e.getMessage());
        }
    }

    /**
     * 更新学员信息
     *
     * @param studentId 学员ID
     * @param request 更新请求
     * @return 更新结果
     */
    @PutMapping("/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<StudentDTO.Detail> updateStudent(@PathVariable String studentId,
                                                   @Valid @RequestBody StudentDTO.UpdateRequest request) {
        try {
            StudentDTO.Detail student = studentManagementService.updateStudent(studentId, request);
            return Result.success("更新学员信息成功", student);
        } catch (Exception e) {
            log.error("更新学员信息失败", e);
            return Result.error("更新学员信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除学员
     *
     * @param studentId 学员ID
     * @return 删除结果
     */
    @DeleteMapping("/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteStudent(@PathVariable String studentId) {
        try {
            studentManagementService.deleteStudent(studentId);
            return Result.success("删除学员成功");
        } catch (Exception e) {
            log.error("删除学员失败", e);
            return Result.error("删除学员失败: " + e.getMessage());
        }
    }

    /**
     * 批量操作学员
     *
     * @param request 批量操作请求
     * @return 操作结果
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> batchOperation(@Valid @RequestBody StudentDTO.BatchOperationRequest request) {
        try {
            Map<String, Object> result = studentManagementService.batchOperation(request);
            return Result.success("批量操作成功", result);
        } catch (Exception e) {
            log.error("批量操作失败", e);
            return Result.error("批量操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取学员学习统计
     *
     * @param studentId 学员ID
     * @return 学习统计
     */
    @GetMapping("/{studentId}/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<StudentDTO.StudyStatistics> getStudentStatistics(@PathVariable String studentId) {
        try {
            StudentDTO.StudyStatistics statistics = studentManagementService.getStudentStatistics(studentId);
            return Result.success("获取学员统计成功", statistics);
        } catch (Exception e) {
            log.error("获取学员统计失败", e);
            return Result.error("获取学员统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取学员学习进度
     *
     * @param studentId 学员ID
     * @return 学习进度
     */
    @GetMapping("/{studentId}/progress")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<List<StudentDTO.CourseProgress>> getStudentProgress(@PathVariable String studentId) {
        try {
            List<StudentDTO.CourseProgress> progress = studentManagementService.getStudentProgress(studentId);
            return Result.success("获取学习进度成功", progress);
        } catch (Exception e) {
            log.error("获取学习进度失败", e);
            return Result.error("获取学习进度失败: " + e.getMessage());
        }
    }

    /**
     * 获取学员考试记录
     *
     * @param studentId 学员ID
     * @param page 页码
     * @param size 每页大小
     * @return 考试记录
     */
    @GetMapping("/{studentId}/exams")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<Page<StudentDTO.ExamRecord>> getStudentExams(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "examTime"));
            Page<StudentDTO.ExamRecord> exams = studentManagementService.getStudentExams(studentId, pageable);
            return Result.success("获取考试记录成功", exams);
        } catch (Exception e) {
            log.error("获取考试记录失败", e);
            return Result.error("获取考试记录失败: " + e.getMessage());
        }
    }

    /**
     * 重置学员密码
     *
     * @param studentId 学员ID
     * @param request 重置密码请求
     * @return 重置结果
     */
    @PostMapping("/{studentId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, String>> resetStudentPassword(@PathVariable String studentId,
                                                            @RequestBody Map<String, String> request) {
        try {
            String newPassword = studentManagementService.resetStudentPassword(studentId, request.get("newPassword"));
            Map<String, String> result = Map.of("newPassword", newPassword);
            return Result.success("重置密码成功", result);
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return Result.error("重置密码失败: " + e.getMessage());
        }
    }

    /**
     * 激活/禁用学员账号
     *
     * @param studentId 学员ID
     * @param request 状态更改请求
     * @return 操作结果
     */
    @PostMapping("/{studentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> changeStudentStatus(@PathVariable String studentId,
                                            @RequestBody Map<String, Object> request) {
        try {
            Boolean active = (Boolean) request.get("active");
            String reason = (String) request.get("reason");
            studentManagementService.changeStudentStatus(studentId, active, reason);
            return Result.success(active ? "激活学员成功" : "禁用学员成功");
        } catch (Exception e) {
            log.error("更改学员状态失败", e);
            return Result.error("更改学员状态失败: " + e.getMessage());
        }
    }

    /**
     * 发送通知给学员
     *
     * @param studentId 学员ID
     * @param request 通知请求
     * @return 发送结果
     */
    @PostMapping("/{studentId}/notify")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<Void> sendNotificationToStudent(@PathVariable String studentId,
                                                  @RequestBody StudentDTO.NotificationRequest request) {
        try {
            String senderId = SecurityUtils.getCurrentUserId();
            studentManagementService.sendNotificationToStudent(studentId, request, senderId);
            return Result.success("发送通知成功");
        } catch (Exception e) {
            log.error("发送通知失败", e);
            return Result.error("发送通知失败: " + e.getMessage());
        }
    }

    /**
     * 批量发送通知
     *
     * @param request 批量通知请求
     * @return 发送结果
     */
    @PostMapping("/notify-batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<Map<String, Object>> sendBatchNotification(@RequestBody StudentDTO.BatchNotificationRequest request) {
        try {
            String senderId = SecurityUtils.getCurrentUserId();
            Map<String, Object> result = studentManagementService.sendBatchNotification(request, senderId);
            return Result.success("批量发送通知成功", result);
        } catch (Exception e) {
            log.error("批量发送通知失败", e);
            return Result.error("批量发送通知失败: " + e.getMessage());
        }
    }

    /**
     * 导出学员数据
     *
     * @param format 导出格式
     * @param keyword 搜索关键词
     * @param status 状态筛选
     * @param role 角色筛选
     * @return 导出文件信息
     */
    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> exportStudents(@RequestParam(defaultValue = "excel") String format,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String status,
                                                      @RequestParam(required = false) String role) {
        try {
            String fileName = studentManagementService.exportStudents(format, keyword, status, role);
            Map<String, Object> result = Map.of(
                    "fileName", fileName,
                    "downloadUrl", "/api/v1/files/download/" + fileName
            );
            return Result.success("导出学员数据成功", result);
        } catch (Exception e) {
            log.error("导出学员数据失败", e);
            return Result.error("导出失败: " + e.getMessage());
        }
    }

    /**
     * 导入学员数据
     *
     * @param request 导入请求
     * @return 导入结果
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> importStudents(@RequestBody StudentDTO.ImportRequest request) {
        try {
            String createdBy = SecurityUtils.getCurrentUserId();
            Map<String, Object> result = studentManagementService.importStudents(request, createdBy);
            return Result.success("导入学员数据成功", result);
        } catch (Exception e) {
            log.error("导入学员数据失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 获取学员统计概览
     *
     * @return 统计概览
     */
    @GetMapping("/statistics/overview")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<StudentDTO.OverviewStatistics> getStudentsOverview() {
        try {
            StudentDTO.OverviewStatistics overview = studentManagementService.getStudentsOverview();
            return Result.success("获取统计概览成功", overview);
        } catch (Exception e) {
            log.error("获取统计概览失败", e);
            return Result.error("获取统计概览失败: " + e.getMessage());
        }
    }

    /**
     * 获取学员活跃度报告
     *
     * @param days 统计天数
     * @return 活跃度报告
     */
    @GetMapping("/statistics/activity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Result<List<StudentDTO.ActivityReport>> getStudentsActivity(@RequestParam(defaultValue = "30") int days) {
        try {
            List<StudentDTO.ActivityReport> activity = studentManagementService.getStudentsActivity(days);
            return Result.success("获取活跃度报告成功", activity);
        } catch (Exception e) {
            log.error("获取活跃度报告失败", e);
            return Result.error("获取活跃度报告失败: " + e.getMessage());
        }
    }
}