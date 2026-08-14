package com.lms.courseservice.dto;

import java.time.Instant;
import java.util.List;

public class GetPreviewResponse {

    private boolean success;
    private String message;
    private String timestamp;
    private int totalPreviews;
    private List<PreviewInfo> data;

    public GetPreviewResponse() {}

    public GetPreviewResponse(boolean success, String message, List<PreviewInfo> data) {
        this.success = success;
        this.message = message;
        this.timestamp = Instant.now().toString();
        this.totalPreviews = data != null ? data.size() : 0;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public int getTotalPreviews() { return totalPreviews; }
    public void setTotalPreviews(int totalPreviews) { this.totalPreviews = totalPreviews; }

    public List<PreviewInfo> getData() { return data; }
    public void setData(List<PreviewInfo> data) { this.data = data; }
}
