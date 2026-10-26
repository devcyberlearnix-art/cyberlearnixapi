package com.lms.courseservice.exception;

public class LiveClassAttendanceException extends RuntimeException {
    public LiveClassAttendanceException(String message) {
        super(message);
    }

    public LiveClassAttendanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
