package com.example.smarttrainingsystem.exception;

/**
 * 业务异常类
 *
 * @author 开发者
 * @version 1.0
 * @since 2025-07-17
 */
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误消息
     */
    private final String message;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * 常用业务异常静态方法
     */
    public static BusinessException userNotFound() {
        return new BusinessException(1003, "用户不存在");
    }

    public static BusinessException invalidCredentials() {
        return new BusinessException(1001, "用户名或密码错误");
    }

    public static BusinessException accountDisabled() {
        return new BusinessException(1002, "账号已被禁用，请联系管理员");
    }

    public static BusinessException invalidToken() {
        return new BusinessException(1004, "Token无效");
    }

    public static BusinessException tokenExpired() {
        return new BusinessException(1005, "Token已过期");
    }

    public static BusinessException passwordMismatch() {
        return new BusinessException(1006, "新密码和确认密码不一致");
    }

    public static BusinessException wrongPassword() {
        return new BusinessException(1007, "原密码错误");
    }

    public static BusinessException tokenRefreshFailed() {
        return new BusinessException(1008, "Token刷新失败");
    }
}