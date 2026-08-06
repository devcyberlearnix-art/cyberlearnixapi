package com.lms.review.controller;

import com.lms.review.dto.response.AdminReviewPageResponse;
import com.lms.review.dto.response.AdminReviewResponse;
import com.lms.review.dto.response.ApiResponse;
import com.lms.review.service.AdminReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@Tag(name = "Admin Reviews", description = "Admin moderation APIs")
@SecurityRequirement(name = "bearerAuth")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping
    @Operation(summary = "List all reviews (paginated)")
    public ResponseEntity<AdminReviewPageResponse> getAllReviews(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminReviewService.getAllReviews(pageable));
    }

    @GetMapping("/{reviewUuid}")
    @Operation(summary = "Get review details")
    public ResponseEntity<AdminReviewResponse> getReviewById(@PathVariable UUID reviewUuid) {
        return ResponseEntity.ok(adminReviewService.getReviewById(reviewUuid));
    }

    @PatchMapping("/{reviewUuid}/hide")
    @Operation(summary = "Hide a review")
    public ResponseEntity<ApiResponse> hideReview(@PathVariable UUID reviewUuid) {
        return ResponseEntity.ok(adminReviewService.hideReview(reviewUuid));
    }

    @PatchMapping("/{reviewUuid}/unhide")
    @Operation(summary = "Unhide a review")
    public ResponseEntity<ApiResponse> unhideReview(@PathVariable UUID reviewUuid) {
        return ResponseEntity.ok(adminReviewService.unhideReview(reviewUuid));
    }

    @DeleteMapping("/{reviewUuid}")
    @Operation(summary = "Delete any review")
    public ResponseEntity<ApiResponse> deleteReview(@PathVariable UUID reviewUuid) {
        return ResponseEntity.ok(adminReviewService.deleteReview(reviewUuid));
    }
}
