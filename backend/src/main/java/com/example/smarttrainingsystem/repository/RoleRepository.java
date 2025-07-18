package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色数据仓储接口
 *
 * @author Smart Training System
 * @version 1.0
 * @since 2025-01-18
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    /**
     * 根据角色代码查找角色
     *
     * @param roleCode 角色代码
     * @return 角色信息（可选）
     */
    Optional<Role> findByRoleCode(String roleCode);

    /**
     * 根据角色名称查找角色
     *
     * @param roleName 角色名称
     * @return 角色信息（可选）
     */
    Optional<Role> findByRoleName(String roleName);

    /**
     * 根据角色名称查找角色 (为了兼容 findByName)
     *
     * @param name 角色名称
     * @return 角色信息（可选）
     */
    default Optional<Role> findByName(String name) {
        return findByRoleCode(name);
    }

    /**
     * 检查角色代码是否存在
     *
     * @param roleCode 角色代码
     * @return true-存在，false-不存在
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * 检查角色名称是否存在
     *
     * @param roleName 角色名称
     * @return true-存在，false-不存在
     */
    boolean existsByRoleName(String roleName);

    /**
     * 根据状态查找角色列表
     *
     * @param status 角色状态
     * @return 角色列表
     */
    List<Role> findByStatus(Integer status);

    /**
     * 查找所有启用的角色
     *
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.status = 1 ORDER BY r.sortOrder ASC, r.createTime ASC")
    List<Role> findAllActiveRoles();

    /**
     * 根据角色代码列表查找角色
     *
     * @param roleCodes 角色代码列表
     * @return 角色列表
     */
    List<Role> findByRoleCodeIn(List<String> roleCodes);

    /**
     * 根据用户ID查找角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r JOIN UserRole ur ON r.id = ur.roleId WHERE ur.userId = :userId")
    List<Role> findRolesByUserId(@Param("userId") String userId);

    /**
     * 模糊查询角色
     *
     * @param keyword 关键词
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.roleName LIKE %:keyword% OR r.roleCode LIKE %:keyword% OR r.description LIKE %:keyword%")
    List<Role> searchRoles(@Param("keyword") String keyword);

    /**
     * 统计角色数量
     *
     * @param status 状态
     * @return 角色数量
     */
    long countByStatus(Integer status);

    /**
     * 获取角色的最大排序号
     *
     * @return 最大排序号
     */
    @Query("SELECT MAX(r.sortOrder) FROM Role r")
    Integer getMaxSortOrder();
}