package com.lms.courseservice.dto;

import java.util.List;

public class EnrolledStudentsResponse {
    private boolean success;
    private String message;
    private EnrolledStudentsData data;

    public EnrolledStudentsResponse() {}

    public EnrolledStudentsResponse(boolean success, String message, EnrolledStudentsData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public EnrolledStudentsData getData() { return data; }
    public void setData(EnrolledStudentsData data) { this.data = data; }

    public static class EnrolledStudentsData {
        private Long courseId;
        private String courseName;
        private int totalEnrolled;
        private List<EnrolledStudentInfo> students;

        public EnrolledStudentsData() {}

        public EnrolledStudentsData(Long courseId, String courseName, int totalEnrolled, List<EnrolledStudentInfo> students) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.totalEnrolled = totalEnrolled;
            this.students = students;
        }

        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }

        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }

        public int getTotalEnrolled() { return totalEnrolled; }
        public void setTotalEnrolled(int totalEnrolled) { this.totalEnrolled = totalEnrolled; }

        public List<EnrolledStudentInfo> getStudents() { return students; }
        public void setStudents(List<EnrolledStudentInfo> students) { this.students = students; }
    }
}
