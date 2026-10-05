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
        Map result = courseServiceClient.createBanner(bannerPayload);
        List<Object> dataList = result != null ? List.of(result) : List.of();
        return CourseListResponse.builder()
                .success(result != null)
                .message(result != null ? "Banner created successfully" : "Failed to create banner")
                .data(dataList)
                .count(dataList.size())
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
        List<Object> dataList = banner != null ? List.of(banner) : List.of();
        return CourseListResponse.builder()
                .success(banner != null)
                .message(banner != null ? "Banner retrieved successfully" : "Banner not found")
                .data(dataList)
                .count(dataList.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Update banner (full)
     */
    @PutMapping("/banners/{bannerId}")
    public CourseListResponse updateBanner(@PathVariable Long bannerId, @RequestBody Map<String, Object> bannerPayload) {
        Map result = courseServiceClient.updateBanner(bannerId, bannerPayload);
        List<Object> dataList = result != null ? List.of(result) : List.of();
        return CourseListResponse.builder()
                .success(result != null)
                .message(result != null ? "Banner updated successfully" : "Failed to update banner")
                .data(dataList)
                .count(dataList.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Partial update banner
     */
    @PatchMapping("/banners/{bannerId}")
    public CourseListResponse partialUpdateBanner(@PathVariable Long bannerId, @RequestBody Map<String, Object> bannerPayload) {
        Map result = courseServiceClient.partialUpdateBanner(bannerId, bannerPayload);
        List<Object> dataList = result != null ? List.of(result) : List.of();
        return CourseListResponse.builder()
                .success(result != null)
                .message(result != null ? "Banner updated successfully" : "Failed to update banner")
                .data(dataList)
                .count(dataList.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Update banner status
     */
    @PatchMapping("/banners/{bannerId}/status")
    public CourseListResponse updateBannerStatus(@PathVariable Long bannerId, @RequestBody Map<String, Object> statusPayload) {
        Map result = courseServiceClient.updateBannerStatus(bannerId, statusPayload);
        List<Object> dataList = result != null ? List.of(result) : List.of();
        return CourseListResponse.builder()
                .success(result != null)
                .message(result != null ? "Banner status updated successfully" : "Failed to update banner status")
                .data(dataList)
                .count(dataList.size())
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
        boolean deleted = result != null;
        List<Object> dataList = deleted ? List.of(result) : List.of();
        return CourseListResponse.builder()
                .success(deleted)
                .message(deleted ? "Banner deleted successfully" : "Failed to delete banner")
                .data(dataList)
                .count(dataList.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Get banner analytics
     */
    @GetMapping("/banners/{bannerId}/analytics")
    public CourseListResponse getBannerAnalytics(@PathVariable Long bannerId) {
        Map analytics = courseServiceClient.getBannerAnalytics(bannerId);
        List<Object> dataList = analytics != null ? List.of(analytics) : List.of();
        return CourseListResponse.builder()
                .success(analytics != null)
                .message(analytics != null ? "Banner analytics retrieved successfully" : "Failed to get banner analytics")
                .data(dataList)
                .count(dataList.size())
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
        List<Object> dataList = result != null ? List.of(result) : List.of();
        return CourseListResponse.builder()
                .success(result != null)
                .message(result != null ? "Banner restored successfully" : "Failed to restore banner")
                .data(dataList)
                .count(dataList.size())
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
        List<Object> dataList = result != null ? List.of(result) : List.of();
        return CourseListResponse.builder()
                .success(result != null)
                .message(result != null ? "Banner conversion tracked successfully" : "Failed to track banner conversion")
                .data(dataList)
                .count(dataList.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}
