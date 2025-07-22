// 文件路径: backend/src/main/java/com/example/smarttrainingsystem/service/CourseChapterService.java
package com.example.smarttrainingsystem.service;

import com.example.smarttrainingsystem.dto.CourseChapterDTO;
import com.example.smarttrainingsystem.entity.Course;
import com.example.smarttrainingsystem.entity.CourseChapter;
import com.example.smarttrainingsystem.exception.BusinessException;
import com.example.smarttrainingsystem.repository.CourseRepository;
import com.example.smarttrainingsystem.repository.CourseChapterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程章节业务服务类
 * 
 * @author 开发者
 * @version 1.0
 * @since 2025-07-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseChapterService {

    private final CourseChapterRepository chapterRepository;
    private final CourseRepository courseRepository;

    /**
     * 创建章节
     */
    @Transactional
    public CourseChapterDTO.Response createChapter(CourseChapterDTO.CreateRequest request) {
        log.info("创建章节: courseId={}, title={}", request.getCourseId(), request.getTitle());
        
        // 验证课程是否存在
        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new BusinessException(2002, "课程不存在"));
        
        // 验证章节顺序是否重复
        if (request.getSortOrder() != null) {
            List<CourseChapter> existingChapters = chapterRepository
                .findByCourseIdAndSortOrderGreaterThanEqualOrderBySortOrderAsc(
                    request.getCourseId(), request.getSortOrder());
            
            if (!existingChapters.isEmpty() && 
                existingChapters.get(0).getSortOrder().equals(request.getSortOrder())) {
                // 如果顺序重复，需要调整后续章节的顺序
                adjustChapterOrder(request.getCourseId(), request.getSortOrder(), true);
            }
        } else {
            // 如果没有指定顺序，自动设置为最后一个
            Integer maxOrder = chapterRepository.findMaxSortOrderByCourseId(request.getCourseId());
            request.setSortOrder(maxOrder == null ? 1 : maxOrder + 1);
        }
        
        // 创建章节实体
        CourseChapter chapter = new CourseChapter();
        BeanUtils.copyProperties(request, chapter);
        
        // 保存章节
        chapter = chapterRepository.save(chapter);
        
        log.info("章节创建成功: chapterId={}", chapter.getId());
        return convertToResponse(chapter);
    }

    /**
     * 更新章节
     */
    @Transactional
    public CourseChapterDTO.Response updateChapter(String chapterId, CourseChapterDTO.UpdateRequest request) {
        log.info("更新章节: chapterId={}", chapterId);
        
        CourseChapter chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new BusinessException(2005, "章节不存在"));
        
        // 更新章节信息（只更新非空字段）
        if (StringUtils.hasText(request.getTitle())) {
            chapter.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            chapter.setDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getChapterType())) {
            chapter.setChapterType(request.getChapterType());
        }
        if (request.getContentUrl() != null) {
            chapter.setContentUrl(request.getContentUrl());
        }
        if (request.getDuration() != null) {
            chapter.setDuration(request.getDuration());
        }
        if (request.getSortOrder() != null && !request.getSortOrder().equals(chapter.getSortOrder())) {
            // 如果修改了排序，需要调整其他章节
            updateChapterSortOrder(chapter, request.getSortOrder());
        }
        if (request.getIsFree() != null) {
            chapter.setIsFree(request.getIsFree());
        }
        if (request.getStatus() != null) {
            chapter.setStatus(request.getStatus());
        }
        if (request.getRequirements() != null) {
            chapter.setRequirements(request.getRequirements());
        }
        if (request.getLearningObjectives() != null) {
            chapter.setLearningObjectives(request.getLearningObjectives());
        }
        if (request.getFileSize() != null) {
            chapter.setFileSize(request.getFileSize());
        }
        if (request.getFileFormat() != null) {
            chapter.setFileFormat(request.getFileFormat());
        }
        if (request.getThumbnailUrl() != null) {
            chapter.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getMaterialUrls() != null) {
            chapter.setMaterialUrls(request.getMaterialUrls());
        }
        if (request.getVideoUrls() != null) {
            chapter.setVideoUrls(request.getVideoUrls());
        }
        
        chapterRepository.save(chapter);
        
        log.info("章节更新成功: chapterId={}", chapterId);
        return convertToResponse(chapter);
    }

    /**
     * 删除章节
     */
    @Transactional
    public void deleteChapter(String chapterId) {
        log.info("删除章节: chapterId={}", chapterId);
        
        CourseChapter chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new BusinessException(2005, "章节不存在"));
        
        String courseId = chapter.getCourseId();
        Integer sortOrder = chapter.getSortOrder();
        
        // 删除章节
        chapterRepository.delete(chapter);
        
        // 调整后续章节的排序
        adjustChapterOrder(courseId, sortOrder, false);
        
        log.info("章节删除成功: chapterId={}", chapterId);
    }

    /**
     * 获取课程章节列表
     */
    public List<CourseChapterDTO.Response> getCourseChapters(String courseId, Integer status) {
        log.info("获取课程章节列表: courseId={}, status={}", courseId, status);
        
        List<CourseChapter> chapters;
        if (status != null) {
            chapters = chapterRepository.findByCourseIdAndStatusOrderBySortOrderAsc(courseId, status);
        } else {
            chapters = chapterRepository.findByCourseIdOrderBySortOrderAsc(courseId);
        }
        
        return chapters.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * 获取章节详情
     */
    public CourseChapterDTO.Response getChapterDetail(String chapterId) {
        log.info("获取章节详情: chapterId={}", chapterId);
        
        CourseChapter chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new BusinessException(2005, "章节不存在"));
        
        CourseChapterDTO.Response response = convertToResponse(chapter);
        
        // 设置课程标题
        courseRepository.findById(chapter.getCourseId())
            .ifPresent(course -> response.setCourseTitle(course.getTitle()));
        
        return response;
    }

    /**
     * 批量调整章节顺序
     */
    @Transactional
    public void batchSortChapters(CourseChapterDTO.BatchSortRequest request) {
        log.info("批量调整章节顺序: courseId={}", request.getCourseId());
        
        // 验证课程是否存在
        courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new BusinessException(2002, "课程不存在"));
        
        // 验证所有章节都属于该课程
        for (CourseChapterDTO.SortRequest sortRequest : request.getChapterSorts()) {
            CourseChapter chapter = chapterRepository.findById(sortRequest.getChapterId())
                .orElseThrow(() -> new BusinessException(2005, "章节不存在: " + sortRequest.getChapterId()));
            
            if (!chapter.getCourseId().equals(request.getCourseId())) {
                throw new BusinessException(2006, "章节不属于指定课程");
            }
        }
        
        // 批量更新排序
        for (CourseChapterDTO.SortRequest sortRequest : request.getChapterSorts()) {
            chapterRepository.updateSortOrder(sortRequest.getChapterId(), sortRequest.getNewSortOrder());
        }
        
        log.info("章节顺序调整完成");
    }

    /**
     * 发布章节
     */
    @Transactional
    public CourseChapterDTO.Response publishChapter(String chapterId) {
        log.info("发布章节: chapterId={}", chapterId);
        
        CourseChapter chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new BusinessException(2005, "章节不存在"));
        
        // 验证章节是否可以发布
        validateChapterForPublish(chapter);
        
        // 发布章节
        chapter.publish();
        chapterRepository.save(chapter);
        
        log.info("章节发布成功: chapterId={}", chapterId);
        return convertToResponse(chapter);
    }

    // ==================== 私有方法 ====================

    /**
     * 验证章节是否可以发布
     */
    private void validateChapterForPublish(CourseChapter chapter) {
        if (!StringUtils.hasText(chapter.getTitle())) {
            throw new BusinessException(2007, "章节标题不能为空");
        }
        if (!StringUtils.hasText(chapter.getChapterType())) {
            throw new BusinessException(2007, "章节类型不能为空");
        }
        if (chapter.getDuration() == null || chapter.getDuration() <= 0) {
            throw new BusinessException(2007, "章节时长必须大于0");
        }
    }

    /**
     * 调整章节排序
     */
    private void adjustChapterOrder(String courseId, Integer fromOrder, boolean increment) {
        List<CourseChapter> chapters = chapterRepository
            .findByCourseIdAndSortOrderGreaterThanEqualOrderBySortOrderAsc(courseId, fromOrder);
        
        for (CourseChapter chapter : chapters) {
            if (increment) {
                chapter.setSortOrder(chapter.getSortOrder() + 1);
            } else {
                chapter.setSortOrder(chapter.getSortOrder() - 1);
            }
            chapterRepository.save(chapter);
        }
    }

    /**
     * 更新章节排序
     */
    private void updateChapterSortOrder(CourseChapter chapter, Integer newSortOrder) {
        Integer oldSortOrder = chapter.getSortOrder();
        String courseId = chapter.getCourseId();
        
        if (newSortOrder > oldSortOrder) {
            // 向后移动，中间的章节向前移动
            List<CourseChapter> chapters = chapterRepository
                .findByCourseIdAndSortOrderGreaterThanEqualOrderBySortOrderAsc(
                    courseId, oldSortOrder + 1);
            
            for (CourseChapter c : chapters) {
                if (c.getSortOrder() <= newSortOrder) {
                    c.setSortOrder(c.getSortOrder() - 1);
                    chapterRepository.save(c);
                }
            }
        } else {
            // 向前移动，中间的章节向后移动
            List<CourseChapter> chapters = chapterRepository
                .findByCourseIdAndSortOrderGreaterThanEqualOrderBySortOrderAsc(
                    courseId, newSortOrder);
            
            for (CourseChapter c : chapters) {
                if (c.getSortOrder() < oldSortOrder) {
                    c.setSortOrder(c.getSortOrder() + 1);
                    chapterRepository.save(c);
                }
            }
        }
        
        // 更新当前章节的排序
        chapter.setSortOrder(newSortOrder);
    }

    /**
     * 转换为响应DTO
     */
    private CourseChapterDTO.Response convertToResponse(CourseChapter chapter) {
        CourseChapterDTO.Response response = new CourseChapterDTO.Response();
        BeanUtils.copyProperties(chapter, response);
        response.setChapterTypeText(chapter.getChapterTypeText());
        response.setStatusText(chapter.getStatusText());
        return response;
    }
}