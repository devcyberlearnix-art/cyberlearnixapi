package com.example.notification.dto;


import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class SystemHealthResponse {

    private Instant timestamp;
    private String status;
    private String service;
    private String version;
    private String uptime;
    private Map<String, String> checks;
}
