package com.lms.courseservice.exception;

import com.lms.courseservice.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EnrollmentException.class)
    public ResponseEntity<ApiResponse<Void>> handleEnrollmentException(EnrollmentException ex) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
            .success(false)
            .message(ex.getMessage())
            .timestamp(Instant.now().toString())
            .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
            .success(false)
            .message(ex.getMessage())
            .timestamp(Instant.now().toString())
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
            .success(false)
            .message("Access denied: " + ex.getMessage())
            .timestamp(Instant.now().toString())
            .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(org.springframework.security.core.AuthenticationException ex) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
            .success(false)
            .message("Authentication failed: " + ex.getMessage())
            .timestamp(Instant.now().toString())
            .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
