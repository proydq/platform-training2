package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据仓储接口
 * 
 * @author 开发者
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
     * 根据部门查找用户列表
     * 
     * @param department 部门
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.department = :department AND u.status = 1")
    java.util.List<User> findActiveUsersByDepartment(@Param("department") String department);

    /**
     * 根据角色代码查找用户列表
     * 
     * @param roleCode 角色代码
     * @return 用户列表
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleCode = :roleCode AND u.status = 1")
    java.util.List<User> findActiveUsersByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 统计启用状态的用户数量
     * 
     * @return 用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 1")
    Long countActiveUsers();

    /**
     * 根据真实姓名模糊查询用户
     * 
     * @param realName 真实姓名
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.realName LIKE %:realName% AND u.status = 1")
    java.util.List<User> findActiveUsersByRealNameContaining(@Param("realName") String realName);
}