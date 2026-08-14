package com.lms.courseservice.dto;

public class UpdatePreviewRequest {

    private String title;
    private String videoUrl;
    private Integer duration;

    public UpdatePreviewRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}
