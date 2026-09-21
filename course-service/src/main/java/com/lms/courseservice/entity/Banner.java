package com.lms.courseservice.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;

@Entity
@Table(name = "banners",
       indexes = {
           @Index(name = "idx_banner_status", columnList = "status"),
           @Index(name = "idx_banner_display_order", columnList = "display_order"),
           @Index(name = "idx_banner_start_date", columnList = "start_date"),
           @Index(name = "idx_banner_end_date", columnList = "end_date"),
           @Index(name = "idx_banner_course_id", columnList = "course_id"),
           @Index(name = "idx_banner_target_type", columnList = "target_type"),
           @Index(name = "idx_banner_deleted_at", columnList = "deleted_at")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_banner_display_order", columnNames = {"display_order", "deleted_at"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE banners SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "mobile_image_url", length = 500)
    private String mobileImageUrl;

    @Column(name = "alt_text", length = 200)
    private String altText;

    @Column(nullable = false, length = 100)
    private String ctaText;

    @Column(nullable = false, length = 500)
    private String ctaUrl;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BannerStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 20, nullable = true)
    private BannerTargetType targetType;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    @Column(name = "impressions", nullable = false)
    @Builder.Default
    private Long impressions = 0L;

    @Column(name = "clicks", nullable = false)
    @Builder.Default
    private Long clicks = 0L;

    @Column(name = "conversions", nullable = true)
    @Builder.Default
    private Long conversions = 0L;

    @Column(name = "last_impression_at")
    private Instant lastImpressionAt;

    @Column(name = "last_click_at")
    private Instant lastClickAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public enum BannerStatus {
        ACTIVE,
        INACTIVE
    }

    public enum BannerTargetType {
        COURSE,
        CATEGORY,
        EXTERNAL_URL,
        CUSTOM_PAGE
    }
}
