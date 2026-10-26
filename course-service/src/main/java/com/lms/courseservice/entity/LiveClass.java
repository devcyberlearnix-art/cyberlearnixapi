package com.lms.courseservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "live_classes", indexes = {
    @Index(name = "idx_live_classes_course_id", columnList = "course_id"),
    @Index(name = "idx_live_classes_section_id", columnList = "section_id"),
    @Index(name = "idx_live_classes_instructor_id", columnList = "instructor_id"),
    @Index(name = "idx_live_classes_scheduled_start_time", columnList = "scheduled_start_time"),
    @Index(name = "idx_live_classes_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "instructor_id", nullable = false)
    private UUID instructorId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "meeting_url", nullable = false, length = 1000)
    private String meetingUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_provider", nullable = false, length = 50)
    private MeetingProvider meetingProvider;

    @Column(name = "scheduled_start_time", nullable = false)
    private LocalDateTime scheduledStartTime;

    @Column(name = "scheduled_end_time", nullable = false)
    private LocalDateTime scheduledEndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private LiveClassStatus status = LiveClassStatus.SCHEDULED;

    @Column(name = "recording_url", length = 1000)
    private String recordingUrl;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    @JsonIgnore
    private Course course;

    @ManyToOne
    @JoinColumn(name = "section_id", insertable = false, updatable = false)
    @JsonIgnore
    private Section section;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum LiveClassStatus {
        SCHEDULED,
        LIVE,
        COMPLETED,
        CANCELLED
    }

    public enum MeetingProvider {
        ZOOM,
        GOOGLE_MEET,
        TEAMS,
        OTHER
    }
}
