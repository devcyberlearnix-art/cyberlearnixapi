package com.lms.review.exception;

import com.cyberlearnix.error.ApiErrorResponse;
import com.cyberlearnix.error.BaseGlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.BAD_REQUEST;
        String code = status == HttpStatus.NOT_FOUND ? "RESOURCE_NOT_FOUND" :
                      status == HttpStatus.CONFLICT ? "CONFLICT" : "BUSINESS_ERROR";
        return buildResponse(status, code, ex.getMessage(), request, ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                                         HttpServletRequest request) {
        String message = "Data integrity violation";
        if (ex.getMessage() != null && ex.getMessage().contains("uk_user_course")) {
            message = "You have already reviewed this course";
        }
        return buildResponse(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", message, request, ex);
    }
}
