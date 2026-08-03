package com.example.notification.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "course_notification_delivery")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseNotificationDelivery {

    @Id
    private UUID id;

    private UUID notificationId;
    private UUID userId;

    private String channel;

    private String status; // QUEUED, SENT, FAILED, READ

    private LocalDateTime attemptedAt;
}