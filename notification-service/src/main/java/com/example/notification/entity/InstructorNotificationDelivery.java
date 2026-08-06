package com.example.notification.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "instructor_notification_delivery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorNotificationDelivery {

    @Id
    private UUID id;

    private UUID notificationId;
    private UUID userId;

    private String channel;

    private String status; // QUEUED, SENT, FAILED, READ

    private LocalDateTime attemptedAt;
}