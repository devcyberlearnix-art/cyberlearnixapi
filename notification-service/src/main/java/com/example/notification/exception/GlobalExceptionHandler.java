package com.example.notification.exception;

import com.cyberlearnix.error.ApiErrorResponse;
import com.cyberlearnix.error.BaseGlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Operation failed";
        String lower = msg.toLowerCase();
        if (lower.contains("not found")) {
            return buildResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", msg, request, ex);
        }
        if (lower.contains("already exists") || lower.contains("already deleted") || lower.contains("conflict")) {
            return buildResponse(HttpStatus.CONFLICT, "CONFLICT", msg, request, ex);
        }
        if (lower.contains("required") || lower.contains("invalid") || lower.contains("cannot")) {
            return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", msg, request, ex);
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", msg, request, ex);
    }
}
