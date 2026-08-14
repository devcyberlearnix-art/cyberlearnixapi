package com.user.register.exception;

import lombok.Getter;

@Getter
public class OtpRateLimitException extends RuntimeException {
    private final long retryAfterSeconds;

    public OtpRateLimitException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}