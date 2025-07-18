package com.example.smarttrainingsystem.service;

import com.example.smarttrainingsystem.dto.StudyRecordDTO;
import com.example.smarttrainingsystem.entity.Course;
import com.example.smarttrainingsystem.entity.StudyRecord;
import com.example.smarttrainingsystem.entity.User;
import com.example.smarttrainingsystem.exception.BusinessException;
import com.example.smarttrainingsystem.repository.CourseRepository;
import com.example.smarttrainingsystem.repository.StudyRecordRepository;
import com.example.smarttrainingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学习记录业务服务
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudyRecordService {

    private final StudyRecordRepository studyRecordRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    /**
     * 创建学习记录
     */
    @Transactional
    public StudyRecordDTO.Response createStudyRecord(String userId, StudyRecordDTO.CreateRequest request) {
        log.info("创建学习记录 - 用户ID: {}, 课程ID: {}", userId, request.getCourseId());

        // 验证用户存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(1001, "用户不存在"));

        // 验证课程存在
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException(2001, "课程不存在"));

        // 检查是否已有学习记录
        studyRecordRepository.findByUserIdAndCourseId(userId, request.getCourseId())
                .ifPresent(record -> {
                    throw new BusinessException(3001, "该课程的学习记录已存在");
                });

        // 创建学习记录
        StudyRecord studyRecord = new StudyRecord();
        studyRecord.setUserId(userId);
        studyRecord.setCourseId(request.getCourseId());
        studyRecord.setChapterId(request.getChapterId());
        studyRecord.setNotes(request.getNotes());
        studyRecord.setStudyStatus(StudyRecord.Status.NOT_STARTED);

        StudyRecord savedRecord = studyRecordRepository.save(studyRecord);

        log.info("学习记录创建成功 - 记录ID: {}", savedRecord.getId());
        return convertToResponse(savedRecord);
    }

    /**
     * 开始学习
     */
    @Transactional
    public StudyRecordDTO.Response startStudy(String userId, String courseId) {
        log.info("开始学习 - 用户ID: {}, 课程ID: {}", userId, courseId);

        StudyRecord studyRecord = studyRecordRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new BusinessException(3002, "学习记录不存在"));

        studyRecord.startStudy();
        StudyRecord savedRecord = studyRecordRepository.save(studyRecord);

        log.info("开始学习成功 - 记录ID: {}", savedRecord.getId());
        return convertToResponse(savedRecord);
    }

    /**
     * 更新学习进度
     */
    @Transactional
    public StudyRecordDTO.Response updateProgress(String userId, String recordId, StudyRecordDTO.UpdateRequest request) {
        log.info("更新学习进度 - 用户ID: {}, 记录ID: {}, 进度: {}%", userId, recordId, request.getProgressPercent());

        StudyRecord studyRecord = studyRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(3002, "学习记录不存在"));

        // 验证用户权限
        if (!studyRecord.getUserId().equals(userId)) {
            throw new BusinessException(3003, "无权限操作此学习记录");
        }

        // 更新学习进度
        if (request.getProgressPercent() != null) {
            studyRecord.updateProgress(request.getProgressPercent(), request.getLastPosition());
        }

        // 更新学习时长
        if (request.getStudyDuration() != null) {
            studyRecord.addStudyDuration(request.getStudyDuration());
        }

        // 更新其他信息
        if (request.getNotes() != null) {
            studyRecord.setNotes(request.getNotes());
        }
        if (request.getRating() != null) {
            studyRecord.setRating(request.getRating());
        }
        if (request.getReview() != null) {
            studyRecord.setReview(request.getReview());
        }
        if (request.getIsFavorited() != null) {
            studyRecord.setIsFavorited(request.getIsFavorited());
        }

        StudyRecord savedRecord = studyRecordRepository.save(studyRecord);

        log.info("学习进度更新成功 - 记录ID: {}, 新进度: {}%", savedRecord.getId(), savedRecord.getProgressPercent());
        return convertToResponse(savedRecord);
    }

    /**
     * 暂停学习
     */
    @Transactional
    public StudyRecordDTO.Response pauseStudy(String userId, String recordId) {
        log.info("暂停学习 - 用户ID: {}, 记录ID: {}", userId, recordId);

        StudyRecord studyRecord = studyRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(3002, "学习记录不存在"));

        if (!studyRecord.getUserId().equals(userId)) {
            throw new BusinessException(3003, "无权限操作此学习记录");
        }

        studyRecord.pauseStudy();
        StudyRecord savedRecord = studyRecordRepository.save(studyRecord);

        log.info("暂停学习成功 - 记录ID: {}", savedRecord.getId());
        return convertToResponse(savedRecord);
    }

    /**
     * 恢复学习
     */
    @Transactional
    public StudyRecordDTO.Response resumeStudy(String userId, String recordId) {
        log.info("恢复学习 - 用户ID: {}, 记录ID: {}", userId, recordId);

        StudyRecord studyRecord = studyRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(3002, "学习记录不存在"));

        if (!studyRecord.getUserId().equals(userId)) {
            throw new BusinessException(3003, "无权限操作此学习记录");
        }

        studyRecord.resumeStudy();
        StudyRecord savedRecord = studyRecordRepository.save(studyRecord);

        log.info("恢复学习成功 - 记录ID: {}", savedRecord.getId());
        return convertToResponse(savedRecord);
    }

    /**
     * 完成学习
     */
    @Transactional
    public StudyRecordDTO.Response completeStudy(String userId, String recordId) {
        log.info("完成学习 - 用户ID: {}, 记录ID: {}", userId, recordId);

        StudyRecord studyRecord = studyRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(3002, "学习记录不存在"));

        if (!studyRecord.getUserId().equals(userId)) {
            throw new BusinessException(3003, "无权限操作此学习记录");
        }

        studyRecord.completeStudy();
        StudyRecord savedRecord = studyRecordRepository.save(studyRecord);

        log.info("完成学习成功 - 记录ID: {}", savedRecord.getId());
        return convertToResponse(savedRecord);
    }

    /**
     * 获取学习记录详情
     */
    public StudyRecordDTO.Response getStudyRecord(String userId, String recordId) {
        log.info("获取学习记录详情 - 用户ID: {}, 记录ID: {}", userId, recordId);

        StudyRecord studyRecord = studyRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(3002, "学习记录不存在"));

        if (!studyRecord.getUserId().equals(userId)) {
            throw new BusinessException(3003, "无权限查看此学习记录");
        }

        return convertToResponse(studyRecord);
    }

    /**
     * 根据课程ID获取学习记录
     */
    public StudyRecordDTO.Response getStudyRecordByCourse(String userId, String courseId) {
        log.info("根据课程ID获取学习记录 - 用户ID: {}, 课程ID: {}", userId, courseId);

        StudyRecord studyRecord = studyRecordRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new BusinessException(3002, "学习记录不存在"));

        return convertToResponse(studyRecord);
    }

    /**
     * 搜索学习记录
     */
    public Page<StudyRecordDTO.BriefResponse> searchStudyRecords(String userId, StudyRecordDTO.SearchRequest request) {
        log.info("搜索学习记录 - 用户ID: {}", userId);

        // 构建排序
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(request.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy()
        );

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<StudyRecord> records;

        if (request.getStatus() != null) {
            records = studyRecordRepository.findByUserIdAndStudyStatus(userId, request.getStatus(), pageable);
        } else {
            records = studyRecordRepository.findByUserId(userId, pageable);
        }

        return records.map(this::convertToBriefResponse);
    }

    /**
     * 获取用户正在学习的课程
     */
    public List<StudyRecordDTO.BriefResponse> getInProgressCourses(String userId) {
        log.info("获取正在学习的课程 - 用户ID: {}", userId);

        List<StudyRecord> records = studyRecordRepository.findInProgressByUserId(userId);
        return records.stream()
                .map(this::convertToBriefResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户已完成的课程
     */
    public Page<StudyRecordDTO.BriefResponse> getCompletedCourses(String userId, int page, int size) {
        log.info("获取已完成的课程 - 用户ID: {}", userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<StudyRecord> records = studyRecordRepository.findCompletedByUserId(userId, pageable);

        return records.map(this::convertToBriefResponse);
    }

    /**
     * 获取用户收藏的课程
     */
    public Page<StudyRecordDTO.BriefResponse> getFavoriteCourses(String userId, int page, int size) {
        log.info("获取收藏的课程 - 用户ID: {}", userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<StudyRecord> records = studyRecordRepository.findByUserIdAndIsFavorited(userId, true, pageable);

        return records.map(this::convertToBriefResponse);
    }

    /**
     * 获取用户学习统计
     */
    public StudyRecordDTO.StatisticsResponse getUserStatistics(String userId) {
        log.info("获取用户学习统计 - 用户ID: {}", userId);

        StudyRecordDTO.StatisticsResponse statistics = new StudyRecordDTO.StatisticsResponse();

        // 基础统计
        statistics.setTotalRecords(studyRecordRepository.countByUserId(userId));
        statistics.setCompletedCourses(studyRecordRepository.countCompletedByUserId(userId));
        statistics.setInProgressCourses(studyRecordRepository.countInProgressByUserId(userId));
        statistics.setTotalStudyTime(studyRecordRepository.sumStudyDurationByUserId(userId));

        // 计算完成率
        if (statistics.getTotalRecords() > 0) {
            statistics.setCompletionRate((double) statistics.getCompletedCourses() / statistics.getTotalRecords() * 100);
        } else {
            statistics.setCompletionRate(0.0);
        }

        // 收藏课程数
        statistics.setFavoritedCourses(studyRecordRepository.countByUserIdAndIsFavorited(userId, true));

        // 最后学习时间
        List<StudyRecord> recentRecords = studyRecordRepository.findRecentByUserId(userId, PageRequest.of(0, 1));
        if (!recentRecords.isEmpty()) {
            statistics.setLastStudyTime(recentRecords.get(0).getLastStudyTime());
        }

        // 学习等级
        statistics.setLearningLevel(calculateLearningLevel(statistics.getTotalStudyTime()));

        return statistics;
    }

    /**
     * 获取用户学习趋势
     */
    public List<StudyRecordDTO.StatisticsResponse.TrendData> getUserStudyTrend(String userId, int days) {
        log.info("获取用户学习趋势 - 用户ID: {}, 天数: {}", userId, days);

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Object[]> trendData = studyRecordRepository.getUserStudyTrend(userId, startDate);

        List<StudyRecordDTO.StatisticsResponse.TrendData> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Object[] data : trendData) {
            StudyRecordDTO.StatisticsResponse.TrendData trend = new StudyRecordDTO.StatisticsResponse.TrendData();
            trend.setDate(((LocalDate) data[0]).format(formatter));
            trend.setStudyDuration(((Number) data[2]).intValue());
            trend.setCoursesStudied(((Number) data[1]).intValue());
            result.add(trend);
        }

        return result;
    }

    /**
     * 获取学习排行榜
     */
    public List<StudyRecordDTO.RankingResponse> getStudyTimeRanking(String currentUserId, int limit) {
        log.info("获取学习时长排行榜 - 限制: {}", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> rankingData = studyRecordRepository.getStudyTimeRanking(pageable);

        List<StudyRecordDTO.RankingResponse> result = new ArrayList<>();
        int ranking = 1;

        for (Object[] data : rankingData) {
            StudyRecordDTO.RankingResponse rankingResponse = new StudyRecordDTO.RankingResponse();
            rankingResponse.setRanking(ranking++);
            rankingResponse.setUserId((String) data[0]);
            rankingResponse.setUsername((String) data[1]);
            rankingResponse.setRealName((String) data[2]);
            rankingResponse.setTotalStudyTime(((Number) data[3]).longValue());
            rankingResponse.setIsCurrentUser(currentUserId.equals(data[0]));
            result.add(rankingResponse);
        }

        return result;
    }

    /**
     * 获取完成课程数排行榜
     */
    public List<StudyRecordDTO.RankingResponse> getCompletionRanking(String currentUserId, int limit) {
        log.info("获取完成课程数排行榜 - 限制: {}", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> rankingData = studyRecordRepository.getCompletionRanking(pageable);

        List<StudyRecordDTO.RankingResponse> result = new ArrayList<>();
        int ranking = 1;

        for (Object[] data : rankingData) {
            StudyRecordDTO.RankingResponse rankingResponse = new StudyRecordDTO.RankingResponse();
            rankingResponse.setRanking(ranking++);
            rankingResponse.setUserId((String) data[0]);
            rankingResponse.setUsername((String) data[1]);
            rankingResponse.setRealName((String) data[2]);
            rankingResponse.setCompletedCourses(((Number) data[3]).longValue());
            rankingResponse.setIsCurrentUser(currentUserId.equals(data[0]));
            result.add(rankingResponse);
        }

        return result;
    }

    /**
     * 删除学习记录
     */
    @Transactional
    public void deleteStudyRecord(String userId, String recordId) {
        log.info("删除学习记录 - 用户ID: {}, 记录ID: {}", userId, recordId);

        StudyRecord studyRecord = studyRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(3002, "学习记录不存在"));

        if (!studyRecord.getUserId().equals(userId)) {
            throw new BusinessException(3003, "无权限删除此学习记录");
        }

        studyRecordRepository.delete(studyRecord);
        log.info("学习记录删除成功 - 记录ID: {}", recordId);
    }

    /**
     * 批量操作学习记录
     */
    @Transactional
    public void batchOperateRecords(String userId, StudyRecordDTO.BatchOperationRequest request) {
        log.info("批量操作学习记录 - 用户ID: {}, 操作: {}, 记录数: {}",
                userId, request.getOperation(), request.getRecordIds().length);

        for (String recordId : request.getRecordIds()) {
            StudyRecord studyRecord = studyRecordRepository.findById(recordId)
                    .orElseThrow(() -> new BusinessException(3002, "学习记录不存在: " + recordId));

            if (!studyRecord.getUserId().equals(userId)) {
                throw new BusinessException(3003, "无权限操作此学习记录: " + recordId);
            }

            switch (request.getOperation()) {
                case "delete":
                    studyRecordRepository.delete(studyRecord);
                    break;
                case "favorite":
                    studyRecord.setIsFavorited(true);
                    studyRecordRepository.save(studyRecord);
                    break;
                case "unfavorite":
                    studyRecord.setIsFavorited(false);
                    studyRecordRepository.save(studyRecord);
                    break;
                default:
                    throw new BusinessException(3004, "不支持的操作类型: " + request.getOperation());
            }
        }

        log.info("批量操作完成 - 用户ID: {}", userId);
    }

    /**
     * 转换为响应对象
     */
    private StudyRecordDTO.Response convertToResponse(StudyRecord studyRecord) {
        StudyRecordDTO.Response response = new StudyRecordDTO.Response();
        BeanUtils.copyProperties(studyRecord, response);

        response.setStudyStatusDesc(studyRecord.getStudyStatus().getDescription());

        // 设置课程信息
        if (studyRecord.getCourse() != null) {
            StudyRecordDTO.Response.CourseInfo courseInfo = new StudyRecordDTO.Response.CourseInfo();
            BeanUtils.copyProperties(studyRecord.getCourse(), courseInfo);
            // 修复：设置正确的封面URL字段
            courseInfo.setCoverUrl(studyRecord.getCourse().getCoverImageUrl());
            response.setCourseInfo(courseInfo);
        }

        // 设置章节信息
        if (studyRecord.getChapter() != null) {
            StudyRecordDTO.Response.ChapterInfo chapterInfo = new StudyRecordDTO.Response.ChapterInfo();
            BeanUtils.copyProperties(studyRecord.getChapter(), chapterInfo);
            response.setChapterInfo(chapterInfo);
        }

        return response;
    }

    /**
     * 转换为简要响应对象
     */
    private StudyRecordDTO.BriefResponse convertToBriefResponse(StudyRecord studyRecord) {
        StudyRecordDTO.BriefResponse response = new StudyRecordDTO.BriefResponse();
        BeanUtils.copyProperties(studyRecord, response);

        response.setStudyStatusDesc(studyRecord.getStudyStatus().getDescription());

        // 设置课程信息
        if (studyRecord.getCourse() != null) {
            response.setCourseTitle(studyRecord.getCourse().getTitle());
            response.setCourseCover(studyRecord.getCourse().getCoverImageUrl()); // 修复：使用 getCoverImageUrl()
        }

        return response;
    }

    /**
     * 计算学习等级
     */
    private String calculateLearningLevel(Long totalStudyTime) {
        if (totalStudyTime == null || totalStudyTime == 0) {
            return "新手";
        }

        long hours = totalStudyTime / 60;

        if (hours < 10) {
            return "新手";
        } else if (hours < 50) {
            return "初级";
        } else if (hours < 200) {
            return "中级";
        } else if (hours < 500) {
            return "高级";
        } else {
            return "专家";
        }
    }
}