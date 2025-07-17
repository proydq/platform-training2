package com.example.smarttrainingsystem.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务异常类
 * 用于处理业务逻辑中的异常情况
 * 
 * @author 开发者
 * @version 1.0
 * @since 2025-01-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {
    
    /**
     * 错误码
     */
    private Integer code;
    
    /**
     * 错误消息
     */
    private String message;
    
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}