package com.lms.coupon_service.exception;

import com.lms.coupon_service.dto.ApiResponse;
import com.lms.coupon_service.exception.CouponAlreadyExistsException;
import com.lms.coupon_service.exception.CouponValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Centralized exception handling for the Coupon Service.
 * Ensures all errors follow a consistent JSON format.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles specific business logic errors thrown from the Service layer.
     * Triggered by: Validation failures, limit reached, or invalid assignments.
     */
    @ExceptionHandler(CouponValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(CouponValidationException ex) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage(), ex.getErrors()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CouponAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleAlreadyExists(CouponAlreadyExistsException ex) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessLogic(RuntimeException ex) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles Database Integrity issues.
     * Triggered by: Duplicate Coupon Codes (Unique constraint violation).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateKeyException(DataIntegrityViolationException ex) {
        return new ResponseEntity<>(ApiResponse.error("A coupon with this code already exists."), HttpStatus.CONFLICT);
    }

    /**
     * Catch-all for all other unexpected exceptions.
     * Ensures the API always returns a JSON body rather than an empty response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAll(Exception ex) {
        return new ResponseEntity<>(ApiResponse.error("An unexpected error occurred: " + ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
