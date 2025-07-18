package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户角色关联数据仓储接口
 * 提供用户角色关联的数据访问操作
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-07-17
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {

    /**
     * 根据用户ID查询角色关联
     *
     * @param userId 用户ID
     * @return 用户角色关联列表
     */
    List<UserRole> findByUserId(String userId);

    /**
     * 根据角色ID查询用户关联
     *
     * @param roleId 角色ID
     * @return 用户角色关联列表
     */
    List<UserRole> findByRoleId(String roleId);

    /**
     * 根据用户ID和角色ID查询关联
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 用户角色关联
     */
    Optional<UserRole> findByUserIdAndRoleId(String userId, String roleId);

    /**
     * 根据用户ID删除所有角色关联
     *
     * @param userId 用户ID
     */
    void deleteByUserId(String userId);

    /**
     * 根据角色ID删除所有用户关联
     *
     * @param roleId 角色ID
     */
    void deleteByRoleId(String roleId);

    /**
     * 检查用户是否有指定角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否存在关联
     */
    boolean existsByUserIdAndRoleId(String userId, String roleId);
}