package com.lms.courseservice.dto;

import java.util.UUID;

public class EnrollmentCheckDetailResponse {
    private boolean success;
    private String message;
    private EnrollmentCheckData data;

    public EnrollmentCheckDetailResponse() {}

    public EnrollmentCheckDetailResponse(boolean success, String message, EnrollmentCheckData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public EnrollmentCheckData getData() { return data; }
    public void setData(EnrollmentCheckData data) { this.data = data; }

    public static class EnrollmentCheckData {
        private Long courseId;
        private String courseName;
        private UUID studentId;
        private boolean enrolled;
        private String enrollmentStatus;

        public EnrollmentCheckData() {}

        public EnrollmentCheckData(Long courseId, String courseName, UUID studentId, boolean enrolled, String enrollmentStatus) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.studentId = studentId;
            this.enrolled = enrolled;
            this.enrollmentStatus = enrollmentStatus;
        }

        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }

        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }

        public UUID getStudentId() { return studentId; }
        public void setStudentId(UUID studentId) { this.studentId = studentId; }

        public boolean isEnrolled() { return enrolled; }
        public void setEnrolled(boolean enrolled) { this.enrolled = enrolled; }

        public String getEnrollmentStatus() { return enrollmentStatus; }
        public void setEnrollmentStatus(String enrollmentStatus) { this.enrollmentStatus = enrollmentStatus; }
    }
}
