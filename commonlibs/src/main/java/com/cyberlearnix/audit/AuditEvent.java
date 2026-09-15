package com.cyberlearnix.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    private UUID eventId;
    private String eventType;
    private String userId;
    private String service;
    private String action;
    private String resource;
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;
    private String status; // SUCCESS, FAILURE
}