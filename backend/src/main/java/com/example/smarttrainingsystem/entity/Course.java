// 文件路径: backend/src/main/java/com/example/smarttrainingsystem/entity/Course.java
package com.example.smarttrainingsystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 课程实体类
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-07-18
 */
@Data
@Entity
@Table(name = "t_course")
@EqualsAndHashCode(callSuper = true)
public class Course extends BaseEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(length = 36)
    private String id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "instructor_id", nullable = false, length = 36)
    private String instructorId;

    @Column(name = "instructor_name", length = 100)
    private String instructorName;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "status", nullable = false)
    private Integer status = 0; // 0-草稿 1-已发布 2-已下架

    @Column(name = "difficulty_level")
    private Integer difficultyLevel; // 1-初级 2-中级 3-高级

    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // 预计学时（分钟）

    @Column(name = "is_required")
    private Boolean isRequired = false; // 是否必修

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO; // 课程评分

    @Column(name = "rating_count")
    private Integer ratingCount = 0; // 评分人数

    @Column(name = "student_count")
    private Integer studentCount = 0; // 学习人数

    @Column(name = "view_count")
    private Integer viewCount = 0; // 浏览次数

    @Column(name = "publish_time")
    private Long publishTime; // 发布时间（时间戳）

    @Column(name = "learning_objectives", length = 2000)
    private String learningObjectives; // 学习目标

    @Column(name = "prerequisites", length = 2000)
    private String prerequisites; // 前置要求

    @Column(name = "tags", length = 1000)
    private String tags; // 标签，用逗号分隔

    @Column(name = "material_urls", length = 500)
    private String materialUrls; // 学习资料URL，用逗号分隔

    @Column(name = "video_urls", length = 500)
    private String videoUrls; // 视频资料URL，用逗号分隔

    // 一对多关联章节
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<CourseChapter> chapters;

    /**
     * 发布课程
     */
    public void publish() {
        this.status = 1;
        this.publishTime = System.currentTimeMillis();
    }

    /**
     * 下架课程
     */
    public void unpublish() {
        this.status = 2;
    }

    /**
     * 是否已发布
     */
    public boolean isPublished() {
        return this.status != null && this.status == 1;
    }

    /**
     * 是否草稿状态
     */
    public boolean isDraft() {
        return this.status != null && this.status == 0;
    }

    /**
     * 增加学习人数
     */
    public void incrementStudentCount() {
        this.studentCount = (this.studentCount == null ? 0 : this.studentCount) + 1;
    }

    /**
     * 增加浏览次数
     */
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    /**
     * 获取状态文本
     */
    public String getStatusText() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "草稿";
            case 1: return "已发布";
            case 2: return "已下架";
            default: return "未知";
        }
    }

    /**
     * 获取难度文本
     */
    public String getDifficultyText() {
        if (difficultyLevel == null) return "未设置";
        switch (difficultyLevel) {
            case 1: return "初级";
            case 2: return "中级";
            case 3: return "高级";
            default: return "未设置";
        }
    }
}