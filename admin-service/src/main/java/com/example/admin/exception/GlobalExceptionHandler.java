package com.example.admin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidationException(org.springframework.web.bind.MethodArgumentNotValidException ex) {
                Map<String, String> errors = new java.util.HashMap<>();
                for (org.springframework.validation.FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
                        errors.put(fieldError.getField(), fieldError.getDefaultMessage());
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of(
                                                "success", false,
                                                "message", "Validation failed for one or more fields",
                                                "errors", errors,
                                                "timestamp", LocalDateTime.now().toString()
                                ));
        }

        @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
        public ResponseEntity<Map<String, Object>> handleNoResourceFoundException(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of(
                                                "success", false,
                                                "message", "Endpoint not found: " + ex.getResourcePath() + ". Please verify the URL and HTTP method (e.g. PUT /api/v1/admin/profile).",
                                                "timestamp", LocalDateTime.now().toString()
                                ));
        }

        @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<Map<String, Object>> handleMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException ex) {
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                                .body(Map.of(
                                                "success", false,
                                                "message", "HTTP method '" + ex.getMethod() + "' not supported for this endpoint. Supported methods: " + ex.getSupportedHttpMethods(),
                                                "timestamp", LocalDateTime.now().toString()
                                ));
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
                String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
                return ResponseEntity.status(ex.getStatusCode())
                                .body(Map.of(
                                                "success", false,
                                                "message", message,
                                                "timestamp", LocalDateTime.now().toString()
                                ));
        }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "success", false,
                        "message", "Access denied: " + ex.getMessage(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(org.springframework.security.core.AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "success", false,
                        "message", "Authentication failed: " + ex.getMessage(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(AdminEmailChangeException.class)
    public ResponseEntity<Map<String, Object>> handleAdminEmailChangeException(AdminEmailChangeException ex) {
        return ResponseEntity.status(ex.getHttpStatus())
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(AdminPasswordChangeException.class)
    public ResponseEntity<Map<String, Object>> handleAdminPasswordChangeException(AdminPasswordChangeException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "success", false,
                        "message", "Unexpected error: " + ex.getMessage(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }
}
