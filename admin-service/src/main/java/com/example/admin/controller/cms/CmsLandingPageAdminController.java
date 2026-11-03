package com.example.admin.controller.cms;

import com.example.admin.dto.ApiResponse;
import com.example.admin.dto.cms.PublishRequest;
import com.example.admin.entity.cms.LandingPageRevision;
import com.example.admin.service.cms.CmsLandingPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/landing-page")
@RequiredArgsConstructor
@Slf4j
public class CmsLandingPageAdminController {

    private final CmsLandingPageService landingPageService;

    /**
     * Get the current draft configuration.
     */
    @GetMapping({"", "/draft"})
    public ApiResponse<Map<String, Object>> getDraft() {
        Map<String, Object> draft = landingPageService.getDraftLandingPage();
        return new ApiResponse<>(true, "Draft landing page retrieved successfully", draft, LocalDateTime.now().toString());
    }

    /**
     * Update the draft configuration.
     */
    @PutMapping
    public ApiResponse<Map<String, Object>> updateDraft(@RequestBody Map<String, Object> data) {
        Map<String, Object> updated = landingPageService.updateDraftLandingPage(data);
        return new ApiResponse<>(true, "Draft landing page updated successfully", updated, LocalDateTime.now().toString());
    }

    /**
     * Publish the draft to production.
     */
    @PostMapping("/publish")
    public ApiResponse<Map<String, Object>> publishPage(
            @RequestBody(required = false) PublishRequest request,
            Authentication authentication) {
        String adminEmail = authentication != null ? authentication.getName() : "ADMIN";
        String summary = request != null ? request.getChangeSummary() : "Manual publish from admin console";
        Map<String, Object> published = landingPageService.publishLandingPage(summary, adminEmail);
        return new ApiResponse<>(true, "Landing page published successfully. Live website updated.", published, LocalDateTime.now().toString());
    }

    /**
     * Unpublish the landing page.
     */
    @PostMapping("/unpublish")
    public ApiResponse<?> unpublishPage() {
        return new ApiResponse<>(true, "Landing page unpublished successfully", landingPageService.unpublishLandingPage(), LocalDateTime.now().toString());
    }

    /**
     * Preview draft landing page.
     */
    @GetMapping("/preview")
    public ApiResponse<Map<String, Object>> getPreview() {
        return new ApiResponse<>(true, "Draft preview retrieved successfully", landingPageService.getPreview(), LocalDateTime.now().toString());
    }

    /**
     * List all revision records.
     */
    @GetMapping("/revisions")
    public ApiResponse<List<LandingPageRevision>> getRevisions() {
        return new ApiResponse<>(true, "Revision history retrieved successfully", landingPageService.getRevisions(), LocalDateTime.now().toString());
    }

    /**
     * Get a specific revision by ID.
     */
    @GetMapping("/revisions/{id}")
    public ApiResponse<LandingPageRevision> getRevision(@PathVariable UUID id) {
        return new ApiResponse<>(true, "Revision retrieved successfully", landingPageService.getRevision(id), LocalDateTime.now().toString());
    }

    /**
     * Restore a specific revision as active draft.
     */
    @PostMapping("/revisions/{id}/restore")
    public ApiResponse<Map<String, Object>> restoreRevision(
            @PathVariable UUID id,
            Authentication authentication) {
        String adminEmail = authentication != null ? authentication.getName() : "ADMIN";
        Map<String, Object> restored = landingPageService.restoreRevision(id, adminEmail);
        return new ApiResponse<>(true, "Revision restored successfully as active draft", restored, LocalDateTime.now().toString());
    }
}

