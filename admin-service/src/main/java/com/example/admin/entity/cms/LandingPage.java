package com.example.admin.entity.cms;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "landing_pages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandingPage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "page_key", nullable = false, unique = true, length = 50)
    @Builder.Default
    private String pageKey = "HOME_PAGE";

    @Column(name = "page_name", nullable = false, length = 100)
    @Builder.Default
    private String pageName = "Main Landing Page";

    @Column(name = "page_status", nullable = false, length = 20)
    @Builder.Default
    private String pageStatus = "DRAFT"; // DRAFT, PUBLISHED

    @Column(name = "published_snapshot", columnDefinition = "TEXT")
    private String publishedSnapshot;

    @Column(name = "draft_snapshot", columnDefinition = "TEXT")
    private String draftSnapshot;

    @Column(name = "published_version")
    @Builder.Default
    private Integer publishedVersion = 1;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
