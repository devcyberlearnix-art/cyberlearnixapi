package com.lms.courseservice.dto;

public class DeleteCourseResponse {
    private boolean success;
    private String message;
    private CourseInfo data;

    public DeleteCourseResponse() {}

    public DeleteCourseResponse(boolean success, String message, CourseInfo data) {
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

    public CourseInfo getData() {
        return data;
    }

    public void setData(CourseInfo data) {
        this.data = data;
    }
}
