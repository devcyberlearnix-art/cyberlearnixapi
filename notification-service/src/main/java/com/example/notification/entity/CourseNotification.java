package com.example.notification.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "course_notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Long courseId;

    private String title;
    private String message;

    private String status; // CREATED, SENT

    private LocalDateTime createdAt;

    @ElementCollection
    @CollectionTable(name = "course_notification_channels",
            joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "channel")
    private List<String> channels;
}

