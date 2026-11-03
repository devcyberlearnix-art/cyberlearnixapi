package com.example.admin.entity.cms;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "landing_page_revisions", indexes = {
    @Index(name = "idx_rev_page_key", columnList = "page_key"),
    @Index(name = "idx_rev_version", columnList = "page_key, version_number")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandingPageRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "page_key", nullable = false, length = 50)
    private String pageKey;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "snapshot_data", nullable = false, columnDefinition = "TEXT")
    private String snapshotData;

    @Column(name = "change_summary", length = 500)
    private String changeSummary;

    @Column(name = "published_by", length = 100)
    private String publishedBy;

    @Column(name = "published_at", nullable = false, updatable = false)
    private LocalDateTime publishedAt;
}

