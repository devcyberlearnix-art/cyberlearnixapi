package com.lms.courseservice.exception;

public class AssignmentException extends RuntimeException {
    public AssignmentException(String message) {
        super(message);
    }

    public AssignmentException(String message, Throwable cause) {
        super(message, cause);
    }
}