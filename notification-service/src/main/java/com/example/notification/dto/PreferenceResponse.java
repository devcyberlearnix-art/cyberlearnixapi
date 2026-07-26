package com.example.notification.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreferenceResponse {

    private boolean emailEnabled;
    private boolean pushEnabled;
    private boolean inAppEnabled;

}
