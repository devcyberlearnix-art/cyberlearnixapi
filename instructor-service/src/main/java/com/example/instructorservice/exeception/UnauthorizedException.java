package com.example.instructorservice.exeception;


public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) { super(message); }
}

