package com.example.admin.entity.cms;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cms_sections", indexes = {
    @Index(name = "idx_sec_page_key", columnList = "page_key"),
    @Index(name = "idx_sec_order", columnList = "page_key, display_order")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsSection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "page_key", nullable = false, length = 50)
    @Builder.Default
    private String pageKey = "HOME_PAGE";

    @Column(name = "section_key", nullable = false, length = 50)
    private String sectionKey;

    @Column(name = "section_type", nullable = false, length = 50)
    private String sectionType;

    @Column(length = 200)
    private String title;

    @Column(name = "content_json", columnDefinition = "TEXT", nullable = false)
    private String contentJson;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    private Boolean isVisible = true;

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
