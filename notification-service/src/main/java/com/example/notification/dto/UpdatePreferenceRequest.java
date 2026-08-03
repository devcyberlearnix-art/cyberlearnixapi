package com.example.notification.dto;


import lombok.Data;

@Data
public class UpdatePreferenceRequest {

    private Boolean emailEnabled;
    private Boolean pushEnabled;
    private Boolean inAppEnabled;


}

