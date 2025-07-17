package com.example.smarttrainingsystem.service;

import com.example.smarttrainingsystem.dto.AuthDTO;
import com.example.smarttrainingsystem.entity.Role;
import com.example.smarttrainingsystem.entity.User;
import com.example.smarttrainingsystem.exception.BusinessException;
import com.example.smarttrainingsystem.repository.UserRepository;
import com.example.smarttrainingsystem.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户认证服务
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-07-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 用户登录认证
     *
     * @param loginRequest 登录请求
     * @return 登录响应信息
     */
    @Transactional
    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest loginRequest) {
        log.info("用户登录请求: {}", loginRequest.getUsername());

        // 1. 查找用户
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BusinessException(1001, "用户名或密码错误"));

        // 2. 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException(1002, "账号已被禁用，请联系管理员");
        }

        // 3. 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("用户{}密码验证失败", loginRequest.getUsername());
            throw new BusinessException(1001, "用户名或密码错误");
        }

        // 4. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        // 5. 生成JWT Token
        List<String> roleCodes = user.getRoles().stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());

        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getId(), roleCodes);

        // 6. 构建响应
        AuthDTO.LoginResponse response = new AuthDTO.LoginResponse();
        response.setAccessToken(accessToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(604800000L); // 7天，与JWT配置保持一致
        response.setUserInfo(buildUserInfo(user));
        response.setAuthorities(roleCodes);

        log.info("用户{}登录成功", user.getUsername());
        return response;
    }

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    public AuthDTO.UserInfo getUserInfo(String username) {
        log.info("开始查询用户信息: {}", username);

        if (username == null || username.trim().isEmpty()) {
            log.error("用户名为空");
            throw new BusinessException(1003, "用户名不能为空");
        }

        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> {
                    log.error("用户不存在: {}", username);
                    return new BusinessException(1003, "用户不存在: " + username);
                });

        log.info("用户查询成功: {} - {}", username, user.getRealName());
        return buildUserInfo(user);
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public AuthDTO.UserInfo getUserInfoById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(1003, "用户不存在"));

        return buildUserInfo(user);
    }

    /**
     * 验证Token并获取用户信息
     *
     * @param token JWT Token
     * @return 用户信息
     */
    public AuthDTO.UserInfo validateTokenAndGetUserInfo(String token) {
        try {
            // 1. 从Token中获取用户名
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null) {
                throw new BusinessException(1004, "Token无效");
            }

            // 2. 验证Token有效性
            if (!jwtUtil.validateToken(token, username)) {
                throw new BusinessException(1005, "Token已过期或无效");
            }

            // 3. 获取用户信息
            return getUserInfo(username);

        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            throw new BusinessException(1004, "Token无效");
        }
    }

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param changePasswordRequest 修改密码请求
     */
    @Transactional
    public void changePassword(String userId, AuthDTO.ChangePasswordRequest changePasswordRequest) {
        // 1. 验证新密码和确认密码是否一致
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
            throw new BusinessException(1006, "新密码和确认密码不一致");
        }

        // 2. 查找用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(1003, "用户不存在"));

        // 3. 验证原密码
        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new BusinessException(1007, "原密码错误");
        }

        // 4. 更新密码
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);

        log.info("用户{}密码修改成功", user.getUsername());
    }

    /**
     * 刷新Token
     *
     * @param token 原Token
     * @return 新Token
     */
    public String refreshToken(String token) {
        try {
            String newToken = jwtUtil.refreshToken(token);
            if (newToken == null) {
                throw new BusinessException(1008, "Token刷新失败");
            }
            return newToken;
        } catch (Exception e) {
            log.error("Token刷新失败: {}", e.getMessage());
            throw new BusinessException(1008, "Token刷新失败");
        }
    }

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return true-存在，false-不存在
     */
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 构建用户信息DTO
     *
     * @param user 用户实体
     * @return 用户信息DTO
     */
    private AuthDTO.UserInfo buildUserInfo(User user) {
        AuthDTO.UserInfo userInfo = new AuthDTO.UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setDepartment(user.getDepartment());
        userInfo.setPosition(user.getPosition());
        userInfo.setStatus(user.getStatus());
        userInfo.setLastLoginTime(user.getLastLoginTime());

        // 构建角色信息
        if (user.getRoles() != null) {
            List<AuthDTO.RoleInfo> roleInfos = user.getRoles().stream()
                    .map(this::buildRoleInfo)
                    .collect(Collectors.toList());
            userInfo.setRoles(roleInfos);
        }

        return userInfo;
    }

    /**
     * 构建角色信息DTO
     *
     * @param role 角色实体
     * @return 角色信息DTO
     */
    private AuthDTO.RoleInfo buildRoleInfo(Role role) {
        AuthDTO.RoleInfo roleInfo = new AuthDTO.RoleInfo();
        roleInfo.setRoleId(role.getId());
        roleInfo.setRoleCode(role.getRoleCode());
        roleInfo.setRoleName(role.getRoleName());
        roleInfo.setDescription(role.getDescription());
        return roleInfo;
    }
}