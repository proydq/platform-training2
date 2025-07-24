package com.example.smarttrainingsystem.controller;

import com.example.smarttrainingsystem.common.Result;
import com.example.smarttrainingsystem.dto.UserDTO;
import com.example.smarttrainingsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;
import javax.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<Map<String, Object>> getUsers(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "keyword", required = false) String keyword) {

        Page<UserDTO.ListItem> users = userService.getUsers(page, pageSize, role, keyword);
        Map<String, Object> resp = new HashMap<>();
        resp.put("data", users.getContent());
        resp.put("total", users.getTotalElements());
        return Result.success("获取用户列表成功", resp);
    }

    @PostMapping
    public Result<UserDTO.ListItem> createUser(@Valid @RequestBody UserDTO.CreateRequest request) {
        UserDTO.ListItem user = userService.createUser(request);
        return Result.success("创建用户成功", user);
    }

    @PutMapping("/{id}")
    public Result<UserDTO.ListItem> updateUser(@PathVariable("id") String id,
                                               @RequestBody UserDTO.CreateRequest request) {
        if (!StringUtils.hasText(request.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("姓名不能为空");
        }
        if (!StringUtils.hasText(request.getRole())) {
            throw new IllegalArgumentException("角色不能为空");
        }
        if (!StringUtils.hasText(request.getStatus())) {
            throw new IllegalArgumentException("状态不能为空");
        }
        if (StringUtils.hasText(request.getPassword())) {
            int len = request.getPassword().length();
            if (len < 6 || len > 20) {
                throw new IllegalArgumentException("密码长度必须在6-20字符之间");
            }
        }
        UserDTO.ListItem user = userService.updateUser(id, request);
        return Result.success("更新用户成功", user);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable("id") String id) {
        userService.deleteUser(id);
        return Result.success("删除用户成功", null);
    }

    @PostMapping("/{id}/reset-password")
    public Result<?> resetPassword(@PathVariable String id) {
        userService.resetPassword(id);
        return Result.success("密码已重置", null);
    }
}
