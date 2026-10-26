package com.example.admin.controller;

import com.example.admin.client.AdminCourseServiceClient;
import com.example.admin.dto.CourseListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Admin Banner Management Controller
 * Integrates with Course Service (Port 8083) via AdminCourseServiceClient
 * All bannerId parameters are Long (from Course Service)
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminBannerController {

    private final AdminCourseServiceClient courseServiceClient;

    public AdminBannerController(AdminCourseServiceClient courseServiceClient) {
        this.courseServiceClient = courseServiceClient;
    }

    /**
     * Create banner
     */
    @PostMapping("/banners")
    public CourseListResponse createBanner(@RequestBody Map<String, Object> bannerPayload) {
        if (bannerPayload == null || bannerPayload.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Banner payload is required");
        }
        Map result = courseServiceClient.createBanner(bannerPayload);
        if (result == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Failed to create banner");
        }
        return CourseListResponse.builder()
                .success(true)
                .message("Banner created successfully")
                .data(List.of(result))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Get all banners
     */
    @GetMapping("/banners")
    public CourseListResponse getAllBanners() {
        List<Map> banners = courseServiceClient.getAllBanners();
        List<Object> dataList = new ArrayList<>(banners);
        return CourseListResponse.builder()
                .success(true)
                .message("Banners retrieved successfully")
                .data(dataList)
                .count(dataList.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Get banner by ID
     */
    @GetMapping("/banners/{bannerId}")
    public CourseListResponse getBannerById(@PathVariable Long bannerId) {
        Map banner = courseServiceClient.getBannerById(bannerId);
        if (banner == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Banner not found");
        }
        return CourseListResponse.builder()
                .success(true)
                .message("Banner retrieved successfully")
                .data(List.of(banner))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Update banner (full)
     */
    @PutMapping("/banners/{bannerId}")
    public CourseListResponse updateBanner(@PathVariable Long bannerId, @RequestBody Map<String, Object> bannerPayload) {
        if (bannerPayload == null || bannerPayload.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Banner payload is required");
        }
        Map result = courseServiceClient.updateBanner(bannerId, bannerPayload);
        if (result == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Banner not found or update failed");
        }
        return CourseListResponse.builder()
                .success(true)
                .message("Banner updated successfully")
                .data(List.of(result))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Partial update banner
     */
    @PatchMapping("/banners/{bannerId}")
    public CourseListResponse partialUpdateBanner(@PathVariable Long bannerId, @RequestBody Map<String, Object> bannerPayload) {
        if (bannerPayload == null || bannerPayload.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Banner payload is required");
        }
        Map result = courseServiceClient.partialUpdateBanner(bannerId, bannerPayload);
        if (result == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Banner not found or update failed");
        }
        return CourseListResponse.builder()
                .success(true)
                .message("Banner updated successfully")
                .data(List.of(result))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Update banner status
     */
    @PatchMapping("/banners/{bannerId}/status")
    public CourseListResponse updateBannerStatus(@PathVariable Long bannerId, @RequestBody Map<String, Object> statusPayload) {
        if (statusPayload == null || statusPayload.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Status payload is required");
        }
        Map result = courseServiceClient.updateBannerStatus(bannerId, statusPayload);
        if (result == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Banner not found or update failed");
        }
        return CourseListResponse.builder()
                .success(true)
                .message("Banner status updated successfully")
                .data(List.of(result))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Reorder banners
     */
    @PatchMapping("/banners/reorder")
    public CourseListResponse reorderBanners(@RequestBody Map<String, Object> reorderPayload) {
        List<Map> banners = courseServiceClient.reorderBanners(reorderPayload);
        List<Object> dataList = new ArrayList<>(banners);
        return CourseListResponse.builder()
                .success(true)
                .message("Banner order updated successfully")
                .data(dataList)
                .count(dataList.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Delete banner
     */
    @DeleteMapping("/banners/{bannerId}")
    public CourseListResponse deleteBanner(@PathVariable Long bannerId) {
        Map result = courseServiceClient.deleteBanner(bannerId);
        if (result == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Banner not found");
        }
        return CourseListResponse.builder()
                .success(true)
                .message("Banner deleted successfully")
                .data(List.of(result))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Get banner analytics
     */
    @GetMapping("/banners/{bannerId}/analytics")
    public CourseListResponse getBannerAnalytics(@PathVariable Long bannerId) {
        Map analytics = courseServiceClient.getBannerAnalytics(bannerId);
        if (analytics == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Banner not found");
        }
        return CourseListResponse.builder()
                .success(true)
                .message("Banner analytics retrieved successfully")
                .data(List.of(analytics))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Upload banner image
     */
    @PostMapping("/banners/upload-image")
    public CourseListResponse uploadBannerImage(@RequestParam("file") MultipartFile file,
                                                    @RequestParam(value = "imageType", defaultValue = "desktop") String imageType) {
        try {
            // For now, return a response indicating that image upload should be done directly via course service
            // due to multipart complexity in service-to-service communication
            return CourseListResponse.builder()
                    .success(false)
                    .message("Please upload images directly to Course Service at: " + courseServiceClient.getCourseServiceUrl() + "/api/v1/admin/banners/upload-image")
                    .data(List.of(Map.of(
                            "directUploadUrl", courseServiceClient.getCourseServiceUrl() + "/api/v1/admin/banners/upload-image",
                            "useMultipart", true,
                            "parameters", Map.of("file", "MultipartFile", "imageType", "String")
                    )))
                    .count(1)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        } catch (Exception e) {
            return CourseListResponse.builder()
                    .success(false)
                    .message("Image upload information: " + e.getMessage())
                    .data(List.of())
                    .count(0)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }
    }

    /**
     * Validate banner image
     */
    @PostMapping("/banners/validate-image")
    public CourseListResponse validateBannerImage(@RequestParam("file") MultipartFile file) {
        try {
            // For now, return a response indicating that image validation should be done directly via course service
            return CourseListResponse.builder()
                    .success(false)
                    .message("Please validate images directly to Course Service at: " + courseServiceClient.getCourseServiceUrl() + "/api/v1/admin/banners/validate-image")
                    .data(List.of(Map.of(
                            "directValidationUrl", courseServiceClient.getCourseServiceUrl() + "/api/v1/admin/banners/validate-image",
                            "useMultipart", true,
                            "parameters", Map.of("file", "MultipartFile")
                    )))
                    .count(1)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        } catch (Exception e) {
            return CourseListResponse.builder()
                    .success(false)
                    .message("Image validation information: " + e.getMessage())
                    .data(List.of())
                    .count(0)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }
    }

    /**
     * Get deleted banners
     */
    @GetMapping("/banners/deleted")
    public CourseListResponse getDeletedBanners() {
        List<Map> banners = courseServiceClient.getDeletedBanners();
        List<Object> dataList = new ArrayList<>(banners);
        return CourseListResponse.builder()
                .success(true)
                .message("Deleted banners retrieved successfully")
                .data(dataList)
                .count(dataList.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Restore banner
     */
    @PostMapping("/banners/{bannerId}/restore")
    public CourseListResponse restoreBanner(@PathVariable Long bannerId) {
        Map result = courseServiceClient.restoreBanner(bannerId);
        if (result == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Banner not found");
        }
        return CourseListResponse.builder()
                .success(true)
                .message("Banner restored successfully")
                .data(List.of(result))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Get active banners by target type
     */
    @GetMapping("/banners/target/{targetType}")
    public CourseListResponse getActiveBannersByTargetType(@PathVariable String targetType) {
        List<Map> banners = courseServiceClient.getActiveBannersByTargetType(targetType);
        List<Object> dataList = new ArrayList<>(banners);
        return CourseListResponse.builder()
                .success(true)
                .message("Banners retrieved successfully for target type: " + targetType)
                .data(dataList)
                .count(dataList.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Track banner conversion
     */
    @PostMapping("/banners/{bannerId}/conversion")
    public CourseListResponse trackBannerConversion(@PathVariable Long bannerId) {
        Map result = courseServiceClient.trackBannerConversion(bannerId);
        if (result == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Banner not found");
        }
        return CourseListResponse.builder()
                .success(true)
                .message("Banner conversion tracked successfully")
                .data(List.of(result))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}
