package com.example.instructorservice.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@JsonInclude(JsonInclude.Include.NON_NULL)

@Entity
@Table(name = "course") // 🔥 add this
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    public enum CourseStatus {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String slug;
    private String thumbnailUrl;
    private String previewVideoUrl;
    private String title;
    private String description;
    private Double price;
    private String category;
    private String subtitle;
    @Column(length = 50)
    private String language;  // e.g., "en"

    @Column(length = 50)
    private String level;     // e.g., "BEGINNER"
    @Enumerated(EnumType.STRING)
    private CourseStatus status;

    @ElementCollection
    private List<String> tags;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private LocalDateTime archivedAt;

    @ManyToOne
    @JoinColumn(name = "instructor_id", nullable = false, columnDefinition = "uuid")
    private Instructor instructor;

    // Course Service Integration
    @Column(name = "course_service_id")
    private Long courseServiceId; // Reference to course ID in Course Service (port 8083)

    @Column(name = "sync_status")
    private String syncStatus = "PENDING"; // PENDING, SYNCED, FAILED

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = CourseStatus.DRAFT;
        }
        
        if (this.syncStatus == null) {
            this.syncStatus = "PENDING";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}