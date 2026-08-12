package com.lms.cart_service.exception;

import com.cyberlearnix.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // PRD Section 7.1: Course not found -> 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handle404(ResourceNotFoundException e, HttpServletRequest request) {
        return buildResponse(e.getMessage(), HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", request);
    }

    // PRD Section 7.4: Coupon invalid -> 422
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handle422(IllegalArgumentException e, HttpServletRequest request) {
        return buildResponse(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", request);
    }

    // PRD Section 6 & 7.3: Data Isolation Violation / Unauthorized -> 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handle403(AccessDeniedException e, HttpServletRequest request) {
        return buildResponse("Access Denied: " + e.getMessage(), HttpStatus.FORBIDDEN, "ACCESS_DENIED", request);
    }

    // PRD Section 7.2: Cart empty / Logic Errors -> 400
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handle400(RuntimeException e, HttpServletRequest request) {
        return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST, "BAD_REQUEST", request);
    }

    /**
     * Helper method to use the static error builder from ApiResponse DTO.
     * This keeps the timestamp and success flag logic centralized.
     */
    private ResponseEntity<ApiErrorResponse> buildResponse(
            String message, HttpStatus status, String code, HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(), status.value(), code, message, request.getRequestURI(), traceId);
        return new ResponseEntity<>(error, status);
    }
}