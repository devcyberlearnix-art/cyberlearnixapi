package com.example.notification.entity;

import com.example.notification.enums.NotificationStatus;
import com.example.notification.enums.Priority;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    private String title;
    private String message;

    @ElementCollection
    @CollectionTable(name = "notification_users",
            joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "user_id")
    private List<UUID> userIds;

    @ElementCollection
    @CollectionTable(name = "notification_channels",
            joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "channel")
    private List<String> channels;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private boolean isDeleted;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
}