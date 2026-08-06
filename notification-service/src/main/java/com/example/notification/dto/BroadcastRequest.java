package com.example.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class BroadcastRequest {

    private List<String> userIds; // optional (if null → send to all)

    private String templateName; // optional

    private Map<String, String> variables; // for {{name}}

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private String channel; // PUSH / EMAIL / SMS
}