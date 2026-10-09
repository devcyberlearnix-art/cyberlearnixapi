package com.user.register.exception;

import org.springframework.http.HttpStatus;

/**
 * Domain exception for all password-change flow violations.
 */
public class PasswordChangeException extends RuntimeException {

    private final HttpStatus status;

    public PasswordChangeException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static PasswordChangeException badRequest(String message) {
        return new PasswordChangeException(message, HttpStatus.BAD_REQUEST);
    }

    public static PasswordChangeException unauthorized(String message) {
        return new PasswordChangeException(message, HttpStatus.UNAUTHORIZED);
    }

    public static PasswordChangeException tooManyRequests(String message) {
        return new PasswordChangeException(message, HttpStatus.TOO_MANY_REQUESTS);
    }

    public static PasswordChangeException gone(String message) {
        return new PasswordChangeException(message, HttpStatus.GONE);
    }

    public static PasswordChangeException notFound(String message) {
        return new PasswordChangeException(message, HttpStatus.NOT_FOUND);
    }
}
