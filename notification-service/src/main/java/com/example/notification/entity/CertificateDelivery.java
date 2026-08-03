package com.example.notification.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificate_delivery")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDelivery {

    @Id
    private UUID id;

    private UUID notificationId;
    private UUID userId;

    private String channel;

    // QUEUED, SENT, FAILED, READ
    private String status;

    private LocalDateTime attemptedAt;
}