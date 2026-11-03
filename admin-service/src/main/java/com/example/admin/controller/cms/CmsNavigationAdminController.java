package com.example.admin.controller.cms;

import com.example.admin.dto.ApiResponse;
import com.example.admin.dto.cms.ReorderRequest;
import com.example.admin.entity.cms.CmsNavigationItem;
import com.example.admin.service.cms.CmsNavigationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/navigation")
@RequiredArgsConstructor
@Slf4j
public class CmsNavigationAdminController {

    private final CmsNavigationService navigationService;

    @GetMapping
    public ApiResponse<List<CmsNavigationItem>> getNavigation() {
        return new ApiResponse<>(true, "Navigation items retrieved successfully", navigationService.getAdminNavigation(), LocalDateTime.now().toString());
    }

    @PostMapping
    public ApiResponse<CmsNavigationItem> createNavItem(@RequestBody CmsNavigationItem item) {
        return new ApiResponse<>(true, "Navigation item created successfully", navigationService.createNavItem(item), LocalDateTime.now().toString());
    }

    @PutMapping("/{id}")
    public ApiResponse<CmsNavigationItem> updateNavItem(@PathVariable UUID id, @RequestBody CmsNavigationItem item) {
        return new ApiResponse<>(true, "Navigation item updated successfully", navigationService.updateNavItem(id, item), LocalDateTime.now().toString());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteNavItem(@PathVariable UUID id) {
        navigationService.deleteNavItem(id);
        return new ApiResponse<>(true, "Navigation item deleted successfully", null, LocalDateTime.now().toString());
    }

    @PatchMapping("/reorder")
    public ApiResponse<List<CmsNavigationItem>> reorderNavItems(@RequestBody ReorderRequest request) {
        List<CmsNavigationItem> reordered = navigationService.reorderNavItems(request != null ? request.getOrder() : List.of());
        return new ApiResponse<>(true, "Navigation items reordered successfully", reordered, LocalDateTime.now().toString());
    }
}

