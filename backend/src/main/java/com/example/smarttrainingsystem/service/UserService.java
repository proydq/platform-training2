package com.example.smarttrainingsystem.service;

import com.example.smarttrainingsystem.dto.UserDTO;
import com.example.smarttrainingsystem.entity.Role;
import com.example.smarttrainingsystem.entity.User;
import com.example.smarttrainingsystem.exception.BusinessException;
import com.example.smarttrainingsystem.repository.RoleRepository;
import com.example.smarttrainingsystem.repository.UserRepository;
import com.example.smarttrainingsystem.repository.UserRoleRepository;
import com.example.smarttrainingsystem.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户
     */
    @Transactional(readOnly = true)
    public Page<UserDTO.ListItem> getUsers(Integer page, Integer pageSize, String role, String keyword) {
        int p = page != null && page > 0 ? page - 1 : 0;
        int size = pageSize != null && pageSize > 0 ? pageSize : 10;
        Pageable pageable = PageRequest.of(p, size);

        Page<User> result = userRepository.searchUsers(
                StringUtils.hasText(keyword) ? keyword : null,
                null,
                StringUtils.hasText(role) ? role : null,
                pageable);

        return result.map(this::convertToListItem);
    }

    /**
     * 创建用户
     */
    @Transactional
    public UserDTO.ListItem createUser(UserDTO.CreateRequest request) {
        log.info("创建用户: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(1009, "用户名已存在");
        }
        if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(1010, "邮箱已存在");
        }

        Role role = roleRepository.findByRoleCode(request.getRole())
                .orElseThrow(() -> new BusinessException(1011, "角色不存在"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setDepartment(request.getDepartment());
        user.setActive("active".equalsIgnoreCase(request.getStatus()));

        User saved = userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setId(UUID.randomUUID().toString());
        userRole.setUserId(saved.getId());
        userRole.setRoleId(role.getId());
        userRole.setAssignedAt(LocalDateTime.now());
        userRoleRepository.save(userRole);

        log.debug("保存用户ID: {}", saved.getId());
        return convertToListItem(saved);
    }

    /**
     * 更新用户
     */
    @Transactional
    public UserDTO.ListItem updateUser(String userId, UserDTO.CreateRequest request) {
        log.info("更新用户: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(1012, "用户不存在"));

        // 先清除旧的角色绑定，避免插入时因主键重复导致报错
        userRoleRepository.deleteByUserId(userId);
        // 立即刷新删除操作，确保中间表记录已移除
        userRoleRepository.flush();

        Role role = roleRepository.findByRoleCode(request.getRole())
                .orElseThrow(() -> new BusinessException(1011, "角色不存在"));

        user.setUsername(request.getUsername());
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setRealName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setDepartment(request.getDepartment());
        user.setActive("active".equalsIgnoreCase(request.getStatus()));

        User saved = userRepository.save(user);
        UserRole userRole = new UserRole();
        userRole.setId(UUID.randomUUID().toString());
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        userRole.setAssignedAt(LocalDateTime.now());
        userRoleRepository.save(userRole);

        return convertToListItem(saved);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(String userId) {
        log.info("删除用户: {}", userId);
        userRoleRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }

    @Transactional
    public void resetPassword(String userId) {
        log.info("重置用户密码: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);
    }

    private UserDTO.ListItem convertToListItem(User user) {
        UserDTO.ListItem dto = new UserDTO.ListItem();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getRealName() != null ? user.getRealName() : user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setStatus(Boolean.TRUE.equals(user.getActive()) ? "active" : "inactive");
        dto.setDepartment(user.getDepartment());
        dto.setPhone(user.getPhone());
        dto.setLastLogin(user.getEffectiveLastLoginTime());
        roleRepository.findRolesByUserId(user.getId()).stream().findFirst()
                .ifPresent(r -> dto.setRole(r.getRoleCode()));
        return dto;
    }
}
