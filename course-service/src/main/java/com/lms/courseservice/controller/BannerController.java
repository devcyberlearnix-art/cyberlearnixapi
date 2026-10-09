package com.lms.courseservice.controller;

import com.lms.courseservice.dto.*;
import com.lms.courseservice.entity.Banner;
import com.lms.courseservice.entity.Banner.BannerTargetType;
import com.lms.courseservice.service.BannerService;
import com.lms.courseservice.service.BannerImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/banners")
@RequiredArgsConstructor
@Tag(name = "Banner Management (Admin)", description = "Admin operations for managing promotional banners")
@SecurityRequirement(name = "bearerAuth")
public class BannerController {

    private final BannerService bannerService;
    private final BannerImageService bannerImageService;

    @Operation(summary = "Create a new banner", description = "Create a new promotional banner with enhanced features including mobile images, alt text, and target types. Supports COURSE, CATEGORY, EXTERNAL_URL, and CUSTOM_PAGE target types. All new fields are optional for backward compatibility.")
    @PostMapping
    public ResponseEntity<ApiResponse<BannerResponse>> createBanner(
            @Valid @RequestBody BannerCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Banner banner;
        if (userId != null && !userId.isEmpty()) {
            banner = bannerService.createBanner(request, userId);
        } else {
            banner = bannerService.createBanner(request);
        }
        BannerResponse response = toBannerResponse(banner);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<BannerResponse>builder()
                        .success(true)
                        .message("Banner created successfully")
                        .data(response)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @Operation(summary = "Upload banner image", description = "Upload and validate a banner image with strict validation: accepts JPG/PNG/WebP formats, max 1MB file size, and 16:9 aspect ratio (recommended 1920x1080). Returns CDN-ready URL from Cloudinary.")
    @PostMapping("/upload-image")
    public ResponseEntity<ApiResponse<BannerImageUploadRequest>> uploadBannerImage(
            @Parameter(description = "Image file (JPG/PNG/WebP, max 1MB, 16:9 aspect ratio, recommended 1920x1080)")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Image type (desktop/mobile) for optimization")
            @RequestParam(value = "imageType", defaultValue = "desktop") String imageType) {
        BannerImageUploadRequest uploadResult = bannerImageService.uploadImage(file, imageType);

        return ResponseEntity.ok(
                ApiResponse.<BannerImageUploadRequest>builder()
                        .success(true)
                        .message("Image uploaded successfully")
                        .data(uploadResult)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @Operation(summary = "Validate banner image", description = "Validate banner image before upload. Checks file format (JPG/PNG/WebP), file size (max 1MB), aspect ratio (16:9 with tolerance), and minimum resolution (800x450). Returns detailed validation feedback with recommendations.")
    @PostMapping("/validate-image")
    public ResponseEntity<ApiResponse<ImageValidationResult>> validateBannerImage(
            @Parameter(description = "Image file to validate against production standards")
            @RequestParam("file") MultipartFile file) {
        ImageValidationResult validation = bannerImageService.validateImage(file);

        return ResponseEntity.ok(
                ApiResponse.<ImageValidationResult>builder()
                        .success(validation.isValid())
                        .message(validation.isValid() ? "Image validation passed" : "Image validation failed")
                        .data(validation)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @Operation(summary = "Get all banners", description = "Retrieve all banners including inactive ones (Admin only)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getAllBanners() {
        List<Banner> banners = bannerService.getAllBanners();
        List<BannerResponse> responses = banners.stream()
                .map(this::toBannerResponse)
                .toList();

        String message = responses.isEmpty() ? "No banners found" : "Banners retrieved successfully";

        return ResponseEntity.ok(
                ApiResponse.<List<BannerResponse>>builder()
                        .success(true)
                        .message(message)
                        .data(responses)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @Operation(summary = "Get deleted banners", description = "Retrieve soft-deleted banners for audit and recovery purposes. Shows banners that have been marked as deleted but not permanently removed from the database.")
    @GetMapping("/deleted")
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getDeletedBanners() {
        List<Banner> banners = bannerService.getDeletedBanners();
        List<BannerResponse> responses = banners.stream()
                .map(this::toBannerResponse)
                .toList();

        String message = responses.isEmpty() ? "No deleted banners found" : "Deleted banners retrieved successfully";

        return ResponseEntity.ok(
                ApiResponse.<List<BannerResponse>>builder()
                        .success(true)
                        .message(message)
                        .data(responses)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @Operation(summary = "Get banner by ID", description = "Retrieve a specific banner by ID (Admin only)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BannerResponse>> getBannerById(@PathVariable Long id) {
        Banner banner = bannerService.getBannerById(id);
        BannerResponse response = toBannerResponse(banner);

        return ResponseEntity.ok(
                ApiResponse.<BannerResponse>builder()
                        .success(true)
                        .message("Banner retrieved successfully")
                        .data(response)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @Operation(summary = "Full update banner", description = "Update all fields of a banner (Admin only). New fields are optional for backward compatibility.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BannerResponse>> updateBanner(
            @PathVariable Long id,
            @Valid @RequestBody BannerUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Banner banner;
        if (userId != null && !userId.isEmpty()) {
            banner = bannerService.updateBanner(id, request, userId);
        } else {
            banner = bannerService.updateBanner(id, request);
        }
        BannerResponse response = toBannerResponse(banner);

        return ResponseEntity.ok(
                ApiResponse.<BannerResponse>builder()
                        .success(true)
                        .message("Banner updated successfully")
                        .data(response)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @Operation(summary = "Partial update banner", description = "Update specific fields of a banner (Admin only). New fields are optional for backward compatibility.")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<BannerResponse>> partialUpdateBanner(
            @PathVariable Long id,
            @Valid @RequestBody BannerPartialUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Banner banner;
        if (userId != null && !userId.isEmpty()) {
            banner = bannerService.partialUpdateBanner(id, request, userId);
        } else {
            banner = bannerService.partialUpdateBanner(id, request);
        }
        BannerResponse response = toBannerResponse(banner);

        return ResponseEntity.ok(
                ApiResponse.<BannerResponse>builder()
                        .success(true)
                        .message("Banner updated successfully")
                        .data(response)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @Operation(summary = "Update banner status", description = "Activate or deactivate a banner (Admin only)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BannerResponse>> updateBannerStatus(
            @PathVariable Long id,
            @Valid @RequestBody BannerStatusUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Banner banner;
        if (userId != null && !userId.isEmpty()) {
            banner = bannerService.updateBannerStatus(id, request, userId);
        } else {
            banner = bannerService.updateBannerStatus(id, request);
        }
        BannerResponse response = toBannerResponse(banner);

        return ResponseEntity.ok(
                ApiResponse.<BannerResponse>builder()
                        .success(true)
                        .message("Banner status updated successfully")
                        .data(response)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @Operation(summary = "Reorder banners", description = "Change the display order of multiple banners (Admin only)")
    @PatchMapping("/reorder")
    public ResponseEntity<ApiResponse<List<BannerResponse>>> reorderBanners(
            @Valid @RequestBody BannerReorderRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        List<Banner> banners;
        if (userId != null && !userId.isEmpty()) {
            banners = bannerService.reorderBanners(request, userId);
        } else {
            banners = bannerService.reorderBanners(request);
        }
        List<BannerResponse> responses = banners.stream()
                .map(this::toBannerResponse)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.<List<BannerResponse>>builder()
                        .success(true)
                        .message("Banner order updated successfully")
                        .data(responses)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @Operation(summary = "Delete banner", description = "Soft delete a banner by ID (Admin only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId != null && !userId.isEmpty()) {
            bannerService.deleteBanner(id, userId);
        } else {
            bannerService.deleteBanner(id);
        }

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Banner deleted successfully")
                        .data(null)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @Operation(summary = "Restore banner", description = "Restore a soft-deleted banner to active status. Useful for recovering accidentally deleted banners or reactivating promotional campaigns.")
    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<BannerResponse>> restoreBanner(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Banner banner = bannerService.restoreBanner(id, userId != null && !userId.isEmpty() ? userId : "system");
        BannerResponse response = toBannerResponse(banner);

        return ResponseEntity.ok(
                ApiResponse.<BannerResponse>builder()
                        .success(true)
                        .message("Banner restored successfully")
                        .data(response)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @Operation(summary = "Get banner analytics", description = "Retrieve comprehensive analytics data for a banner including impressions, clicks, conversions, click-through rate (CTR), conversion rate, and timestamps of last interaction. Useful for measuring campaign effectiveness.")
    @GetMapping("/{id}/analytics")
    public ResponseEntity<ApiResponse<BannerAnalyticsResponse>> getBannerAnalytics(@PathVariable Long id) {
        BannerAnalyticsResponse analytics = bannerService.getBannerAnalytics(id);

        return ResponseEntity.ok(
                ApiResponse.<BannerAnalyticsResponse>builder()
                        .success(true)
                        .message("Banner analytics retrieved successfully")
                        .data(analytics)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    private BannerResponse toBannerResponse(Banner banner) {
        return BannerResponse.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .description(banner.getDescription())
                .imageUrl(banner.getImageUrl())
                .mobileImageUrl(banner.getMobileImageUrl())
                .altText(banner.getAltText())
                .ctaText(banner.getCtaText())
                .ctaUrl(banner.getCtaUrl())
                .courseId(banner.getCourseId())
                .displayOrder(banner.getDisplayOrder())
                .status(banner.getStatus())
                .targetType(banner.getTargetType())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .createdBy(banner.getCreatedBy())
                .updatedBy(banner.getUpdatedBy())
                .impressions(banner.getImpressions())
                .clicks(banner.getClicks())
                .conversions(banner.getConversions())
                .lastImpressionAt(banner.getLastImpressionAt())
                .lastClickAt(banner.getLastClickAt())
                .build();
    }
}
