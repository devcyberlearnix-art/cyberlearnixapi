package com.lms.courseservice.dto;

public class CreateLectureResponse {
    private boolean success;
    private String message;
    private LectureInfo data;

    public CreateLectureResponse() {}

    public CreateLectureResponse(boolean success, String message, LectureInfo data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LectureInfo getData() { return data; }
    public void setData(LectureInfo data) { this.data = data; }

    public static class LectureInfo {
        private Long lectureId;
        private String title;
        private String description;
        private String videoUrl;
        private Integer duration;
        private Integer orderIndex;
        private Boolean previewEnabled;
        private String resources;
        private Long sectionId;
        private String sectionName;
        private Long courseId;

        public LectureInfo() {}

        public LectureInfo(Long lectureId, String title, String description, String videoUrl,
                           Integer duration, Integer orderIndex, Boolean previewEnabled,
                           String resources, Long sectionId, String sectionName, Long courseId) {
            this.lectureId = lectureId;
            this.title = title;
            this.description = description;
            this.videoUrl = videoUrl;
            this.duration = duration;
            this.orderIndex = orderIndex;
            this.previewEnabled = previewEnabled;
            this.resources = resources;
            this.sectionId = sectionId;
            this.sectionName = sectionName;
            this.courseId = courseId;
        }

        public Long getLectureId() { return lectureId; }
        public void setLectureId(Long lectureId) { this.lectureId = lectureId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getVideoUrl() { return videoUrl; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

        public Integer getDuration() { return duration; }
        public void setDuration(Integer duration) { this.duration = duration; }

        public Integer getOrderIndex() { return orderIndex; }
        public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

        public Boolean getPreviewEnabled() { return previewEnabled; }
        public void setPreviewEnabled(Boolean previewEnabled) { this.previewEnabled = previewEnabled; }

        public String getResources() { return resources; }
        public void setResources(String resources) { this.resources = resources; }

        public Long getSectionId() { return sectionId; }
        public void setSectionId(Long sectionId) { this.sectionId = sectionId; }

        public String getSectionName() { return sectionName; }
        public void setSectionName(String sectionName) { this.sectionName = sectionName; }

        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
    }
}
