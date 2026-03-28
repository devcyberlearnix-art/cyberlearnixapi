package com.cyberlearnix.commonlibs.exception;

import lombok.Getter;

/**
 * Base exception class for all CyberLearnix services
 */
@Getter
public class CyberLearnixException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final String userMessage;
    
    public CyberLearnixException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = message;
    }
    
    public CyberLearnixException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = message;
    }
    
    public CyberLearnixException(ErrorCode errorCode, String message, String userMessage) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    public CyberLearnixException(ErrorCode errorCode, String message, String userMessage, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }
}
