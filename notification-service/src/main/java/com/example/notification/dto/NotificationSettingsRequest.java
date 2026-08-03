package com.example.notification.dto;

import lombok.Data;

@Data
public class NotificationSettingsRequest {

    private Boolean emailEnabled;
    private Boolean pushEnabled;
    private Boolean smsEnabled;
}