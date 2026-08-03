package com.example.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "certificate_notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateNotification {

    @Id
    private UUID id;

    private Long courseId;
    private UUID certificateId;
    private UUID instructorId;

    private String title;
    private String message;

    private LocalDateTime issuedAt;
    private String status;

    @ElementCollection
    @CollectionTable(name = "certificate_channels",
            joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "channel")
    private List<String> channels;
}
