package com.example.instructorservice.exeception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) { super(message); }
}
