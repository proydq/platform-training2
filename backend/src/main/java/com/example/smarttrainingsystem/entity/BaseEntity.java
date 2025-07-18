// 文件路径: backend/src/main/java/com/example/smarttrainingsystem/entity/BaseEntity.java
package com.example.smarttrainingsystem.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

/**
 * 基础实体类
 * 提供所有实体的公共字段：创建时间、更新时间、创建人、更新人
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-07-18
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /**
     * 创建时间（时间戳，毫秒）
     */
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private Long createTime;

    /**
     * 更新时间（时间戳，毫秒）
     */
    @LastModifiedDate
    @Column(name = "update_time", nullable = false)
    private Long updateTime;

    /**
     * 创建人
     */
    @Column(name = "create_by", length = 100)
    private String createBy;

    /**
     * 更新人
     */
    @Column(name = "update_by", length = 100)
    private String updateBy;

    /**
     * 实体创建前的处理
     */
    @PrePersist
    protected void onCreate() {
        long now = System.currentTimeMillis();
        this.createTime = now;
        this.updateTime = now;
    }

    /**
     * 实体更新前的处理
     */
    @PreUpdate
    protected void onUpdate() {
        this.updateTime = System.currentTimeMillis();
    }
}