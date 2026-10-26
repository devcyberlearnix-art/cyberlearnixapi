package com.lms.courseservice.exception;

import com.cyberlearnix.error.ApiErrorResponse;
import com.cyberlearnix.error.BaseGlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {

    @ExceptionHandler(EnrollmentException.class)
    public ResponseEntity<ApiErrorResponse> handleEnrollmentException(
            EnrollmentException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "ENROLLMENT_CONFLICT", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(BannerException.class)
    public ResponseEntity<ApiErrorResponse> handleBannerException(
            BannerException ex, HttpServletRequest request) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Banner error";
        HttpStatus status = msg.toLowerCase().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        String code = status == HttpStatus.NOT_FOUND ? "RESOURCE_NOT_FOUND" : "BANNER_ERROR";
        return buildResponse(status, code, msg, request, ex);
    }

    @ExceptionHandler(LiveClassException.class)
    public ResponseEntity<ApiErrorResponse> handleLiveClassException(
            LiveClassException ex, HttpServletRequest request) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Live class error";
        HttpStatus status = msg.toLowerCase().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        String code = status == HttpStatus.NOT_FOUND ? "RESOURCE_NOT_FOUND" : "LIVE_CLASS_ERROR";
        return buildResponse(status, code, msg, request, ex);
    }

    @ExceptionHandler(LiveClassAttendanceException.class)
    public ResponseEntity<ApiErrorResponse> handleLiveClassAttendanceException(
            LiveClassAttendanceException ex, HttpServletRequest request) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Live class attendance error";
        HttpStatus status = msg.toLowerCase().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        String code = status == HttpStatus.NOT_FOUND ? "RESOURCE_NOT_FOUND" : "ATTENDANCE_ERROR";
        return buildResponse(status, code, msg, request, ex);
    }

    @ExceptionHandler(QuizException.class)
    public ResponseEntity<ApiErrorResponse> handleQuizException(
            QuizException ex, HttpServletRequest request) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Quiz error";
        HttpStatus status = msg.toLowerCase().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        String code = status == HttpStatus.NOT_FOUND ? "RESOURCE_NOT_FOUND" : "QUIZ_ERROR";
        return buildResponse(status, code, msg, request, ex);
    }

    @ExceptionHandler(AssignmentException.class)
    public ResponseEntity<ApiErrorResponse> handleAssignmentException(
            AssignmentException ex, HttpServletRequest request) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Assignment error";
        HttpStatus status = msg.toLowerCase().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        String code = status == HttpStatus.NOT_FOUND ? "RESOURCE_NOT_FOUND" : "ASSIGNMENT_ERROR";
        return buildResponse(status, code, msg, request, ex);
    }

    @ExceptionHandler(CertificateException.class)
    public ResponseEntity<ApiErrorResponse> handleCertificateException(
            CertificateException ex, HttpServletRequest request) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Certificate error";
        HttpStatus status = msg.toLowerCase().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        String code = status == HttpStatus.NOT_FOUND ? "RESOURCE_NOT_FOUND" : "CERTIFICATE_ERROR";
        return buildResponse(status, code, msg, request, ex);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Bad request";
        if (msg.toLowerCase().contains("not found")) {
            return buildResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", msg, request, ex);
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", msg, request, ex);
    }
}
