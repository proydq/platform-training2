package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知数据仓储接口
 * 提供通知相关的数据访问操作
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    /**
     * 根据用户ID查询通知
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 通知分页数据
     */
    Page<Notification> findByUserId(String userId, Pageable pageable);

    /**
     * 根据用户ID和状态查询通知
     *
     * @param userId 用户ID
     * @param status 通知状态
     * @param pageable 分页参数
     * @return 通知分页数据
     */
    Page<Notification> findByUserIdAndStatus(String userId, Notification.Status status, Pageable pageable);

    /**
     * 根据用户ID和类型查询通知
     *
     * @param userId 用户ID
     * @param type 通知类型
     * @param pageable 分页参数
     * @return 通知分页数据
     */
    Page<Notification> findByUserIdAndType(String userId, Notification.Type type, Pageable pageable);

    /**
     * 根据用户ID查询未读通知
     *
     * @param userId 用户ID
     * @return 未读通知列表
     */
    List<Notification> findByUserIdAndStatus(String userId, Notification.Status status);

    /**
     * 统计用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    long countByUserIdAndStatus(String userId, Notification.Status status);

    /**
     * 根据发送者ID查询通知
     *
     * @param senderId 发送者ID
     * @param pageable 分页参数
     * @return 通知分页数据
     */
    Page<Notification> findBySenderId(String senderId, Pageable pageable);

    /**
     * 根据通知类型统计数量
     *
     * @param type 通知类型
     * @return 通知数量
     */
    long countByType(Notification.Type type);

    /**
     * 根据创建时间范围查询通知
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 通知分页数据
     */
    Page<Notification> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 批量标记为已读
     *
     * @param userId 用户ID
     * @param notificationIds 通知ID列表
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.id IN :notificationIds")
    void markAsReadBatch(@Param("userId") String userId, @Param("notificationIds") List<String> notificationIds);

    /**
     * 批量删除通知
     *
     * @param userId 用户ID
     * @param notificationIds 通知ID列表
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'deleted' WHERE n.userId = :userId AND n.id IN :notificationIds")
    void markAsDeletedBatch(@Param("userId") String userId, @Param("notificationIds") List<String> notificationIds);

    /**
     * 删除指定时间之前的已读通知
     *
     * @param beforeTime 指定时间
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.status = 'read' AND n.readAt < :beforeTime")
    void deleteReadNotificationsBefore(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 查询用户最近的通知
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 最近通知列表
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.status != 'deleted' ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("userId") String userId, @Param("limit") int limit);

    /**
     * 复合条件查询通知
     *
     * @param userId 用户ID
     * @param type 通知类型
     * @param status 通知状态
     * @param keyword 关键词搜索
     * @param pageable 分页参数
     * @return 通知分页数据
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
           "AND (:type IS NULL OR n.type = :type) " +
           "AND (:status IS NULL OR n.status = :status) " +
           "AND (:keyword IS NULL OR n.title LIKE %:keyword% OR n.content LIKE %:keyword%) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> searchNotifications(@Param("userId") String userId,
                                          @Param("type") Notification.Type type,
                                          @Param("status") Notification.Status status,
                                          @Param("keyword") String keyword,
                                          Pageable pageable);

    /**
     * 获取通知统计信息
     *
     * @param userId 用户ID
     * @return 统计信息数组 [总数, 未读数, 已读数]
     */
    @Query("SELECT " +
           "COUNT(*) as total, " +
           "SUM(CASE WHEN n.status = 'unread' THEN 1 ELSE 0 END) as unread, " +
           "SUM(CASE WHEN n.status = 'read' THEN 1 ELSE 0 END) as read " +
           "FROM Notification n WHERE n.userId = :userId AND n.status != 'deleted'")
    Object[] getNotificationStatistics(@Param("userId") String userId);
}