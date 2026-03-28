package com.cyberlearnix.commonlibs.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.http.converter.HttpMessageNotReadableException;
 import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

 import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
 import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Base exception handler that can be extended by service-specific handlers
 */
@Slf4j
public abstract class BaseExceptionHandler {

    protected abstract String getServiceName();

    /**
     * Handle CyberLearnixException - custom business logic exceptions
     */
    @ExceptionHandler(CyberLearnixException.class)
    public ResponseEntity<?> handleCyberLearnixException(CyberLearnixException ex, WebRequest request) {
        log.warn("{} business exception: {} - {}", getServiceName(), ex.getErrorCode().getCode(), ex.getMessage());

        if (isSimpleFormat(request)) {
            SimpleErrorResponse simple = SimpleErrorResponse.builder()
                    .success(false)
                    .message(ex.getUserMessage() != null ? ex.getUserMessage() : ex.getMessage())
                    .timestamp(LocalDateTime.now())
                    .path(request.getDescription(false))
                    .status(ex.getErrorCode().getHttpStatus().value())
                    .traceId(generateTraceId())
                    .build();
            return new ResponseEntity<>(simple, ex.getErrorCode().getHttpStatus());
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .service(getServiceName())
                .errorCode(ex.getErrorCode().getCode())
                .message(ex.getMessage())
                .userMessage(ex.getUserMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(ex.getErrorCode().getHttpStatus().value())
                .traceId(generateTraceId())
                .build();

        return new ResponseEntity<>(errorResponse, ex.getErrorCode().getHttpStatus());
    }

    /**
     * Handle IllegalArgumentException - typically used for validation errors
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.warn("{} illegal argument exception: {}", getServiceName(), ex.getMessage());

        if (isSimpleFormat(request)) {
            SimpleErrorResponse simple = SimpleErrorResponse.builder()
                    .success(false)
                    .message(ex.getMessage())
                    .timestamp(LocalDateTime.now())
                    .path(request.getDescription(false))
                    .status(HttpStatus.BAD_REQUEST.value())
                    .traceId(generateTraceId())
                    .build();
            return new ResponseEntity<>(simple, HttpStatus.BAD_REQUEST);
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .service(getServiceName())
                .errorCode(CommonErrorCode.VALIDATION_ERROR.getCode())
                .message(ex.getMessage())
                .userMessage(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(HttpStatus.BAD_REQUEST.value())
                .traceId(generateTraceId())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

        String userMsg = ex.getReason() != null ? ex.getReason() : "Request failed";

        if (isSimpleFormat(request)) {
            SimpleErrorResponse simple = SimpleErrorResponse.builder()
                    .success(false)
                    .message(userMsg)
                    .timestamp(LocalDateTime.now())
                    .path(request.getDescription(false))
                    .status(status.value())
                    .traceId(generateTraceId())
                    .build();
            return new ResponseEntity<>(simple, status);
        }

        // Treat all ResponseStatusExceptions as client-visible errors and keep them out of SYSTEM_001
        CommonErrorCode code = (status.is4xxClientError())
                ? CommonErrorCode.VALIDATION_ERROR
                : CommonErrorCode.INTERNAL_ERROR;

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .service(getServiceName())
                .errorCode(code.getCode())
                .message(userMsg)
                .userMessage(userMsg)
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(status.value())
                .traceId(generateTraceId())
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler({EntityNotFoundException.class, JpaObjectRetrievalFailureException.class, NoSuchElementException.class})
    public ResponseEntity<?> handleNotFoundExceptions(Exception ex, WebRequest request) {
        log.warn("{} resource not found: {}", getServiceName(), ex.getMessage());

        String userMsg = "Requested resource was not found";

        if (isSimpleFormat(request)) {
            SimpleErrorResponse simple = SimpleErrorResponse.builder()
                    .success(false)
                    .message(userMsg)
                    .timestamp(LocalDateTime.now())
                    .path(request.getDescription(false))
                    .status(HttpStatus.NOT_FOUND.value())
                    .traceId(generateTraceId())
                    .build();
            return new ResponseEntity<>(simple, HttpStatus.NOT_FOUND);
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .service(getServiceName())
                .errorCode(CommonErrorCode.RESOURCE_NOT_FOUND.getCode())
                .message(ex.getMessage())
                .userMessage(userMsg)
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(HttpStatus.NOT_FOUND.value())
                .traceId(generateTraceId())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    /**
     * Handle MethodArgumentTypeMismatchException - e.g. path/query param type mismatch
     * Example: /api/cart/{cartId}/coupon where cartId expects Long but client sends UUID.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String param = ex.getName();
        String value = ex.getValue() == null ? null : String.valueOf(ex.getValue());
        String required = ex.getRequiredType() == null ? null : ex.getRequiredType().getSimpleName();

        String msg = "Invalid value";
        if (param != null && required != null && value != null) {
            msg = "Invalid value for '" + param + "': " + value + " (expected " + required + ")";
        } else if (param != null && value != null) {
            msg = "Invalid value for '" + param + "': " + value;
        }

        String userMsg = "Invalid ID format provided";

        if (isSimpleFormat(request)) {
            SimpleErrorResponse simple = SimpleErrorResponse.builder()
                    .success(false)
                    .message(userMsg)
                    .timestamp(LocalDateTime.now())
                    .path(request.getDescription(false))
                    .status(HttpStatus.BAD_REQUEST.value())
                    .traceId(generateTraceId())
                    .build();
            return new ResponseEntity<>(simple, HttpStatus.BAD_REQUEST);
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .service(getServiceName())
                .errorCode(CommonErrorCode.INVALID_REQUEST_FORMAT.getCode())
                .message(msg)
                .userMessage(userMsg)
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(HttpStatus.BAD_REQUEST.value())
                .traceId(generateTraceId())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle downstream connectivity issues (connection refused, timeouts)
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<?> handleResourceAccessException(ResourceAccessException ex, WebRequest request) {
        log.error("{} downstream service unavailable: {}", getServiceName(), ex.getMessage());

        String msg = "Downstream service unavailable";
        String userMsg = "A required service is currently unavailable. Please try again in a moment.";

        if (isSimpleFormat(request)) {
            SimpleErrorResponse simple = SimpleErrorResponse.builder()
                    .success(false)
                    .message(userMsg)
                    .timestamp(LocalDateTime.now())
                    .path(request.getDescription(false))
                    .status(CommonErrorCode.EXTERNAL_SERVICE_UNAVAILABLE.getHttpStatus().value())
                    .traceId(generateTraceId())
                    .build();
            return new ResponseEntity<>(simple, CommonErrorCode.EXTERNAL_SERVICE_UNAVAILABLE.getHttpStatus());
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .service(getServiceName())
                .errorCode(CommonErrorCode.EXTERNAL_SERVICE_UNAVAILABLE.getCode())
                .message(msg)
                .userMessage(userMsg)
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(CommonErrorCode.EXTERNAL_SERVICE_UNAVAILABLE.getHttpStatus().value())
                .traceId(generateTraceId())
                .build();

        return new ResponseEntity<>(errorResponse, CommonErrorCode.EXTERNAL_SERVICE_UNAVAILABLE.getHttpStatus());
    }

    /**
     * Handle JSON parse errors (e.g., invalid UUID format in request body)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        log.warn("{} unreadable HTTP message: {}", getServiceName(), ex.getMessage());

        String msg = "Malformed request payload";
        String userMsg = "We could not read your request. Please check the details and try again.";
        try {
            Throwable cause = ex.getMostSpecificCause();
            if (cause != null) {
                String cm = cause.getMessage();
                if (cm != null && cm.contains("java.util.UUID")) {
                    String field = null;
                    String value = null;

                    try {
                        // Example patterns from Jackson:
                        // - Cannot deserialize value of type `java.util.UUID` from String "...": UUID has to be represented...
                        // - ... through reference chain: com...ApplyCouponsRequest["userId"]
                        java.util.regex.Matcher v = java.util.regex.Pattern
                                .compile("from String \\\"([^\\\"]+)\\\"")
                                .matcher(cm);
                        if (v.find()) {
                            value = v.group(1);
                        }

                        java.util.regex.Matcher f = java.util.regex.Pattern
                                .compile("\\[\\\"([^\\\"]+)\\\"\\]\\s*$")
                                .matcher(cm);
                        if (f.find()) {
                            field = f.group(1);
                        }
                    } catch (Exception ignore) { }

                    if (field != null && value != null) {
                        msg = "Invalid UUID format for field '" + field + "': " + value;
                        userMsg = "Invalid UUID in '" + field + "'. Please send a valid UUID (example: 123e4567-e89b-12d3-a456-426614174000).";
                    } else if (value != null) {
                        msg = "Invalid UUID format in request: " + value;
                        userMsg = "One of the IDs you sent is invalid. Please copy the full ID and try again.";
                    } else {
                        msg = "Invalid UUID format in request";
                        userMsg = "One of the IDs you sent is invalid. Please copy the full ID and try again.";
                    }
                } else if (cm != null && !cm.isBlank()) {
                    msg = cm;
                }
            }
        } catch (Exception ignore) { }

        if (isSimpleFormat(request)) {
            SimpleErrorResponse simple = SimpleErrorResponse.builder()
                    .success(false)
                    .message(userMsg)
                    .timestamp(LocalDateTime.now())
                    .path(request.getDescription(false))
                    .status(HttpStatus.BAD_REQUEST.value())
                    .traceId(generateTraceId())
                    .build();
            return new ResponseEntity<>(simple, HttpStatus.BAD_REQUEST);
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .service(getServiceName())
                .errorCode(CommonErrorCode.INVALID_REQUEST_FORMAT.getCode())
                .message(msg)
                .userMessage(userMsg)
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(HttpStatus.BAD_REQUEST.value())
                .traceId(generateTraceId())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalStateException - often indicates business rule/state violations
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        log.warn("{} business rule/state exception: {}", getServiceName(), ex.getMessage());

        // Default friendly message
        String friendly = "Reduce cart weight to 50.00 kg or less to proceed";

        // Try to extract structured values (maxWeightAllowed, currentWeight) from the message
        Double maxW = null;
        Double currW = null;
        try {
            // Matches: "Maximum allowed: 50.00 kg, Current weight: 51.88 kg"
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("Maximum allowed:\\s*([0-9]+(?:\\.[0-9]+)?)\\s*kg,\\s*Current weight:\\s*([0-9]+(?:\\.[0-9]+)?)\\s*kg");
            java.util.regex.Matcher m = p.matcher(ex.getMessage() != null ? ex.getMessage() : "");
            if (m.find()) {
                maxW = Double.parseDouble(m.group(1));
                currW = Double.parseDouble(m.group(2));
                // If we parsed max, tailor friendly message with two decimals
                friendly = String.format("Reduce cart weight to %.2f kg or less to proceed", maxW);
            }
        } catch (Exception ignore) { }

        if (isSimpleFormat(request)) {
            SimpleErrorResponse simple = SimpleErrorResponse.builder()
                    .success(false)
                    .message(friendly)
                    .timestamp(LocalDateTime.now())
                    .path(request.getDescription(false))
                    .status(HttpStatus.BAD_REQUEST.value())
                    .traceId(generateTraceId())
                    .build();
            return new ResponseEntity<>(simple, HttpStatus.BAD_REQUEST);
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .service(getServiceName())
                .errorCode(CommonErrorCode.BUSINESS_RULE_VIOLATION.getCode())
                .message(ex.getMessage())
                .userMessage(friendly)
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(HttpStatus.BAD_REQUEST.value())
                .traceId(generateTraceId())
                .maxWeightAllowed(maxW)
                .currentWeight(currW)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle MethodArgumentNotValidException - Spring validation errors for @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("{} validation exception: {}", getServiceName(), ex.getMessage());
        
        List<ErrorResponse.ValidationError> validationErrors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.add(ErrorResponse.ValidationError.builder()
                    .field(fieldError.getField())
                    .rejectedValue(fieldError.getRejectedValue())
                    .message(fieldError.getDefaultMessage())
                    .build());
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .service(getServiceName())
                .errorCode(CommonErrorCode.VALIDATION_ERROR.getCode())
                .message("Validation failed")
                .userMessage("Please check your input data")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(HttpStatus.BAD_REQUEST.value())
                .validationErrors(validationErrors)
                .traceId(generateTraceId())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle ConstraintViolationException - Bean validation errors
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        log.warn("{} constraint violation exception: {}", getServiceName(), ex.getMessage());
        
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        List<ErrorResponse.ValidationError> validationErrors = violations.stream()
                .map(violation -> ErrorResponse.ValidationError.builder()
                        .field(violation.getPropertyPath().toString())
                        .rejectedValue(violation.getInvalidValue())
                        .message(violation.getMessage())
                        .build())
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .service(getServiceName())
                .errorCode(CommonErrorCode.VALIDATION_ERROR.getCode())
                .message("Validation constraint violation")
                .userMessage("Please check your input data")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(HttpStatus.BAD_REQUEST.value())
                .validationErrors(validationErrors)
                .traceId(generateTraceId())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle DataIntegrityViolationException - Database constraint violations
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
        log.error("{} data integrity violation: {}", getServiceName(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .service(getServiceName())
                .errorCode(CommonErrorCode.DATA_INTEGRITY_VIOLATION.getCode())
                .message("Data integrity constraint violation")
                .userMessage("The operation conflicts with existing data constraints")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(HttpStatus.CONFLICT.value())
                .traceId(generateTraceId())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle DuplicateKeyException - Duplicate entries
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateKeyException(DuplicateKeyException ex, WebRequest request) {
        log.warn("{} duplicate key exception: {}", getServiceName(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .service(getServiceName())
                .errorCode(CommonErrorCode.DUPLICATE_ENTRY.getCode())
                .message("Duplicate entry detected")
                .userMessage("This record already exists")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(HttpStatus.CONFLICT.value())
                .traceId(generateTraceId())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle DataAccessException - Database related errors
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex, WebRequest request) {
        log.error("{} database access exception: {}", getServiceName(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .service(getServiceName())
                .errorCode(CommonErrorCode.DATABASE_ERROR.getCode())
                .message("Database operation failed")
                .userMessage("A database error occurred. Please try again later.")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .traceId(generateTraceId())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle NumberFormatException - typically for UUID parsing errors
     */
    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<ErrorResponse> handleNumberFormatException(NumberFormatException ex, WebRequest request) {
        log.warn("{} number format exception: {}", getServiceName(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .service(getServiceName())
                .errorCode(CommonErrorCode.INVALID_REQUEST_FORMAT.getCode())
                .message("Invalid number format")
                .userMessage("Invalid ID format provided")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(HttpStatus.BAD_REQUEST.value())
                .traceId(generateTraceId())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle RuntimeException - Generic runtime errors
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("{} runtime exception: {}", getServiceName(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .service(getServiceName())
                .errorCode(CommonErrorCode.INTERNAL_ERROR.getCode())
                .message("An unexpected error occurred")
                .userMessage("Something went wrong. Please try again later.")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .traceId(generateTraceId())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle all other exceptions - Generic fallback
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("{} unexpected exception: {}", getServiceName(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .service(getServiceName())
                .errorCode(CommonErrorCode.INTERNAL_ERROR.getCode())
                .message("An unexpected error occurred")
                .userMessage("Something went wrong. Please try again later.")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .traceId(generateTraceId())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Generate a trace ID for error tracking
     */
    protected String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private boolean isSimpleFormat(WebRequest request) {
        String header = request.getHeader("X-Error-Format");
        return header != null && header.trim().equalsIgnoreCase("simple");
    }
}
