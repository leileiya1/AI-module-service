package com.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 资源未找到异常。
 * <p>
 * 当尝试访问或操作一个不存在的资源时抛出此异常。
 * 通常会导致 HTTP 404 (Not Found) 响应。
 * </p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // Spring MVC 会将此异常映射为 404 状态码
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造一个新的资源未找到异常，使用指定的详细消息。
     *
     * @param message 详细消息，用于描述异常。
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * 构造一个新的资源未找到异常，使用指定的详细消息和原因。
     *
     * @param message 详细消息。
     * @param cause   异常的原因 (通常是另一个导致此异常的Throwable)。
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
