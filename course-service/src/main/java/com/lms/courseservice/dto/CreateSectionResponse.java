package com.lms.courseservice.dto;

public class CreateSectionResponse {
    private boolean success;
    private String message;
    private SectionInfo data;

    public CreateSectionResponse() {}

    public CreateSectionResponse(boolean success, String message, SectionInfo data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public SectionInfo getData() { return data; }
    public void setData(SectionInfo data) { this.data = data; }

    public static class SectionInfo {
        private Long sectionId;
        private String title;
        private Integer orderIndex;
        private Long courseId;
        private String courseName;

        public SectionInfo() {}

        public SectionInfo(Long sectionId, String title, Integer orderIndex, Long courseId, String courseName) {
            this.sectionId = sectionId;
            this.title = title;
            this.orderIndex = orderIndex;
            this.courseId = courseId;
            this.courseName = courseName;
        }

        public Long getSectionId() { return sectionId; }
        public void setSectionId(Long sectionId) { this.sectionId = sectionId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public Integer getOrderIndex() { return orderIndex; }
        public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }

        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
    }
}
