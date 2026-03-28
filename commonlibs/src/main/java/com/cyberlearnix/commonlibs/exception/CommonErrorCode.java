package com.cyberlearnix.commonlibs.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Common error codes used across all CyberLearnix services
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    
    // Validation errors
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_001", "Validation failed", "COMMON"),
    INVALID_REQUEST_FORMAT(HttpStatus.BAD_REQUEST, "VALIDATION_002", "Invalid request format", "COMMON"),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "VALIDATION_003", "Required field is missing", "COMMON"),
    
    // Authentication & Authorization
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "Unauthorized access", "COMMON"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_002", "Access forbidden", "COMMON"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "Invalid or expired token", "COMMON"),
    INSUFFICIENT_PERMISSIONS(HttpStatus.FORBIDDEN, "AUTH_004", "Insufficient permissions", "COMMON"),
    
    // Resource errors
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_001", "Resource not found", "COMMON"),
    RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "RESOURCE_002", "Resource already exists", "COMMON"),
    RESOURCE_LOCKED(HttpStatus.LOCKED, "RESOURCE_003", "Resource is locked", "COMMON"),
    
    // Product-specific errors
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Product not found", "PRODUCT"),
    
    // Data errors
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "DATA_001", "Data integrity violation", "COMMON"),
    DUPLICATE_ENTRY(HttpStatus.CONFLICT, "DATA_002", "Duplicate entry", "COMMON"),
    FOREIGN_KEY_CONSTRAINT(HttpStatus.CONFLICT, "DATA_003", "Foreign key constraint violation", "COMMON"),
    
    // External service errors
    EXTERNAL_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_001", "External service unavailable", "COMMON"),
    EXTERNAL_SERVICE_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "EXTERNAL_002", "External service timeout", "COMMON"),
    EXTERNAL_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "EXTERNAL_003", "External service error", "COMMON"),
    
    // System errors
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYSTEM_001", "Internal server error", "COMMON"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYSTEM_002", "Database operation failed", "COMMON"),
    CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYSTEM_003", "Configuration error", "COMMON"),
    
    // Rate limiting
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "RATE_001", "Rate limit exceeded", "COMMON"),
    
    // Business logic
    BUSINESS_RULE_VIOLATION(HttpStatus.BAD_REQUEST, "BUSINESS_001", "Business rule violation", "COMMON"),
    OPERATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "BUSINESS_002", "Operation not allowed", "COMMON"),
    INVALID_STATE(HttpStatus.CONFLICT, "BUSINESS_003", "Invalid state for operation", "COMMON");
    
    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;
    private final String service;
}
