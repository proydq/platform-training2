package com.example.smarttrainingsystem.service;

import com.example.smarttrainingsystem.dto.UserDTO;
import com.example.smarttrainingsystem.entity.Role;
import com.example.smarttrainingsystem.entity.User;
import com.example.smarttrainingsystem.exception.BusinessException;
import com.example.smarttrainingsystem.repository.RoleRepository;
import com.example.smarttrainingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
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

        user.setRoles(new java.util.HashSet<>(java.util.Collections.singletonList(role)));

        User saved = userRepository.save(user);
        return convertToListItem(saved);
    }

    private UserDTO.ListItem convertToListItem(User user) {
        UserDTO.ListItem dto = new UserDTO.ListItem();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getRealName() != null ? user.getRealName() : user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setStatus(Boolean.TRUE.equals(user.getActive()) ? "active" : "inactive");
        dto.setLastLogin(user.getEffectiveLastLoginTime());
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            Role first = user.getRoles().iterator().next();
            dto.setRole(first.getRoleCode());
        }
        return dto;
    }
}
