package com.example.smarttrainingsystem.service;

import com.example.smarttrainingsystem.dto.CourseListItemDTO;
import com.example.smarttrainingsystem.dto.MyCourseDTO;
import com.example.smarttrainingsystem.dto.UserStudyDTO;
import com.example.smarttrainingsystem.entity.Course;
import com.example.smarttrainingsystem.entity.StudyRecord;
import com.example.smarttrainingsystem.repository.CourseRepository;
import com.example.smarttrainingsystem.repository.StudyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserStudyService {

    private final StudyRecordRepository studyRecordRepository;
    private final CourseRepository courseRepository;

    /**
     * 获取学习概览
     */
    public UserStudyDTO.Overview getOverview(String userId) {
        UserStudyDTO.Overview dto = new UserStudyDTO.Overview();
        dto.setCompleted(studyRecordRepository.countByUserIdAndStatus(userId, StudyRecord.Status.COMPLETED));
        dto.setInProgress(studyRecordRepository.countByUserIdAndStatus(userId, StudyRecord.Status.IN_PROGRESS));
        dto.setNotStarted(studyRecordRepository.countByUserIdAndStatus(userId, StudyRecord.Status.NOT_STARTED));
        Long total = studyRecordRepository.sumDurationByUserId(userId);
        dto.setTotalTime(total == null ? 0L : total);
        return dto;
    }

    /**
     * 获取我的课程
     */
    public List<MyCourseDTO> getMyCourses(String userId) {
        List<StudyRecord> records = studyRecordRepository.findByUserId(userId);
        return records.stream().map(r -> {
            Course course = r.getCourse();
            if (course == null) {
                course = courseRepository.findById(r.getCourseId()).orElse(null);
            }
            if (course == null) {
                return null;
            }
            MyCourseDTO item = new MyCourseDTO();
            item.setCourseId(course.getId());
            item.setTitle(course.getTitle());
            item.setCoverUrl(course.getCoverImageUrl());
            item.setDuration(course.getEstimatedDuration());
            item.setProgress(r.getEffectiveProgress());
            item.setStatus(r.getStatus().name());
            item.setCompletedDate(r.getEffectiveCompletionTime());
            return item;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 获取推荐课程
     */
    public List<CourseListItemDTO> getRecommendations(String userId) {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Course> page = courseRepository.findRecommendedCourses(1, pageable);
        return page.getContent().stream().map(c -> {
            CourseListItemDTO item = new CourseListItemDTO();
            item.setCourseId(c.getId());
            item.setTitle(c.getTitle());
            item.setCoverUrl(c.getCoverImageUrl());
            item.setDuration(c.getEstimatedDuration());
            item.setStatus("RECOMMEND");
            item.setProgress(0);
            return item;
        }).collect(Collectors.toList());
    }
}
