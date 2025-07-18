package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据仓储接口
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-01-18
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户信息（可选）
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     *
     * @param email 邮箱
     * @return 用户信息（可选）
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查找用户
     *
     * @param phone 手机号
     * @return 用户信息（可选）
     */
    Optional<User> findByPhone(String phone);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return true-存在，false-不存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return true-存在，false-不存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return true-存在，false-不存在
     */
    boolean existsByPhone(String phone);

    /**
     * 根据用户名和状态查找用户
     *
     * @param username 用户名
     * @param status 用户状态
     * @return 用户信息（可选）
     */
    Optional<User> findByUsernameAndStatus(String username, Integer status);

    /**
     * 根据激活状态查找用户
     *
     * @param active 激活状态
     * @return 用户列表
     */
    List<User> findByActive(Boolean active);

    /**
     * 根据激活状态查找用户（分页）
     *
     * @param active 激活状态
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByActive(Boolean active, Pageable pageable);

    /**
     * 统计激活状态的用户数量
     *
     * @return 用户数量
     */
    long countByActiveTrue();

    /**
     * 统计指定状态的用户数量
     *
     * @param status 用户状态
     * @return 用户数量
     */
    long countByStatus(Integer status);

    /**
     * 根据创建时间之后查找用户数量
     *
     * @param createdAt 创建时间
     * @return 用户数量
     */
    long countByCreatedAtAfter(LocalDateTime createdAt);

    /**
     * 根据部门查找用户列表
     *
     * @param department 部门
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.department = :department AND u.active = true")
    List<User> findActiveUsersByDepartment(@Param("department") String department);

    /**
     * 根据角色代码查找用户列表
     *
     * @param roleCode 角色代码
     * @return 用户列表
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleCode = :roleCode AND u.active = true")
    List<User> findActiveUsersByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 根据真实姓名模糊查询用户
     *
     * @param realName 真实姓名
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.realName LIKE %:realName% AND u.active = true")
    List<User> findActiveUsersByRealNameContaining(@Param("realName") String realName);

    /**
     * 复合条件搜索用户
     *
     * @param keyword 关键词（用户名、昵称、真实姓名、邮箱）
     * @param status 状态筛选
     * @param role 角色筛选
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE " +
            "(:keyword IS NULL OR " +
            " u.username LIKE %:keyword% OR " +
            " u.nickname LIKE %:keyword% OR " +
            " u.realName LIKE %:keyword% OR " +
            " u.email LIKE %:keyword%) AND " +
            "(:status IS NULL OR " +
            " (:status = 'active' AND u.active = true) OR " +
            " (:status = 'inactive' AND u.active = false)) AND " +
            "(:role IS NULL OR r.roleCode = :role)")
    Page<User> searchUsers(@Param("keyword") String keyword,
                           @Param("status") String status,
                           @Param("role") String role,
                           Pageable pageable);

    /**
     * 根据创建者查找用户
     *
     * @param createdBy 创建者ID
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByCreatedBy(String createdBy, Pageable pageable);

    /**
     * 查找最近注册的用户
     *
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :sinceDate ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * 查找最近登录的用户
     *
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :sinceDate ORDER BY u.lastLoginAt DESC")
    List<User> findRecentlyActiveUsers(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * 统计各部门用户数量
     *
     * @return 统计结果
     */
    @Query("SELECT u.department, COUNT(u) FROM User u WHERE u.active = true AND u.department IS NOT NULL GROUP BY u.department")
    List<Object[]> countUsersByDepartment();

    /**
     * 统计各角色用户数量
     *
     * @return 统计结果
     */
    @Query("SELECT r.roleCode, r.roleName, COUNT(u) FROM User u JOIN u.roles r WHERE u.active = true GROUP BY r.roleCode, r.roleName")
    List<Object[]> countUsersByRole();

    /**
     * 根据性别统计用户数量
     *
     * @return 统计结果
     */
    @Query("SELECT u.gender, COUNT(u) FROM User u WHERE u.active = true GROUP BY u.gender")
    List<Object[]> countUsersByGender();
}