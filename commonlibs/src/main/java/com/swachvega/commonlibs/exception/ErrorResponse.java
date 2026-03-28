package com.cyberlearnix.commonlibs.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response structure for all CyberLearnix services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private boolean success;
    private String service;
    private String errorCode;
    private String message;
    private String userMessage;
    private LocalDateTime timestamp;
    private String path;
    private int status;
    private List<ValidationError> validationErrors;
    private String traceId; // For distributed tracing
    // Optional structured fields for business errors
    private Double maxWeightAllowed;
    private Double currentWeight;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private Object rejectedValue;
        private String message;
    }
}
