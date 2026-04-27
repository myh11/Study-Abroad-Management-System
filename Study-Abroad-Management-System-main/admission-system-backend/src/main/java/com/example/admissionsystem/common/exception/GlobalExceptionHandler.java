package com.example.admissionsystem.common.exception;

import com.example.admissionsystem.common.response.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public Result<Void> handleInvalidStatusTransitionException(
            InvalidStatusTransitionException ex,
            HttpServletRequest request
    ) {
        log.warn("Invalid status transition on [{}]: {}", request.getRequestURI(), ex.getMessage());
        return Result.fail(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.warn("Illegal argument on [{}]: {}", request.getRequestURI(), ex.getMessage());
        return Result.fail(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = extractValidationMessage(ex.getBindingResult().getFieldError());
        log.warn("Method argument validation failed on [{}]: {}", request.getRequestURI(), message);
        return Result.fail(message);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException ex, HttpServletRequest request) {
        String message = extractValidationMessage(ex.getBindingResult().getFieldError());
        log.warn("Bind validation failed on [{}]: {}", request.getRequestURI(), message);
        return Result.fail(message);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Result<Void> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Authentication failed on [{}]: {}", request.getRequestURI(), ex.getMessage());
        return Result.fail(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on [{}]", request.getRequestURI(), ex);
        return Result.fail("系统异常，请稍后重试");
    }

    private String extractValidationMessage(FieldError fieldError) {
        if (fieldError == null) {
            return "请求参数不合法";
        }
        if (fieldError.getDefaultMessage() != null && !fieldError.getDefaultMessage().isBlank()) {
            return fieldError.getDefaultMessage();
        }
        return fieldError.getField() + " is invalid";
    }
}
