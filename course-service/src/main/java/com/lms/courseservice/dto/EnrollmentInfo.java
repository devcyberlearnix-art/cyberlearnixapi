package com.lms.courseservice.dto;

import java.util.UUID;

public class EnrollmentInfo {
    private Long courseId;
    private String courseName;
    private UUID studentId;
    private String category;
    private String status;
    private String activeTime;

    public EnrollmentInfo() {}

    public EnrollmentInfo(Long courseId, String courseName, UUID studentId, String category, String status, String activeTime) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.studentId = studentId;
        this.category = category;
        this.status = status;
        this.activeTime = activeTime;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(String activeTime) {
        this.activeTime = activeTime;
    }
}
