package com.example.notification.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CertificateNotifyRequest {

    private Long courseId;
    private UUID certificateId;
    private UUID instructorId;

    private String title;
    private String message;

    private LocalDateTime issuedAt;

    private Boolean sendToAll;
    private List<UUID> userIds;

    private List<String> channels; // IN_APP, EMAIL, SMS
}
