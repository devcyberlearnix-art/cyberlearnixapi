package com.lms.courseservice.dto;

public class EnrollCourseResponse {
    private boolean success;
    private String message;
    private EnrollmentInfo data;

    public EnrollCourseResponse() {}

    public EnrollCourseResponse(boolean success, String message, EnrollmentInfo data) {
        this.success = success;
        this.message = message;
        this.data = data;
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

    public EnrollmentInfo getData() {
        return data;
    }

    public void setData(EnrollmentInfo data) {
        this.data = data;
    }
}
