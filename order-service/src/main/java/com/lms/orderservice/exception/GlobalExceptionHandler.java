package com.lms.orderservice.exception;

import com.cyberlearnix.error.ApiErrorResponse;
import com.cyberlearnix.error.BaseGlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {

    @Override
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", exception.getMessage(), request, exception);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Bad request";
        if (msg.toLowerCase().contains("not found")) {
            return buildResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", msg, request, ex);
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", msg, request, ex);
    }
}
