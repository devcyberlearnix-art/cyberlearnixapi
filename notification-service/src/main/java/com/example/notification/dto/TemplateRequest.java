package com.example.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TemplateRequest {

    @NotBlank(message = "Template name is required")
    private String name;   // e.g. WELCOME_EMAIL

    @NotBlank(message = "Title is required")
    private String title;  // e.g. Welcome to CyberLearnix

    @NotBlank(message = "Content is required")
    private String content;
    // e.g. "Hello {{name}}, welcome to {{platform}}"

    private String channel; // EMAIL / SMS / PUSH
}
