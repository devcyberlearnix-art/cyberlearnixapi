package com.example.admin.entity.cms;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "cms_footer_links")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsFooterLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "group_name", nullable = false, length = 50)
    private String groupName;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(nullable = false, length = 255)
    private String destination;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    private Boolean isVisible = true;

    @Column(name = "open_in_new_tab", nullable = false)
    @Builder.Default
    private Boolean openInNewTab = false;
}

