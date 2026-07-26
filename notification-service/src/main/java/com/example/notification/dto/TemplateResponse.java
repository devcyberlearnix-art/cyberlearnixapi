package com.example.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TemplateResponse {

    private String templateId;
    private String name;
    private String title;
    private String content;
    private String channel;
    private boolean active;
    private LocalDateTime createdAt;
}