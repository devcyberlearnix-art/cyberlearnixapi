package com.lms.courseservice.dto;

public class PreviewInfo {
    private Long previewId;
    private String title;
    private String videoUrl;
    private Integer duration;
    private Long courseId;
    private String courseName;

    public PreviewInfo() {}

    public PreviewInfo(Long previewId, String title, String videoUrl, Integer duration, Long courseId, String courseName) {
        this.previewId = previewId;
        this.title = title;
        this.videoUrl = videoUrl;
        this.duration = duration;
        this.courseId = courseId;
        this.courseName = courseName;
    }

    public Long getPreviewId() { return previewId; }
    public void setPreviewId(Long previewId) { this.previewId = previewId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
}
