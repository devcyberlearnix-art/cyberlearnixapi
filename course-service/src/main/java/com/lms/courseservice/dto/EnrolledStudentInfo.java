package com.lms.courseservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class EnrolledStudentInfo {
    private UUID studentId;
    private String studentName;
    private LocalDateTime enrolledAt;
    private String status;
    private Double progress;

    public EnrolledStudentInfo() {}

    public EnrolledStudentInfo(UUID studentId, String studentName, LocalDateTime enrolledAt, String status, Double progress) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.enrolledAt = enrolledAt;
        this.status = status;
        this.progress = progress;
    }

    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getProgress() { return progress; }
    public void setProgress(Double progress) { this.progress = progress; }
}
