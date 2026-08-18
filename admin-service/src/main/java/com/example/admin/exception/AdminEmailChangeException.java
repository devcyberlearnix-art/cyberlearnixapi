package com.example.admin.exception;

import org.springframework.http.HttpStatus;

/**
 * Domain exception for the admin email-change flow.
 * Each static factory method produces an exception carrying a specific HTTP status
 * so that {@link GlobalExceptionHandler} can map it directly to the correct response code.
 */
public class AdminEmailChangeException extends RuntimeException {

    private final HttpStatus httpStatus;

    private AdminEmailChangeException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    // ── Factory methods ────────────────────────────────────────────────────────

    public static AdminEmailChangeException badRequest(String message) {
        return new AdminEmailChangeException(HttpStatus.BAD_REQUEST, message);
    }

    public static AdminEmailChangeException unauthorized(String message) {
        return new AdminEmailChangeException(HttpStatus.UNAUTHORIZED, message);
    }

    public static AdminEmailChangeException forbidden(String message) {
        return new AdminEmailChangeException(HttpStatus.FORBIDDEN, message);
    }

    public static AdminEmailChangeException notFound(String message) {
        return new AdminEmailChangeException(HttpStatus.NOT_FOUND, message);
    }

    public static AdminEmailChangeException conflict(String message) {
        return new AdminEmailChangeException(HttpStatus.CONFLICT, message);
    }

    public static AdminEmailChangeException gone(String message) {
        return new AdminEmailChangeException(HttpStatus.GONE, message);
    }

    public static AdminEmailChangeException tooManyRequests(String message) {
        return new AdminEmailChangeException(HttpStatus.TOO_MANY_REQUESTS, message);
    }
}
