package com.example.smarttrainingsystem.repository;

import com.example.smarttrainingsystem.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色数据仓储接口
 * 
 * @author 开发者
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
     * 查找所有启用状态的角色
     * 
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.status = 1 ORDER BY r.sortOrder ASC, r.createTime ASC")
    List<Role> findAllActiveRoles();

    /**
     * 根据状态查找角色列表
     * 
     * @param status 角色状态
     * @return 角色列表
     */
    List<Role> findByStatusOrderBySortOrderAscCreateTimeAsc(Integer status);

    /**
     * 统计启用状态的角色数量
     * 
     * @return 角色数量
     */
    @Query("SELECT COUNT(r) FROM Role r WHERE r.status = 1")
    Long countActiveRoles();

    /**
     * 根据角色代码列表查找角色
     * 
     * @param roleCodes 角色代码列表
     * @return 角色列表
     */
    List<Role> findByRoleCodeIn(List<String> roleCodes);

    /**
     * 根据角色名称模糊查询
     * 
     * @param roleName 角色名称关键字
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.roleName LIKE %:roleName% AND r.status = 1 ORDER BY r.sortOrder ASC")
    List<Role> findActiveRolesByNameContaining(String roleName);
}