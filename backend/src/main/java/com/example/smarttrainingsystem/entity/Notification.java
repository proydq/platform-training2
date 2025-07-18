package com.example.smarttrainingsystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 通知实体
 * 用于管理系统通知消息
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Entity
@Table(name = "t_notification")
@Data
@EqualsAndHashCode(callSuper = false)
public class Notification {

    /**
     * 主键ID
     */
    @Id
    @Column(name = "id", length = 36)
    private String id = UUID.randomUUID().toString();

    /**
     * 接收用户ID
     */
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    /**
     * 发送者ID
     */
    @Column(name = "sender_id", length = 36)
    private String senderId;

    /**
     * 通知标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 通知内容
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 通知类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private Type type = Type.SYSTEM;

    /**
     * 通知状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.UNREAD;

    /**
     * 相关业务ID（如考试ID、课程ID等）
     */
    @Column(name = "related_id", length = 36)
    private String relatedId;

    /**
     * 相关业务类型
     */
    @Column(name = "related_type", length = 50)
    private String relatedType;

    /**
     * 阅读时间
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 用户实体关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 发送者实体关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", insertable = false, updatable = false)
    private User sender;

    /**
     * 通知类型枚举
     */
    public enum Type {
        SYSTEM("系统通知"),
        COURSE("课程通知"),
        EXAM("考试通知"),
        REMINDER("提醒通知"),
        ACHIEVEMENT("成就通知");

        private final String description;

        Type(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 通知状态枚举
     */
    public enum Status {
        UNREAD("未读"),
        READ("已读"),
        DELETED("已删除");

        private final String description;

        Status(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.status = Status.READ;
        this.readAt = LocalDateTime.now();
    }

    /**
     * 标记为删除
     */
    public void markAsDeleted() {
        this.status = Status.DELETED;
    }

    /**
     * 检查是否已读
     */
    public boolean isRead() {
        return Status.READ.equals(this.status);
    }

    /**
     * 检查是否已删除
     */
    public boolean isDeleted() {
        return Status.DELETED.equals(this.status);
    }
}