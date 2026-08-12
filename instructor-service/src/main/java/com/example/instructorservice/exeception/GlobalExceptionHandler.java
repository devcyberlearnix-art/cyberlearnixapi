package com.example.instructorservice.exeception;

import com.cyberlearnix.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ApiErrorResponse> handleUnauthorized(
                        UnauthorizedException ex, HttpServletRequest request) {
                return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Instructor access required", request);
    }

    @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<ApiErrorResponse> handleForbidden(
                        ForbiddenException ex, HttpServletRequest request) {
                return build(HttpStatus.FORBIDDEN, "FORBIDDEN", "Cannot access another instructor's data", request);
    }

    @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleNotFound(
                        NotFoundException ex, HttpServletRequest request) {
                return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(ValidationException.class)
        public ResponseEntity<ApiErrorResponse> handleValidation(
                        ValidationException ex, HttpServletRequest request) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleServerError(Exception ex, HttpServletRequest request) {
        ex.printStackTrace();
                return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                                "An unexpected error occurred", request);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
        public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
                        org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
                return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied", request);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
        public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
                        org.springframework.security.core.AuthenticationException ex, HttpServletRequest request) {
                return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication failed", request);
        }

        private ResponseEntity<ApiErrorResponse> build(
                        HttpStatus status, String code, String message, HttpServletRequest request) {
                String traceId = request.getHeader("X-Trace-Id");
                if (traceId == null || traceId.isBlank()) {
                        traceId = UUID.randomUUID().toString();
                }
                ApiErrorResponse error = new ApiErrorResponse(
                                Instant.now(), status.value(), code, message, request.getRequestURI(), traceId);
                return ResponseEntity.status(status).body(error);
    }
}
