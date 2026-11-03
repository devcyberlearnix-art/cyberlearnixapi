package com.example.admin.entity.cms;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cms_seo_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsSeoMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "page_key", nullable = false, unique = true, length = 50)
    private String pageKey;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String keywords;

    @Column(name = "canonical_url", length = 500)
    private String canonicalUrl;

    @Column(name = "og_title", length = 200)
    private String ogTitle;

    @Column(name = "og_description", columnDefinition = "TEXT")
    private String ogDescription;

    @Column(name = "og_image", length = 500)
    private String ogImage;

    @Column(name = "robots_directive", length = 100)
    @Builder.Default
    private String robotsDirective = "index, follow";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
