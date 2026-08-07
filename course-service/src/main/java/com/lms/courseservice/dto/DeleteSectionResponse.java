package com.lms.courseservice.dto;

public class DeleteSectionResponse {
    private boolean success;
    private String message;
    private SectionInfo data;

    public DeleteSectionResponse() {}

    public DeleteSectionResponse(boolean success, String message, SectionInfo data) {
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
        private Long courseId;
        private boolean deleted;

        public SectionInfo() {}

        public SectionInfo(Long sectionId, String title, Long courseId, boolean deleted) {
            this.sectionId = sectionId;
            this.title = title;
            this.courseId = courseId;
            this.deleted = deleted;
        }

        public Long getSectionId() { return sectionId; }
        public void setSectionId(Long sectionId) { this.sectionId = sectionId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }

        public boolean isDeleted() { return deleted; }
        public void setDeleted(boolean deleted) { this.deleted = deleted; }
    }
}
