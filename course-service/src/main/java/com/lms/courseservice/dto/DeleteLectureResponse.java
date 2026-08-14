package com.lms.courseservice.dto;

public class DeleteLectureResponse {
    private boolean success;
    private String message;
    private DeletedLectureInfo data;

    public DeleteLectureResponse() {}

    public DeleteLectureResponse(boolean success, String message, DeletedLectureInfo data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public DeletedLectureInfo getData() { return data; }
    public void setData(DeletedLectureInfo data) { this.data = data; }

    public static class DeletedLectureInfo {
        private Long lectureId;
        private String title;
        private Long sectionId;
        private boolean deleted;

        public DeletedLectureInfo() {}

        public DeletedLectureInfo(Long lectureId, String title, Long sectionId, boolean deleted) {
            this.lectureId = lectureId;
            this.title = title;
            this.sectionId = sectionId;
            this.deleted = deleted;
        }

        public Long getLectureId() { return lectureId; }
        public void setLectureId(Long lectureId) { this.lectureId = lectureId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public Long getSectionId() { return sectionId; }
        public void setSectionId(Long sectionId) { this.sectionId = sectionId; }

        public boolean isDeleted() { return deleted; }
        public void setDeleted(boolean deleted) { this.deleted = deleted; }
    }
}
