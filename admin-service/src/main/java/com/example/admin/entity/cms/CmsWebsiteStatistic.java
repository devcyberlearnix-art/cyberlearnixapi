package com.example.admin.entity.cms;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cms_website_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsWebsiteStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(nullable = false, length = 50)
    private String value;

    @Column(length = 100)
    private String subtext;

    @Column(name = "icon_name", length = 50)
    private String iconName;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    private Boolean isVisible = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
