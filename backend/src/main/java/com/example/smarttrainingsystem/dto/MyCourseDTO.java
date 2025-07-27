package com.example.smarttrainingsystem.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 我的课程 DTO
 */
@Data
public class MyCourseDTO {
    private String courseId;
    private String title;
    private String coverUrl;
    private Integer duration;
    private Integer progress;
    private String status;
    private LocalDateTime completedDate;
}
