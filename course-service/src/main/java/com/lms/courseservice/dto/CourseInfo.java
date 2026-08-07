package com.lms.courseservice.dto;

public class CourseInfo {
    private Long courseId;
    private String courseName;
    private String description;
    private String category;
    private String instructorName;
    private boolean deleted;

    public CourseInfo() {}

    public CourseInfo(Long courseId, String courseName, String description, String category, String instructorName, boolean deleted) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.description = description;
        this.category = category;
        this.instructorName = instructorName;
        this.deleted = deleted;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
