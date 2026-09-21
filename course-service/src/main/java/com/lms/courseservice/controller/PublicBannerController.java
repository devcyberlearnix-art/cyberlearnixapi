package com.lms.courseservice.controller;

import com.lms.courseservice.dto.ApiResponse;
import com.lms.courseservice.dto.PublicBannerResponse;
import com.lms.courseservice.entity.Banner.BannerTargetType;
import com.lms.courseservice.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/banners")
@RequiredArgsConstructor
@Tag(name = "Public Banners", description = "Public endpoints for retrieving active promotional banners")
public class PublicBannerController {

    private final BannerService bannerService;

    @Operation(summary = "Get active banners", description = "Retrieve all active and currently valid banners for public display. Results are cached in Redis for 15 minutes for performance. Only returns banners with ACTIVE status within their scheduled date range.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PublicBannerResponse>>> getActiveBanners() {
        List<PublicBannerResponse> banners = bannerService.getActiveBanners();

        String message = banners.isEmpty() ? "No active banners found" : "Active banners retrieved successfully";

        return ResponseEntity.ok(
                ApiResponse.<List<PublicBannerResponse>>builder()
                        .success(true)
                        .message(message)
                        .data(banners)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @Operation(summary = "Get active banners by target type", description = "Retrieve active banners filtered by target type (COURSE, CATEGORY, EXTERNAL_URL, CUSTOM_PAGE). Results are cached in Redis for 15 minutes. Useful for displaying contextual banners on specific pages.")
    @GetMapping("/target/{targetType}")
    public ResponseEntity<ApiResponse<List<PublicBannerResponse>>> getActiveBannersByTargetType(
            @Parameter(description = "Target type filter: COURSE (for course pages), CATEGORY (for category pages), EXTERNAL_URL (for external links), CUSTOM_PAGE (for custom pages)")
            @PathVariable BannerTargetType targetType) {
        List<PublicBannerResponse> banners = bannerService.getActiveBannersByTargetType(targetType);

        String message = banners.isEmpty() ? "No active banners found for target type: " + targetType
                                         : "Active banners retrieved successfully for target type: " + targetType;

        return ResponseEntity.ok(
                ApiResponse.<List<PublicBannerResponse>>builder()
                        .success(true)
                        .message(message)
                        .data(banners)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @Operation(summary = "Track banner impression", description = "Record an impression when a banner is viewed by a user. Automatically updates the last impression timestamp. Used for analytics and performance measurement.")
    @PostMapping("/{id}/impression")
    public ResponseEntity<ApiResponse<Void>> trackImpression(@PathVariable Long id) {
        bannerService.trackImpression(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Banner impression recorded successfully")
                        .data(null)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @Operation(summary = "Track banner click", description = "Record a click when a user interacts with a banner CTA button. Automatically updates the last click timestamp. Used for calculating click-through rate (CTR).")
    @PostMapping("/{id}/click")
    public ResponseEntity<ApiResponse<Void>> trackClick(@PathVariable Long id) {
        bannerService.trackClick(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Banner click recorded successfully")
                        .data(null)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @Operation(summary = "Track banner conversion", description = "Record a conversion when a user completes a desired action after clicking a banner (e.g., course enrollment, purchase). Used for calculating conversion rate and measuring campaign ROI.")
    @PostMapping("/{id}/conversion")
    public ResponseEntity<ApiResponse<Void>> trackConversion(@PathVariable Long id) {
        bannerService.trackConversion(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Banner conversion recorded successfully")
                        .data(null)
                        .timestamp(Instant.now().toString())
                        .build());
    }
}
