package com.example.admin.controller.cms;

import com.example.admin.dto.ApiResponse;
import com.example.admin.entity.cms.*;
import com.example.admin.service.cms.CmsAuxiliaryService;
import com.example.admin.service.cms.CmsMediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class CmsAuxiliaryAdminController {

    private final CmsAuxiliaryService auxiliaryService;
    private final CmsMediaService mediaService;

    // ============================================================
    // TESTIMONIALS
    // ============================================================

    @GetMapping("/testimonials")
    public ApiResponse<List<CmsTestimonial>> getTestimonials() {
        return new ApiResponse<>(true, "Testimonials retrieved successfully", auxiliaryService.getAdminTestimonials(), LocalDateTime.now().toString());
    }

    @PostMapping("/testimonials")
    public ApiResponse<CmsTestimonial> createTestimonial(@RequestBody CmsTestimonial testimonial) {
        return new ApiResponse<>(true, "Testimonial created successfully", auxiliaryService.createTestimonial(testimonial), LocalDateTime.now().toString());
    }

    @PutMapping("/testimonials/{id}")
    public ApiResponse<CmsTestimonial> updateTestimonial(@PathVariable UUID id, @RequestBody CmsTestimonial testimonial) {
        return new ApiResponse<>(true, "Testimonial updated successfully", auxiliaryService.updateTestimonial(id, testimonial), LocalDateTime.now().toString());
    }

    @DeleteMapping("/testimonials/{id}")
    public ApiResponse<?> deleteTestimonial(@PathVariable UUID id) {
        auxiliaryService.deleteTestimonial(id);
        return new ApiResponse<>(true, "Testimonial deleted successfully", null, LocalDateTime.now().toString());
    }

    // ============================================================
    // WEBSITE STATISTICS
    // ============================================================

    @GetMapping("/website-stats")
    public ApiResponse<List<CmsWebsiteStatistic>> getStatistics() {
        return new ApiResponse<>(true, "Website statistics retrieved successfully", auxiliaryService.getAdminStatistics(), LocalDateTime.now().toString());
    }

    @PutMapping("/website-stats")
    public ApiResponse<List<CmsWebsiteStatistic>> updateStatistics(@RequestBody List<CmsWebsiteStatistic> statistics) {
        return new ApiResponse<>(true, "Website statistics updated successfully", auxiliaryService.updateStatistics(statistics), LocalDateTime.now().toString());
    }

    // ============================================================
    // FOOTER MANAGEMENT
    // ============================================================

    @GetMapping("/footer")
    public ApiResponse<Map<String, Object>> getFooter() {
        return new ApiResponse<>(true, "Footer retrieved successfully", auxiliaryService.getAdminFooter(), LocalDateTime.now().toString());
    }

    @PutMapping("/footer")
    public ApiResponse<Map<String, Object>> updateFooter(@RequestBody Map<String, Object> footerData) {
        return new ApiResponse<>(true, "Footer updated successfully", auxiliaryService.updateFooter(footerData), LocalDateTime.now().toString());
    }

    // ============================================================
    // SITE SETTINGS
    // ============================================================

    @GetMapping("/site-settings")
    public ApiResponse<Map<String, String>> getSiteSettings() {
        return new ApiResponse<>(true, "Site settings retrieved successfully", auxiliaryService.getAdminSiteSettings(), LocalDateTime.now().toString());
    }

    @PutMapping("/site-settings")
    public ApiResponse<Map<String, String>> updateSiteSettings(@RequestBody Map<String, String> settings) {
        return new ApiResponse<>(true, "Site settings updated successfully", auxiliaryService.updateSiteSettings(settings), LocalDateTime.now().toString());
    }

    @PatchMapping("/site-settings/{key}")
    public ApiResponse<CmsSiteSetting> updateSetting(
            @PathVariable String key,
            @RequestBody Map<String, String> body) {
        String value = body != null ? body.get("value") : "";
        return new ApiResponse<>(true, "Setting updated successfully", auxiliaryService.updateSetting(key, value), LocalDateTime.now().toString());
    }

    // ============================================================
    // SEO METADATA
    // ============================================================

    @GetMapping("/seo")
    public ApiResponse<List<CmsSeoMetadata>> getSeoMetadata() {
        return new ApiResponse<>(true, "SEO metadata list retrieved successfully", auxiliaryService.getAdminSeo(), LocalDateTime.now().toString());
    }

    @PutMapping("/seo/{pageKey}")
    public ApiResponse<CmsSeoMetadata> updateSeoMetadata(
            @PathVariable String pageKey,
            @RequestBody CmsSeoMetadata metadata) {
        return new ApiResponse<>(true, "SEO metadata updated successfully", auxiliaryService.updateSeo(pageKey, metadata), LocalDateTime.now().toString());
    }

    // ============================================================
    // MEDIA LIBRARY
    // ============================================================

    @PostMapping("/media/upload")
    public ApiResponse<CmsMediaAsset> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "altText", required = false) String altText,
            Authentication authentication) {
        String adminEmail = authentication != null ? authentication.getName() : "ADMIN";
        CmsMediaAsset asset = mediaService.uploadMedia(file, altText, adminEmail);
        return new ApiResponse<>(true, "Media uploaded successfully", asset, LocalDateTime.now().toString());
    }

    @GetMapping("/media")
    public ApiResponse<Page<CmsMediaAsset>> getMediaLibrary(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return new ApiResponse<>(true, "Media library retrieved successfully", mediaService.getMediaLibrary(page, size), LocalDateTime.now().toString());
    }

    @DeleteMapping("/media/{id}")
    public ApiResponse<?> deleteMedia(@PathVariable UUID id) {
        mediaService.deleteMedia(id);
        return new ApiResponse<>(true, "Media asset deleted successfully", null, LocalDateTime.now().toString());
    }
}

