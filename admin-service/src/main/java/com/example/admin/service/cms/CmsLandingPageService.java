package com.example.admin.service.cms;

import com.example.admin.dto.cms.*;
import com.example.admin.entity.cms.*;
import com.example.admin.repository.cms.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CmsLandingPageService {

    private final LandingPageRepository landingPageRepository;
    private final LandingPageRevisionRepository revisionRepository;
    private final CmsNavigationItemRepository navigationItemRepository;
    private final CmsSectionRepository sectionRepository;
    private final CmsSiteSettingRepository siteSettingRepository;
    private final CmsTestimonialRepository testimonialRepository;
    private final CmsWebsiteStatisticRepository statisticRepository;
    private final CmsFooterLinkRepository footerLinkRepository;
    private final CmsSeoMetadataRepository seoMetadataRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PAGE_KEY = "HOME_PAGE";
    private static final String CACHE_KEY = "cms:landing_page:published";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    /**
     * Fetch the published landing page configuration.
     * Checks Redis first. If not cached, reads from database, updates Redis, and returns.
     */
    public Map<String, Object> getPublishedLandingPage() {
        // 1. Try cache
        try {
            String cached = redisTemplate.opsForValue().get(CACHE_KEY);
            if (cached != null && !cached.isBlank()) {
                log.debug("CMS Landing Page fetched from Redis cache");
                return objectMapper.readValue(cached, new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis read failed for {}: {}", CACHE_KEY, e.getMessage());
        }

        // 2. Fetch from DB
        LandingPage page = getOrCreateLandingPage();
        Map<String, Object> payload;

        if (page.getPublishedSnapshot() != null && !page.getPublishedSnapshot().isBlank()) {
            try {
                payload = objectMapper.readValue(page.getPublishedSnapshot(), new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                log.error("Failed to deserialize publishedSnapshot: {}", e.getMessage());
                payload = buildLiveSnapshot(true);
            }
        } else {
            payload = buildLiveSnapshot(true);
        }

        // 3. Write to cache
        try {
            redisTemplate.opsForValue().set(CACHE_KEY, objectMapper.writeValueAsString(payload), CACHE_TTL);
        } catch (Exception e) {
            log.warn("Redis write failed for {}: {}", CACHE_KEY, e.getMessage());
        }

        return payload;
    }

    /**
     * Fetch the current working draft.
     */
    public Map<String, Object> getDraftLandingPage() {
        LandingPage page = getOrCreateLandingPage();
        Map<String, Object> live = buildLiveSnapshot(false);
        if (page.getDraftSnapshot() != null && !page.getDraftSnapshot().isBlank()) {
            try {
                Map<String, Object> stored = objectMapper.readValue(page.getDraftSnapshot(), new TypeReference<Map<String, Object>>() {});
                if (stored.containsKey("hero")) {
                    live.put("hero", stored.get("hero"));
                }
                if (stored.containsKey("theme")) {
                    live.put("theme", stored.get("theme"));
                }
            } catch (Exception e) {
                log.error("Failed to parse draftSnapshot: {}", e.getMessage());
            }
        }
        return live;
    }

    /**
     * Update the draft landing page configuration.
     */
    @Transactional
    public Map<String, Object> updateDraftLandingPage(Map<String, Object> data) {
        LandingPage page = getOrCreateLandingPage();
        try {
            String json = objectMapper.writeValueAsString(data);
            page.setDraftSnapshot(json);
            landingPageRepository.save(page);
            log.info("CMS Landing Page draft updated successfully");
            return data;
        } catch (Exception e) {
            log.error("Error updating draft: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update draft: " + e.getMessage());
        }
    }

    /**
     * Publish the current draft to production.
     */
    @Transactional
    public Map<String, Object> publishLandingPage(String changeSummary, String adminEmail) {
        LandingPage page = getOrCreateLandingPage();
        Map<String, Object> draft = getDraftLandingPage();

        try {
            int nextVersion = (page.getPublishedVersion() != null ? page.getPublishedVersion() : 0) + 1;

            // Build public published snapshot with visible items and draft hero/theme
            Map<String, Object> publishedPayload = buildLiveSnapshot(true);
            if (draft.containsKey("hero")) {
                publishedPayload.put("hero", draft.get("hero"));
            }
            if (draft.containsKey("theme")) {
                publishedPayload.put("theme", draft.get("theme"));
            }
            publishedPayload.put("publishedVersion", nextVersion);
            publishedPayload.put("publishedAt", LocalDateTime.now().toString());
            publishedPayload.put("pageStatus", "PUBLISHED");

            String publishedJson = objectMapper.writeValueAsString(publishedPayload);
            String draftJson = objectMapper.writeValueAsString(draft);

            // Archive to revisions
            LandingPageRevision revision = LandingPageRevision.builder()
                    .pageKey(PAGE_KEY)
                    .versionNumber(nextVersion)
                    .snapshotData(publishedJson)
                    .changeSummary(changeSummary != null && !changeSummary.isBlank() ? changeSummary : "Published version " + nextVersion)
                    .publishedBy(adminEmail != null ? adminEmail : "ADMIN")
                    .publishedAt(LocalDateTime.now())
                    .build();
            revisionRepository.save(revision);

            // Update main page record
            page.setPublishedSnapshot(publishedJson);
            page.setPublishedVersion(nextVersion);
            page.setPublishedAt(LocalDateTime.now());
            page.setPageStatus("PUBLISHED");
            landingPageRepository.save(page);

            // Invalidate Redis cache
            try {
                redisTemplate.delete(CACHE_KEY);
                log.info("Redis cache evicted for {}", CACHE_KEY);
            } catch (Exception e) {
                log.warn("Failed to evict Redis cache: {}", e.getMessage());
            }

            log.info("CMS Landing Page published successfully. Version: {}", nextVersion);
            return publishedPayload;
        } catch (Exception e) {
            log.error("Publishing error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish landing page: " + e.getMessage());
        }
    }

    /**
     * Unpublish the landing page.
     */
    @Transactional
    public Map<String, Object> unpublishLandingPage() {
        LandingPage page = getOrCreateLandingPage();
        page.setPageStatus("DRAFT");
        landingPageRepository.save(page);

        try {
            redisTemplate.delete(CACHE_KEY);
        } catch (Exception e) {
            log.warn("Cache eviction failed: {}", e.getMessage());
        }

        return Map.of("success", true, "message", "Landing page unpublished successfully. Status is now DRAFT.");
    }

    /**
     * Preview draft content without publishing.
     */
    public Map<String, Object> getPreview() {
        Map<String, Object> draft = getDraftLandingPage();
        draft.put("isPreview", true);
        return draft;
    }

    /**
     * List all revision records.
     */
    public List<LandingPageRevision> getRevisions() {
        return revisionRepository.findByPageKeyOrderByVersionNumberDesc(PAGE_KEY);
    }

    /**
     * Get a single revision by ID.
     */
    public LandingPageRevision getRevision(UUID id) {
        return revisionRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Revision not found: " + id));
    }

    /**
     * Restore a previous revision as the current working draft.
     */
    @Transactional
    public Map<String, Object> restoreRevision(UUID id, String adminEmail) {
        LandingPageRevision rev = getRevision(id);
        LandingPage page = getOrCreateLandingPage();

        page.setDraftSnapshot(rev.getSnapshotData());
        landingPageRepository.save(page);

        log.info("Restored revision {} (version {}) as active draft by {}", id, rev.getVersionNumber(), adminEmail);
        return getDraftLandingPage();
    }

    /**
     * Find or bootstrap the landing page record.
     */
    public synchronized LandingPage getOrCreateLandingPage() {
        return landingPageRepository.findByPageKey(PAGE_KEY).orElseGet(() -> {
            log.info("Initializing default CMS Landing Page record in database...");
            Map<String, Object> initial = buildLiveSnapshot(false);
            try {
                String json = objectMapper.writeValueAsString(initial);
                LandingPage page = LandingPage.builder()
                        .pageKey(PAGE_KEY)
                        .pageName("Main Landing Page")
                        .pageStatus("PUBLISHED")
                        .publishedVersion(1)
                        .publishedAt(LocalDateTime.now())
                        .publishedSnapshot(json)
                        .draftSnapshot(json)
                        .build();
                LandingPage saved = landingPageRepository.save(page);

                // Initial revision 1
                LandingPageRevision rev1 = LandingPageRevision.builder()
                        .pageKey(PAGE_KEY)
                        .versionNumber(1)
                        .snapshotData(json)
                        .changeSummary("System bootstrap initialization")
                        .publishedBy("SYSTEM")
                        .publishedAt(LocalDateTime.now())
                        .build();
                revisionRepository.save(rev1);

                return saved;
            } catch (Exception e) {
                log.error("Failed to bootstrap default landing page: {}", e.getMessage(), e);
                throw new RuntimeException("Bootstrap failed: " + e.getMessage());
            }
        });
    }

    /**
     * Build snapshot aggregating all active sections, settings, and navigation.
     */
    public Map<String, Object> buildLiveSnapshot(boolean onlyVisible) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("pageKey", PAGE_KEY);
        snapshot.put("pageName", "Main Landing Page");
        snapshot.put("pageStatus", "PUBLISHED");
        snapshot.put("publishedVersion", 1);
        snapshot.put("publishedAt", LocalDateTime.now().toString());

        // Default Hero matching design system specification
        HeroDto hero = HeroDto.builder()
                .badgeText("Architecture Curriculum")
                .verificationLabel("Verified Production Track")
                .heading("Master Engineering On Real Architecture.")
                .headingEmphasis("On Real Architecture.")
                .description("Zero synthetic boilerplate. Learn distributed microservices, fullstack design systems, security verification, and cloud-native resilience directly from verified engineering leads.")
                .primaryCta(CtaDto.builder().label("Initialize Portal Matrix (Get Started)").destination("/register").visible(true).build())
                .secondaryCta(CtaDto.builder().label("Access Authenticated Node").destination("/login").visible(true).build())
                .searchPlaceholder("Search tracks by topic, distributed architecture, or lead...")
                .searchVisible(true)
                .textAlignment("center")
                .sectionVisible(true)
                .build();
        snapshot.put("hero", hero);

        // Navigation
        if (navigationItemRepository.count() == 0) {
            navigationItemRepository.saveAll(List.of(
                    CmsNavigationItem.builder().label("Explore").destination("/courses").itemType("LINK").displayOrder(0).isVisible(true).build(),
                    CmsNavigationItem.builder().label("Certifications").destination("/certifications").itemType("LINK").displayOrder(1).isVisible(true).build(),
                    CmsNavigationItem.builder().label("Teach on FD_LMS").destination("/instructor/apply").itemType("LINK").displayOrder(2).isVisible(true).build()
            ));
        }
        List<CmsNavigationItem> navItems = onlyVisible
                ? navigationItemRepository.findByIsVisibleTrueOrderByDisplayOrderAsc()
                : navigationItemRepository.findAllByOrderByDisplayOrderAsc();
        snapshot.put("navigation", navItems);

        // Sections
        if (sectionRepository.count() == 0) {
            sectionRepository.saveAll(List.of(
                    CmsSection.builder().pageKey(PAGE_KEY).sectionKey("HERO").sectionType("HERO").title("Hero Section").contentJson("{}").displayOrder(0).isVisible(true).build(),
                    CmsSection.builder().pageKey(PAGE_KEY).sectionKey("CATALOG").sectionType("FEATURED_COURSES").title("Explore Live Catalog").contentJson("{}").displayOrder(1).isVisible(true).build(),
                    CmsSection.builder().pageKey(PAGE_KEY).sectionKey("BANNER").sectionType("BANNER").title("Promotional Banners").contentJson("{}").displayOrder(2).isVisible(true).build(),
                    CmsSection.builder().pageKey(PAGE_KEY).sectionKey("STATS").sectionType("STATISTICS").title("Platform Telemetry").contentJson("{}").displayOrder(3).isVisible(true).build(),
                    CmsSection.builder().pageKey(PAGE_KEY).sectionKey("FOOTER").sectionType("FOOTER").title("Footer").contentJson("{}").displayOrder(4).isVisible(true).build()
            ));
        }
        List<CmsSection> sections = onlyVisible
                ? sectionRepository.findByPageKeyAndIsVisibleTrueOrderByDisplayOrderAsc(PAGE_KEY)
                : sectionRepository.findByPageKeyOrderByDisplayOrderAsc(PAGE_KEY);
        snapshot.put("sections", sections);

        // Statistics
        if (statisticRepository.count() == 0) {
            statisticRepository.saveAll(List.of(
                    CmsWebsiteStatistic.builder().label("Active Tracks").value("24+").subtext("Production architectures").displayOrder(0).isVisible(true).build(),
                    CmsWebsiteStatistic.builder().label("Enrolled Engineers").value("10,000+").subtext("Across top tech firms").displayOrder(1).isVisible(true).build(),
                    CmsWebsiteStatistic.builder().label("Average Rating").value("4.9").subtext("Verified learner reviews").displayOrder(2).isVisible(true).build(),
                    CmsWebsiteStatistic.builder().label("Lead Instructors").value("12").subtext("Principal engineers").displayOrder(3).isVisible(true).build()
            ));
        }
        List<CmsWebsiteStatistic> stats = onlyVisible
                ? statisticRepository.findByIsVisibleTrueOrderByDisplayOrderAsc()
                : statisticRepository.findAllByOrderByDisplayOrderAsc();
        snapshot.put("statistics", stats);

        // Site Settings
        Map<String, String> settingsMap = new HashMap<>();
        siteSettingRepository.findAll().forEach(s -> settingsMap.put(s.getSettingKey(), s.getSettingValue()));
        if (!settingsMap.containsKey("brand.name")) settingsMap.put("brand.name", "FD_LMS");
        if (!settingsMap.containsKey("brand.tagline")) settingsMap.put("brand.tagline", "Architecture");
        if (!settingsMap.containsKey("brand.logoUrl")) settingsMap.put("brand.logoUrl", "");
        snapshot.put("siteSettings", settingsMap);

        // Footer
        Map<String, Object> footerData = new HashMap<>();
        footerData.put("brandDescription", "Engineering education built on actual distributed microservices architectures. No synthetic toys.");
        footerData.put("copyright", "© 2026 FD_LMS / Asivo Inc. All rights reserved.");
        footerData.put("links", footerLinkRepository.findByIsVisibleTrueOrderByGroupNameAscDisplayOrderAsc());
        snapshot.put("footer", footerData);

        // SEO
        seoMetadataRepository.findByPageKey(PAGE_KEY).ifPresentOrElse(
                seo -> snapshot.put("seo", seo),
                () -> snapshot.put("seo", Map.of(
                        "title", "FD_LMS — Master Engineering on Real Architecture",
                        "description", "Learn distributed microservices, design systems, and resilience from verified leads.",
                        "keywords", "microservices, spring boot, react, architecture, lms",
                        "robotsDirective", "index, follow"
                ))
        );

        return snapshot;
    }
}

