package com.example.admin.service.cms;

import com.example.admin.entity.cms.CmsNavigationItem;
import com.example.admin.repository.cms.CmsNavigationItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CmsNavigationService {

    private final CmsNavigationItemRepository repository;

    public List<CmsNavigationItem> getPublicNavigation() {
        List<CmsNavigationItem> items = repository.findByIsVisibleTrueOrderByDisplayOrderAsc();
        if (items.isEmpty()) {
            return bootstrapDefaultItems();
        }
        return items;
    }

    public List<CmsNavigationItem> getAdminNavigation() {
        List<CmsNavigationItem> items = repository.findAllByOrderByDisplayOrderAsc();
        if (items.isEmpty()) {
            return bootstrapDefaultItems();
        }
        return items;
    }

    @Transactional
    public CmsNavigationItem createNavItem(CmsNavigationItem item) {
        if (item.getDisplayOrder() == null || item.getDisplayOrder() == 0) {
            item.setDisplayOrder((int) repository.count());
        }
        return repository.save(item);
    }

    @Transactional
    public CmsNavigationItem updateNavItem(UUID id, CmsNavigationItem updated) {
        CmsNavigationItem existing = repository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Navigation item not found: " + id));

        if (updated.getLabel() != null) existing.setLabel(updated.getLabel());
        if (updated.getDestination() != null) existing.setDestination(updated.getDestination());
        if (updated.getItemType() != null) existing.setItemType(updated.getItemType());
        if (updated.getDisplayOrder() != null) existing.setDisplayOrder(updated.getDisplayOrder());
        if (updated.getIsVisible() != null) existing.setIsVisible(updated.getIsVisible());
        if (updated.getOpenInNewTab() != null) existing.setOpenInNewTab(updated.getOpenInNewTab());
        if (updated.getRequiresAuth() != null) existing.setRequiresAuth(updated.getRequiresAuth());
        if (updated.getParentId() != null) existing.setParentId(updated.getParentId());

        return repository.save(existing);
    }

    @Transactional
    public void deleteNavItem(UUID id) {
        repository.deleteById(id);
    }

    @Transactional
    public List<CmsNavigationItem> reorderNavItems(List<Object> order) {
        if (order == null || order.isEmpty()) {
            return repository.findAllByOrderByDisplayOrderAsc();
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
                repository.findById(id).ifPresent(item -> {
                    item.setDisplayOrder(finalI);
                    repository.save(item);
                });
            }
        }

        return repository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional
    public List<CmsNavigationItem> bootstrapDefaultItems() {
        List<CmsNavigationItem> defaults = List.of(
                CmsNavigationItem.builder().label("Explore").destination("/courses").itemType("LINK").displayOrder(0).isVisible(true).build(),
                CmsNavigationItem.builder().label("Certifications").destination("/certifications").itemType("LINK").displayOrder(1).isVisible(true).build(),
                CmsNavigationItem.builder().label("Teach on FD_LMS").destination("/instructor/apply").itemType("LINK").displayOrder(2).isVisible(true).build()
        );
        return repository.saveAll(defaults);
    }
}

