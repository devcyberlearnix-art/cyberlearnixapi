package com.example.admin.controller.cms;

import com.example.admin.dto.ApiResponse;
import com.example.admin.dto.cms.ReorderRequest;
import com.example.admin.dto.cms.VisibilityRequest;
import com.example.admin.entity.cms.CmsSection;
import com.example.admin.service.cms.CmsSectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/landing-page/sections")
@RequiredArgsConstructor
@Slf4j
public class CmsSectionAdminController {

    private final CmsSectionService sectionService;

    @GetMapping
    public ApiResponse<List<CmsSection>> getSections() {
        return new ApiResponse<>(true, "Sections retrieved successfully", sectionService.getAdminSections(), LocalDateTime.now().toString());
    }

    @PostMapping
    public ApiResponse<CmsSection> createSection(@RequestBody CmsSection section) {
        return new ApiResponse<>(true, "Section created successfully", sectionService.createSection(section), LocalDateTime.now().toString());
    }

    @PutMapping("/{id}")
    public ApiResponse<CmsSection> updateSection(@PathVariable UUID id, @RequestBody CmsSection section) {
        return new ApiResponse<>(true, "Section updated successfully", sectionService.updateSection(id, section), LocalDateTime.now().toString());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteSection(@PathVariable UUID id) {
        sectionService.deleteSection(id);
        return new ApiResponse<>(true, "Section deleted successfully", null, LocalDateTime.now().toString());
    }

    @PatchMapping("/reorder")
    public ApiResponse<List<CmsSection>> reorderSections(@RequestBody ReorderRequest request) {
        List<CmsSection> reordered = sectionService.reorderSections(request != null ? request.getOrder() : List.of());
        return new ApiResponse<>(true, "Sections reordered successfully", reordered, LocalDateTime.now().toString());
    }

    @PatchMapping("/{id}/visibility")
    public ApiResponse<CmsSection> toggleSectionVisibility(
            @PathVariable UUID id,
            @RequestBody VisibilityRequest request) {
        boolean isVisible = request != null && Boolean.TRUE.equals(request.getIsVisible());
        return new ApiResponse<>(true, "Section visibility updated successfully", sectionService.toggleVisibility(id, isVisible), LocalDateTime.now().toString());
    }
}

