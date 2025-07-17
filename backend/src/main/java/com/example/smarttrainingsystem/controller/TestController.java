package com.example.smarttrainingsystem.controller;

import com.example.smarttrainingsystem.common.Result;
import com.example.smarttrainingsystem.exception.BusinessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * 用于验证统一返回值格式和异常处理
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-01-18
 */
@RestController
@RequestMapping("/v1/test")
public class TestController {

    /**
     * 测试成功返回
     */
    @GetMapping("/success")
    public Result<String> testSuccess() {
        return Result.success("测试数据");
    }

    /**
     * 测试成功返回（带对象数据）
     */
    @GetMapping("/success-object")
    public Result<Map<String, Object>> testSuccessObject() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "智能培训系统");
        data.put("version", "1.0.0");
        data.put("status", "running");
        return Result.success("获取系统信息成功", data);
    }

    /**
     * 测试失败返回
     */
    @GetMapping("/error")
    public Result<String> testError() {
        return Result.error("这是一个测试错误");
    }

    /**
     * 测试自定义错误码
     */
    @GetMapping("/custom-error")
    public Result<String> testCustomError() {
        return Result.error(400, "自定义错误码测试");
    }

    /**
     * 测试业务异常处理
     */
    @GetMapping("/business-exception")
    public Result<String> testBusinessException() {
        throw new BusinessException(1001, "这是一个业务异常测试");
    }

    /**
     * 测试系统异常处理
     */
    @GetMapping("/system-exception")
    public Result<String> testSystemException() {
        // 故意触发空指针异常
        String str = null;
        return Result.success(str.length() + "");
    }

    /**
     * 测试参数异常处理
     */
    @GetMapping("/param-exception")
    public Result<String> testParamException() {
        throw new IllegalArgumentException("参数不能为空");
    }
}