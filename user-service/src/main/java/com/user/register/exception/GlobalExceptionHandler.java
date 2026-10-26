package com.user.register.exception;

import com.cyberlearnix.error.ApiErrorResponse;
import com.cyberlearnix.error.BaseGlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {

    @ExceptionHandler(OtpRateLimitException.class)
    public ResponseEntity<ApiErrorResponse> handleOtpRateLimit(OtpRateLimitException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(OtpCooldownException.class)
    public ResponseEntity<ApiErrorResponse> handleOtpCooldown(OtpCooldownException ex, HttpServletRequest request) {
        String message = "Please wait " + ex.getRemainingSeconds() + " seconds before requesting another OTP.";
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", message, request, ex);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidOtp(InvalidOtpException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_OTP", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ApiErrorResponse> handleLoginFailed(LoginFailedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(EmailChangeException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailChangeException(EmailChangeException ex, HttpServletRequest request) {
        HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.BAD_REQUEST;
        String code = status == HttpStatus.CONFLICT ? "CONFLICT"
                : status == HttpStatus.NOT_FOUND ? "RESOURCE_NOT_FOUND"
                : status == HttpStatus.TOO_MANY_REQUESTS ? "RATE_LIMIT_EXCEEDED"
                : status == HttpStatus.UNAUTHORIZED ? "UNAUTHORIZED"
                : "BAD_REQUEST";
        return buildResponse(status, code, ex.getMessage(), request, ex);
    }

    @ExceptionHandler(PasswordChangeException.class)
    public ResponseEntity<ApiErrorResponse> handlePasswordChangeException(PasswordChangeException ex, HttpServletRequest request) {
        HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.BAD_REQUEST;
        String code = status == HttpStatus.UNAUTHORIZED ? "UNAUTHORIZED"
                : status == HttpStatus.BAD_REQUEST ? "BAD_REQUEST"
                : "PASSWORD_ERROR";
        return buildResponse(status, code, ex.getMessage(), request, ex);
    }

    @ExceptionHandler(ProfilePhotoException.class)
    public ResponseEntity<ApiErrorResponse> handleProfilePhotoException(ProfilePhotoException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameNotFound(UsernameNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request, ex);
    }
}
