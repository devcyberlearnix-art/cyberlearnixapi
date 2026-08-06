package com.lms.courseservice.dto;

import java.time.Instant;

public class ApiResponse {

    private boolean success;
    private String message;
    private String timestamp;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, Instant timestamp) {
        this.success = success;
        this.message = message;
        this.timestamp = timestamp.toString();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
