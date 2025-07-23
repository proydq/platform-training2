package com.example.smarttrainingsystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-01-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_user")
public class User {

    /**
     * 用户ID - 使用UUID
     */
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", length = 36)
    private String id;

    /**
     * 用户名 - 登录账号，唯一
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码 - BCrypt加密存储
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * 昵称 - 显示名称
     */
    @Column(name = "nickname", length = 50)
    private String nickname;

    /**
     * 真实姓名
     */
    @Column(name = "real_name", length = 50)
    private String realName;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 手机号
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 性别
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender = Gender.UNKNOWN;

    /**
     * 头像URL
     */
    @Column(name = "avatar", length = 500)
    private String avatar;

    /**
     * 头像URL (兼容旧字段名)
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * 部门
     */
    @Column(name = "department", length = 100)
    private String department;

    /**
     * 职位
     */
    @Column(name = "position", length = 100)
    private String position;

    /**
     * 账号状态：false-禁用，true-启用
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * 账号状态：0-禁用，1-启用 (兼容旧字段)
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 创建者ID
     */
    @Column(name = "created_by", length = 36)
    private String createdBy;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录时间 (兼容旧字段名)
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 删除时间 (软删除)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 创建时间 (兼容旧字段名)
     */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 更新时间 (兼容旧字段名)
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;


    /**
     * 性别枚举
     */
    public enum Gender {
        MALE("男"),
        FEMALE("女"),
        UNKNOWN("未知");

        private final String description;

        Gender(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.createTime == null) {
            this.createTime = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.updateTime == null) {
            this.updateTime = now;
        }
    }

    /**
     * 自动设置更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        LocalDateTime now = LocalDateTime.now();
        this.updatedAt = now;
        this.updateTime = now;
    }

    /**
     * 获取有效的头像URL
     */
    public String getEffectiveAvatar() {
        return avatar != null ? avatar : avatarUrl;
    }

    /**
     * 获取有效的最后登录时间
     */
    public LocalDateTime getEffectiveLastLoginTime() {
        return lastLoginAt != null ? lastLoginAt : lastLoginTime;
    }

    /**
     * 设置最后登录时间 (同时设置两个字段)
     */
    public void setLastLoginTime(LocalDateTime time) {
        this.lastLoginTime = time;
        this.lastLoginAt = time;
    }

    /**
     * 获取账号激活状态
     */
    public Boolean getActive() {
        if (active != null) {
            return active;
        }
        return status != null && status == 1;
    }

    /**
     * 设置账号激活状态 (同时设置两个字段)
     */
    public void setActive(Boolean active) {
        this.active = active;
        this.status = active ? 1 : 0;
    }
}