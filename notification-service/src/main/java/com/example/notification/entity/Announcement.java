package com.example.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Entity
@Table(name = "announcements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement {

    @Id
    private UUID id;

    private String title;

    private String message;

    private Boolean sendToAll;

    @ElementCollection
    @CollectionTable(name = "announcement_users",
            joinColumns = @JoinColumn(name = "announcement_id"))
    @Column(name = "user_id")
    private List<UUID> userIds;

    // ✅ NEW: channels (dynamic instead of hardcoded)
    @ElementCollection
    @CollectionTable(name = "announcement_channels",
            joinColumns = @JoinColumn(name = "announcement_id"))
    @Column(name = "channel")
    private List<String> channels;

    private LocalDateTime scheduledAt;

    private LocalDateTime createdAt;

    // ✅ NEW: updated time
    private LocalDateTime updatedAt;

    // CREATED, SCHEDULED, SENT
    private String status;

    // ✅ NEW: priority (LOW, MEDIUM, HIGH)
    private String priority;

    private Long courseId;

    private UUID createdBy;

    @Builder.Default
    private Boolean active = true;
}