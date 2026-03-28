package com.cyberlearnix.commonlibs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "banners")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String imageUrl;

    @Column
    private String ctaText;

    @Column
    private String ctaUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BannerType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BannerPosition position;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column
    private LocalDateTime startDate;

    @Column
    private LocalDateTime endDate;

    @Column
    private String targetAudience;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum BannerType {
        PROMOTIONAL,
        SEASONAL,
        CATEGORY,
        BRAND,
        OFFER,
        NEW_ARRIVAL
    }

    public enum BannerPosition {
        TOP_SLIDER,
        MIDDLE_SECTION,
        BOTTOM_SECTION,
        CATEGORY_HEADER,
        SIDEBAR
    }

    // Helper method to check if banner is currently active
    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && 
               (startDate == null || startDate.isBefore(now) || startDate.isEqual(now)) &&
               (endDate == null || endDate.isAfter(now));
    }
}
