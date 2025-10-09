package com.example.aichatservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // 当抛出此异常时，HTTP响应状态码为 404
public class PersonaNotFoundException extends RuntimeException {
    public PersonaNotFoundException(String message) {
        super(message);
    }
}
