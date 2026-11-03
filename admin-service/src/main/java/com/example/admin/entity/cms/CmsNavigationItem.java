package com.example.admin.entity.cms;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cms_navigation_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsNavigationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(nullable = false, length = 255)
    private String destination;

    @Column(name = "item_type", nullable = false, length = 20)
    @Builder.Default
    private String itemType = "LINK"; // LINK, BUTTON, DROPDOWN

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    private Boolean isVisible = true;

    @Column(name = "open_in_new_tab", nullable = false)
    @Builder.Default
    private Boolean openInNewTab = false;

    @Column(name = "requires_auth", nullable = false)
    @Builder.Default
    private Boolean requiresAuth = false;

    @Column(name = "parent_id")
    private UUID parentId;

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
