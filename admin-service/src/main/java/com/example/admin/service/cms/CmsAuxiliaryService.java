package com.example.admin.service.cms;

import com.example.admin.entity.cms.*;
import com.example.admin.repository.cms.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CmsAuxiliaryService {

    private final CmsTestimonialRepository testimonialRepository;
    private final CmsWebsiteStatisticRepository statisticRepository;
    private final CmsFooterLinkRepository footerLinkRepository;
    private final CmsSiteSettingRepository siteSettingRepository;
    private final CmsSeoMetadataRepository seoMetadataRepository;

    // ============================================================
    // TESTIMONIALS
    // ============================================================

    public List<CmsTestimonial> getPublicTestimonials() {
        return testimonialRepository.findByIsVisibleTrueOrderByDisplayOrderAsc();
    }

    public List<CmsTestimonial> getAdminTestimonials() {
        return testimonialRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional
    public CmsTestimonial createTestimonial(CmsTestimonial t) {
        if (t.getDisplayOrder() == null) t.setDisplayOrder((int) testimonialRepository.count());
        return testimonialRepository.save(t);
    }

    @Transactional
    public CmsTestimonial updateTestimonial(UUID id, CmsTestimonial t) {
        CmsTestimonial existing = testimonialRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Testimonial not found: " + id));

        if (t.getAuthorName() != null) existing.setAuthorName(t.getAuthorName());
        if (t.getAuthorRole() != null) existing.setAuthorRole(t.getAuthorRole());
        if (t.getAvatarUrl() != null) existing.setAvatarUrl(t.getAvatarUrl());
        if (t.getContent() != null) existing.setContent(t.getContent());
        if (t.getRating() != null) existing.setRating(t.getRating());
        if (t.getCompany() != null) existing.setCompany(t.getCompany());
        if (t.getDisplayOrder() != null) existing.setDisplayOrder(t.getDisplayOrder());
        if (t.getIsVisible() != null) existing.setIsVisible(t.getIsVisible());

        return testimonialRepository.save(existing);
    }

    @Transactional
    public void deleteTestimonial(UUID id) {
        testimonialRepository.deleteById(id);
    }

    // ============================================================
    // STATISTICS
    // ============================================================

    public List<CmsWebsiteStatistic> getPublicStatistics() {
        return statisticRepository.findByIsVisibleTrueOrderByDisplayOrderAsc();
    }

    public List<CmsWebsiteStatistic> getAdminStatistics() {
        return statisticRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional
    public List<CmsWebsiteStatistic> updateStatistics(List<CmsWebsiteStatistic> stats) {
        if (stats == null) return getAdminStatistics();
        statisticRepository.deleteAll();
        for (int i = 0; i < stats.size(); i++) {
            stats.get(i).setDisplayOrder(i);
        }
        return statisticRepository.saveAll(stats);
    }

    // ============================================================
    // FOOTER
    // ============================================================

    public Map<String, Object> getPublicFooter() {
        Map<String, Object> map = new HashMap<>();
        map.put("brandDescription", "Engineering education built on actual distributed microservices architectures. No synthetic toys.");
        map.put("copyright", "© 2026 FD_LMS / Asivo Inc. All rights reserved.");
        map.put("links", footerLinkRepository.findByIsVisibleTrueOrderByGroupNameAscDisplayOrderAsc());
        return map;
    }

    public Map<String, Object> getAdminFooter() {
        Map<String, Object> map = new HashMap<>();
        map.put("brandDescription", "Engineering education built on actual distributed microservices architectures. No synthetic toys.");
        map.put("copyright", "© 2026 FD_LMS / Asivo Inc. All rights reserved.");
        map.put("links", footerLinkRepository.findAllByOrderByGroupNameAscDisplayOrderAsc());
        return map;
    }

    @Transactional
    public Map<String, Object> updateFooter(Map<String, Object> footerData) {
        if (footerData.containsKey("links")) {
            Object linksObj = footerData.get("links");
            if (linksObj instanceof List<?> list) {
                footerLinkRepository.deleteAll();
                List<CmsFooterLink> links = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) instanceof Map<?, ?> m) {
                        links.add(CmsFooterLink.builder()
                                .groupName(m.get("groupName") != null ? m.get("groupName").toString() : "General")
                                .label(m.get("label") != null ? m.get("label").toString() : "Link")
                                .destination(m.get("destination") != null ? m.get("destination").toString() : "/")
                                .displayOrder(i)
                                .isVisible(true)
                                .openInNewTab(Boolean.TRUE.equals(m.get("openInNewTab")))
                                .build());
                    }
                }
                footerLinkRepository.saveAll(links);
            }
        }
        return getAdminFooter();
    }

    // ============================================================
    // SITE SETTINGS
    // ============================================================

    public Map<String, String> getPublicSiteSettings() {
        Map<String, String> map = new HashMap<>();
        siteSettingRepository.findAll().forEach(s -> map.put(s.getSettingKey(), s.getSettingValue()));
        if (!map.containsKey("brand.name")) map.put("brand.name", "FD_LMS");
        if (!map.containsKey("brand.tagline")) map.put("brand.tagline", "Architecture");
        if (!map.containsKey("brand.logoUrl")) map.put("brand.logoUrl", "");
        return map;
    }

    public Map<String, String> getAdminSiteSettings() {
        return getPublicSiteSettings();
    }

    @Transactional
    public Map<String, String> updateSiteSettings(Map<String, String> settings) {
        if (settings != null) {
            settings.forEach(this::updateSetting);
        }
        return getAdminSiteSettings();
    }

    @Transactional
    public CmsSiteSetting updateSetting(String key, String value) {
        CmsSiteSetting setting = siteSettingRepository.findBySettingKey(key)
                .orElse(CmsSiteSetting.builder().settingKey(key).settingGroup("BRANDING").build());
        setting.setSettingValue(value);
        return siteSettingRepository.save(setting);
    }

    // ============================================================
    // SEO METADATA
    // ============================================================

    public CmsSeoMetadata getPublicSeo(String pageKey) {
        return seoMetadataRepository.findByPageKey(pageKey)
                .orElse(CmsSeoMetadata.builder()
                        .pageKey(pageKey)
                        .title("FD_LMS — Master Engineering on Real Architecture")
                        .description("Learn distributed microservices, design systems, and resilience from verified leads.")
                        .keywords("microservices, spring boot, react, architecture, lms")
                        .robotsDirective("index, follow")
                        .build());
    }

    public List<CmsSeoMetadata> getAdminSeo() {
        return seoMetadataRepository.findAll();
    }

    @Transactional
    public CmsSeoMetadata updateSeo(String pageKey, CmsSeoMetadata data) {
        CmsSeoMetadata seo = seoMetadataRepository.findByPageKey(pageKey)
                .orElse(CmsSeoMetadata.builder().pageKey(pageKey).build());

        if (data.getTitle() != null) seo.setTitle(data.getTitle());
        if (data.getDescription() != null) seo.setDescription(data.getDescription());
        if (data.getKeywords() != null) seo.setKeywords(data.getKeywords());
        if (data.getCanonicalUrl() != null) seo.setCanonicalUrl(data.getCanonicalUrl());
        if (data.getOgTitle() != null) seo.setOgTitle(data.getOgTitle());
        if (data.getOgDescription() != null) seo.setOgDescription(data.getOgDescription());
        if (data.getOgImage() != null) seo.setOgImage(data.getOgImage());
        if (data.getRobotsDirective() != null) seo.setRobotsDirective(data.getRobotsDirective());

        return seoMetadataRepository.save(seo);
    }
}

