package com.example.smarttrainingsystem.dto;

import lombok.Data;

/**
 * 我的课程列表项 DTO
 */
@Data
public class CourseListItemDTO {
    private String courseId;
    private String title;
    private String coverUrl;
    private String status;
    private Integer progress;
    private Integer duration;
}
