package com.example.smarttrainingsystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 * 用于管理用户和角色的多对多关系
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Entity
@Table(name = "t_user_role")
@Data
@EqualsAndHashCode(callSuper = false)
public class UserRole {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", length = 36)
    private String id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    /**
     * 角色ID
     */
    @Column(name = "role_id", length = 36, nullable = false)
    private String roleId;

    /**
     * 分配者ID
     */
    @Column(name = "assigned_by", length = 36)
    private String assignedBy;

    /**
     * 分配时间
     */
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
