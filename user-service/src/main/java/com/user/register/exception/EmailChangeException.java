package com.user.register.exception;

import org.springframework.http.HttpStatus;

/**
 * Domain exception for all email-change flow violations.
 *
 * <p>Carries an {@link HttpStatus} so the {@code GlobalExceptionHandler} can
 * return the most semantically accurate HTTP response code without extra logic.
 *
 * <p>Examples:
 * <ul>
 *   <li>{@code 400} — invalid state, OTP wrong, already verified</li>
 *   <li>{@code 409} — email already in use</li>
 *   <li>{@code 410} — request expired</li>
 *   <li>{@code 429} — rate-limited / max attempts exhausted</li>
 * </ul>
 */
public class EmailChangeException extends RuntimeException {

    private final HttpStatus status;

    public EmailChangeException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    // ===== FACTORY METHODS =====

    public static EmailChangeException badRequest(String message) {
        return new EmailChangeException(message, HttpStatus.BAD_REQUEST);
    }

    public static EmailChangeException conflict(String message) {
        return new EmailChangeException(message, HttpStatus.CONFLICT);
    }

    public static EmailChangeException gone(String message) {
        return new EmailChangeException(message, HttpStatus.GONE);
    }

    public static EmailChangeException tooManyRequests(String message) {
        return new EmailChangeException(message, HttpStatus.TOO_MANY_REQUESTS);
    }

    public static EmailChangeException unauthorized(String message) {
        return new EmailChangeException(message, HttpStatus.UNAUTHORIZED);
    }

    public static EmailChangeException notFound(String message) {
        return new EmailChangeException(message, HttpStatus.NOT_FOUND);
    }
}
