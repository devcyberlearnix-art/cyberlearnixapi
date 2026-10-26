package com.lms.courseservice.exception;

public class LiveClassException extends RuntimeException {
    public LiveClassException(String message) {
        super(message);
    }

    public LiveClassException(String message, Throwable cause) {
        super(message, cause);
    }
}
