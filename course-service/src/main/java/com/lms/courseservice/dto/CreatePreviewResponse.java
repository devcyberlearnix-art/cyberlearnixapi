package com.lms.courseservice.dto;

public class CreatePreviewResponse {
    private boolean success;
    private String message;
    private PreviewInfo data;

    public CreatePreviewResponse() {}

    public CreatePreviewResponse(boolean success, String message, PreviewInfo data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public PreviewInfo getData() { return data; }
    public void setData(PreviewInfo data) { this.data = data; }
}
