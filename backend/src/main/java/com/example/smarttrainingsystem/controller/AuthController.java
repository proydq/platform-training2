package com.example.smarttrainingsystem.controller;

import com.example.smarttrainingsystem.common.Result;
import com.example.smarttrainingsystem.dto.AuthDTO;
import com.example.smarttrainingsystem.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证控制器
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-07-17
 */
@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Api(tags = "用户认证接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<AuthDTO.LoginResponse> login(@Valid @RequestBody AuthDTO.LoginRequest loginRequest) {
        log.info("收到登录请求: {}", loginRequest.getUsername());

        AuthDTO.LoginResponse response = authService.login(loginRequest);

        return Result.success("登录成功", response);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/userinfo")
    @ApiOperation("获取当前用户信息")
    public Result<AuthDTO.UserInfo> getUserInfo(HttpServletRequest request) {
        // 从请求中获取用户名（由JWT拦截器设置）
        String username = (String) request.getAttribute("username");
        String userId = (String) request.getAttribute("userId");

        log.info("获取用户信息请求 - username: {}, userId: {}", username, userId);

        if (username == null) {
            log.error("无法从请求属性中获取用户名");
            return Result.error(1003, "用户认证信息缺失");
        }

        try {
            AuthDTO.UserInfo userInfo = authService.getUserInfo(username);
            log.info("用户信息获取成功: {}", username);
            return Result.success("获取用户信息成功", userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败: username={}, error={}", username, e.getMessage(), e);
            return Result.error(1003, "获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    @ApiOperation("用户退出登录")
    public Result<String> logout(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        log.info("用户{}退出登录", username);

        // 注意：由于使用JWT无状态认证，这里只是记录日志
        // 实际的token失效由前端处理（删除本地存储的token）

        return Result.success("退出登录成功", null);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    @ApiOperation("刷新Token")
    public Result<String> refreshToken(@Valid @RequestBody AuthDTO.RefreshTokenRequest refreshRequest) {
        String newToken = authService.refreshToken(refreshRequest.getRefreshToken());

        return Result.success("Token刷新成功", newToken);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @ApiOperation("修改密码")
    public Result<String> changePassword(
            @Valid @RequestBody AuthDTO.ChangePasswordRequest changePasswordRequest,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        authService.changePassword(userId, changePasswordRequest);

        return Result.success("密码修改成功", null);
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    @ApiOperation("检查用户名是否可用")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = authService.isUsernameExists(username);

        return Result.success("检查完成", !exists); // 返回true表示可用
    }

    /**
     * 验证Token有效性
     */
    @PostMapping("/validate")
    @ApiOperation("验证Token有效性")
    public Result<AuthDTO.UserInfo> validateToken(@RequestHeader("Authorization") String authHeader) {
        // 提取Token（去掉Bearer前缀）
        String token = authHeader.substring(7);

        AuthDTO.UserInfo userInfo = authService.validateTokenAndGetUserInfo(token);

        return Result.success("Token验证成功", userInfo);
    }
}