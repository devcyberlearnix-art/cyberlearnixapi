package com.lms.wishlist_service.exception;

import com.cyberlearnix.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles your custom WishlistException (e.g., Course already exists)
     */
    @ExceptionHandler(WishlistException.class)
    public ResponseEntity<ApiErrorResponse> handleWishlistException(
            WishlistException ex, HttpServletRequest request) {
        return build(ex.getStatus(), ex.getStatus().name(), ex.getMessage(), request);
    }

    /**
     * Fallback for unexpected errors (NullPointer, Database down, etc.)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(
            Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred", request);
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