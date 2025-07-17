package com.example.smarttrainingsystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程实体类
 * 
 * @author 开发者
 * @version 1.0
 * @since 2025-07-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_course")
public class Course {

    /**
     * 课程ID - 使用UUID
     */
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", length = 36)
    private String id;

    /**
     * 课程标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 课程描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 课程封面图片URL
     */
    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    /**
     * 课程分类：技术培训、产品培训、管理培训等
     */
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    /**
     * 课程标签，逗号分隔
     */
    @Column(name = "tags", length = 500)
    private String tags;

    /**
     * 课程状态：0-草稿，1-发布，2-下架
     */
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    /**
     * 课程难度：1-初级，2-中级，3-高级
     */
    @Column(name = "difficulty_level", nullable = false)
    private Integer difficultyLevel = 1;

    /**
     * 预计学习时长（分钟）
     */
    @Column(name = "estimated_duration")
    private Integer estimatedDuration;

    /**
     * 课程评分（1-5星，保留1位小数）
     */
    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating;

    /**
     * 评分人数
     */
    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    /**
     * 学习人数
     */
    @Column(name = "student_count")
    private Integer studentCount = 0;

    /**
     * 讲师ID
     */
    @Column(name = "instructor_id", nullable = false, length = 36)
    private String instructorId;

    /**
     * 讲师姓名（冗余字段，提高查询效率）
     */
    @Column(name = "instructor_name", length = 50)
    private String instructorName;

    /**
     * 课程学习目标
     */
    @Column(name = "learning_objectives", columnDefinition = "TEXT")
    private String learningObjectives;

    /**
     * 课程先决条件
     */
    @Column(name = "prerequisites", columnDefinition = "TEXT")
    private String prerequisites;

    /**
     * 是否为必修课程
     */
    @Column(name = "is_required")
    private Boolean isRequired = false;

    /**
     * 课程有效期（天数，null表示永久有效）
     */
    @Column(name = "validity_days")
    private Integer validityDays;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 发布时间
     */
    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    /**
     * 课程章节 - 一对多关系
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<CourseChapter> chapters;

    /**
     * 自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 自动设置更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 发布课程
     */
    public void publish() {
        this.status = 1;
        this.publishTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 下架课程
     */
    public void unpublish() {
        this.status = 2;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 检查课程是否已发布
     */
    public boolean isPublished() {
        return this.status != null && this.status == 1;
    }
}