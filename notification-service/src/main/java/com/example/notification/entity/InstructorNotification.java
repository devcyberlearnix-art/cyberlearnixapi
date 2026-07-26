package com.example.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "instructor_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorNotification {

    @Id
    private UUID id;

    private UUID instructorId;

    private String title;
    private String message;

    private String status;

    private LocalDateTime createdAt;

    @ElementCollection
    @CollectionTable(name = "instructor_notification_channels",
            joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "channel")
    private List<String> channels;
}