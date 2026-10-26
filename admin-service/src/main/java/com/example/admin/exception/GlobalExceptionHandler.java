package com.example.admin.exception;

import com.cyberlearnix.error.ApiErrorResponse;
import com.cyberlearnix.error.BaseGlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {

    @ExceptionHandler(AdminEmailChangeException.class)
    public ResponseEntity<ApiErrorResponse> handleAdminEmailChangeException(
            AdminEmailChangeException ex, HttpServletRequest request) {
        HttpStatus status = ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.BAD_REQUEST;
        String code = status == HttpStatus.CONFLICT ? "CONFLICT"
                : status == HttpStatus.NOT_FOUND ? "RESOURCE_NOT_FOUND"
                : status == HttpStatus.UNAUTHORIZED ? "UNAUTHORIZED"
                : "BAD_REQUEST";
        return buildResponse(status, code, ex.getMessage(), request, ex);
    }

    @ExceptionHandler(AdminPasswordChangeException.class)
    public ResponseEntity<ApiErrorResponse> handleAdminPasswordChangeException(
            AdminPasswordChangeException ex, HttpServletRequest request) {
        HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.BAD_REQUEST;
        String code = status == HttpStatus.UNAUTHORIZED ? "UNAUTHORIZED"
                : status == HttpStatus.BAD_REQUEST ? "BAD_REQUEST"
                : "PASSWORD_ERROR";
        return buildResponse(status, code, ex.getMessage(), request, ex);
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
