package com.example.admin.service.cms;

import com.example.admin.entity.cms.CmsSection;
import com.example.admin.repository.cms.CmsSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CmsSectionService {

    private final CmsSectionRepository repository;
    private static final String PAGE_KEY = "HOME_PAGE";

    public List<CmsSection> getPublicSections() {
        List<CmsSection> list = repository.findByPageKeyAndIsVisibleTrueOrderByDisplayOrderAsc(PAGE_KEY);
        if (list.isEmpty()) {
            return bootstrapDefaultSections();
        }
        return list;
    }

    public List<CmsSection> getAdminSections() {
        List<CmsSection> list = repository.findByPageKeyOrderByDisplayOrderAsc(PAGE_KEY);
        if (list.isEmpty()) {
            return bootstrapDefaultSections();
        }
        return list;
    }

    @Transactional
    public CmsSection createSection(CmsSection section) {
        section.setPageKey(PAGE_KEY);
        if (section.getDisplayOrder() == null || section.getDisplayOrder() == 0) {
            section.setDisplayOrder((int) repository.count());
        }
        if (section.getContentJson() == null || section.getContentJson().isBlank()) {
            section.setContentJson("{}");
        }
        return repository.save(section);
    }

    @Transactional
    public CmsSection updateSection(UUID id, CmsSection updated) {
        CmsSection existing = repository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Section not found: " + id));

        if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
        if (updated.getSectionType() != null) existing.setSectionType(updated.getSectionType());
        if (updated.getContentJson() != null) existing.setContentJson(updated.getContentJson());
        if (updated.getDisplayOrder() != null) existing.setDisplayOrder(updated.getDisplayOrder());
        if (updated.getIsVisible() != null) existing.setIsVisible(updated.getIsVisible());

        return repository.save(existing);
    }

    @Transactional
    public void deleteSection(UUID id) {
        repository.deleteById(id);
    }

    @Transactional
    public CmsSection toggleVisibility(UUID id, boolean isVisible) {
        CmsSection section = repository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Section not found: " + id));
        section.setIsVisible(isVisible);
        return repository.save(section);
    }

    @Transactional
    public List<CmsSection> reorderSections(List<Object> order) {
        if (order == null || order.isEmpty()) {
            return repository.findByPageKeyOrderByDisplayOrderAsc(PAGE_KEY);
        }

        for (int i = 0; i < order.size(); i++) {
            Object obj = order.get(i);
            UUID id = null;
            if (obj instanceof String str) {
                try { id = UUID.fromString(str); } catch (Exception ignored) {}
            } else if (obj instanceof Map map && map.containsKey("id")) {
                try { id = UUID.fromString(map.get("id").toString()); } catch (Exception ignored) {}
            }

            if (id != null) {
                int finalI = i;
                repository.findById(id).ifPresent(sec -> {
                    sec.setDisplayOrder(finalI);
                    repository.save(sec);
                });
            }
        }

        return repository.findByPageKeyOrderByDisplayOrderAsc(PAGE_KEY);
    }

    @Transactional
    public List<CmsSection> bootstrapDefaultSections() {
        List<CmsSection> defaults = List.of(
                CmsSection.builder().pageKey(PAGE_KEY).sectionKey("HERO").sectionType("HERO").title("Hero Section").contentJson("{}").displayOrder(0).isVisible(true).build(),
                CmsSection.builder().pageKey(PAGE_KEY).sectionKey("CATALOG").sectionType("FEATURED_COURSES").title("Explore Live Catalog").contentJson("{}").displayOrder(1).isVisible(true).build(),
                CmsSection.builder().pageKey(PAGE_KEY).sectionKey("BANNER").sectionType("BANNER").title("Promotional Banners").contentJson("{}").displayOrder(2).isVisible(true).build(),
                CmsSection.builder().pageKey(PAGE_KEY).sectionKey("STATS").sectionType("STATISTICS").title("Platform Telemetry").contentJson("{}").displayOrder(3).isVisible(true).build(),
                CmsSection.builder().pageKey(PAGE_KEY).sectionKey("FOOTER").sectionType("FOOTER").title("Footer").contentJson("{}").displayOrder(4).isVisible(true).build()
        );
        return repository.saveAll(defaults);
    }
}

