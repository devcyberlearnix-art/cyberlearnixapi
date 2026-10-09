package com.example.admin.exception;

import org.springframework.http.HttpStatus;

/**
 * Domain exception for all admin password-change flow violations.
 */
public class AdminPasswordChangeException extends RuntimeException {

    private final HttpStatus status;

    public AdminPasswordChangeException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static AdminPasswordChangeException badRequest(String message) {
        return new AdminPasswordChangeException(message, HttpStatus.BAD_REQUEST);
    }

    public static AdminPasswordChangeException unauthorized(String message) {
        return new AdminPasswordChangeException(message, HttpStatus.UNAUTHORIZED);
    }

    public static AdminPasswordChangeException tooManyRequests(String message) {
        return new AdminPasswordChangeException(message, HttpStatus.TOO_MANY_REQUESTS);
    }

    public static AdminPasswordChangeException gone(String message) {
        return new AdminPasswordChangeException(message, HttpStatus.GONE);
    }

    public static AdminPasswordChangeException notFound(String message) {
        return new AdminPasswordChangeException(message, HttpStatus.NOT_FOUND);
    }
}
