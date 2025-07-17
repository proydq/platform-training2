package com.example.smarttrainingsystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 课程章节实体类
 * 
 * @author 开发者
 * @version 1.0
 * @since 2025-07-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_course_chapter")
public class CourseChapter {

    /**
     * 章节ID - 使用UUID
     */
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", length = 36)
    private String id;

    /**
     * 所属课程
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * 课程ID（冗余字段，便于查询）
     */
    @Column(name = "course_id", insertable = false, updatable = false)
    private String courseId;

    /**
     * 章节标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 章节描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 章节类型：video-视频，document-文档，audio-音频，quiz-测验
     */
    @Column(name = "chapter_type", nullable = false, length = 20)
    private String chapterType;

    /**
     * 章节内容URL（视频、文档等文件链接）
     */
    @Column(name = "content_url", length = 500)
    private String contentUrl;

    /**
     * 章节时长（分钟）
     */
    @Column(name = "duration")
    private Integer duration;

    /**
     * 章节排序序号
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    /**
     * 章节状态：0-草稿，1-发布
     */
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    /**
     * 是否为免费章节
     */
    @Column(name = "is_free")
    private Boolean isFree = false;

    /**
     * 章节学习要求（如需要完成前置章节等）
     */
    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    /**
     * 章节学习目标
     */
    @Column(name = "learning_objectives", columnDefinition = "TEXT")
    private String learningObjectives;

    /**
     * 章节文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 章节文件格式
     */
    @Column(name = "file_format", length = 20)
    private String fileFormat;

    /**
     * 缩略图URL
     */
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

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
     * 发布章节
     */
    public void publish() {
        this.status = 1;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 检查章节是否已发布
     */
    public boolean isPublished() {
        return this.status != null && this.status == 1;
    }

    /**
     * 检查是否为视频章节
     */
    public boolean isVideo() {
        return "video".equalsIgnoreCase(this.chapterType);
    }

    /**
     * 检查是否为文档章节
     */
    public boolean isDocument() {
        return "document".equalsIgnoreCase(this.chapterType);
    }
}