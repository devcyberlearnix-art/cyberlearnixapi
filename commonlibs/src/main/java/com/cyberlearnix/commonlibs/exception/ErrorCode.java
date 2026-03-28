package com.cyberlearnix.commonlibs.exception;

import org.springframework.http.HttpStatus;

/**
 * Interface for error codes used across CyberLearnix services
 */
public interface ErrorCode {
    HttpStatus getHttpStatus();
    String getCode();
    String getDefaultMessage();
    String getService();
}
