// 文件路径: backend/src/main/java/com/example/smarttrainingsystem/entity/CourseChapter.java
package com.example.smarttrainingsystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * 课程章节实体类
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-07-18
 */
@Data
@Entity
@Table(name = "t_course_chapter")
@EqualsAndHashCode(callSuper = true)
public class CourseChapter extends BaseEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(length = 36)
    private String id;

    @Column(name = "course_id", nullable = false, length = 36)
    private String courseId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "chapter_type", length = 50)
    private String chapterType; // video, document, audio, quiz

    @Column(name = "content_url", length = 500)
    private String contentUrl;

    @Column(name = "duration")
    private Integer duration; // 时长（分钟）

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder; // 排序序号

    @Column(name = "status", nullable = false)
    private Integer status = 0; // 0-草稿 1-已发布

    @Column(name = "is_free")
    private Boolean isFree = false; // 是否免费

    @Column(name = "requirements", length = 2000)
    private String requirements; // 学习要求

    @Column(name = "learning_objectives", length = 2000)
    private String learningObjectives; // 学习目标

    @Column(name = "file_size")
    private Long fileSize; // 文件大小（字节）

    @Column(name = "file_format", length = 20)
    private String fileFormat; // 文件格式

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl; // 缩略图URL

    @Column(name = "material_urls", length = 500)
    private String materialUrls; // 章节关联的学习资料

    @Column(name = "video_urls", length = 500)
    private String videoUrls; // 章节关联的视频资料

    // 多对一关联课程
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    private Course course;

    /**
     * 发布章节
     */
    public void publish() {
        this.status = 1;
    }

    /**
     * 是否已发布
     */
    public boolean isPublished() {
        return this.status != null && this.status == 1;
    }

    /**
     * 获取章节类型文本
     */
    public String getChapterTypeText() {
        if (chapterType == null) return "未知";
        switch (chapterType.toLowerCase()) {
            case "video": return "视频";
            case "document": return "文档";
            case "audio": return "音频";
            case "quiz": return "测验";
            default: return "未知";
        }
    }

    /**
     * 获取状态文本
     */
    public String getStatusText() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "草稿";
            case 1: return "已发布";
            default: return "未知";
        }
    }
}