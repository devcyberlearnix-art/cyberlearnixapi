package com.lms.review.controller;

import com.lms.review.dto.request.CreateReviewRequest;
import com.lms.review.dto.request.UpdateReviewRequest;
import com.lms.review.dto.response.ApiResponse;
import com.lms.review.dto.response.CourseRatingSummaryResponse;
import com.lms.review.dto.response.CourseReviewPageResponse;
import com.lms.review.dto.response.MyReviewResponse;
import com.lms.review.security.UserPrincipal;
import com.lms.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Student and public review APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Create a review", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> createReview(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateReviewRequest request) {
        ApiResponse response = reviewService.createReview(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "Update own review", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> updateReview(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(principal, reviewId, request));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete own review", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> deleteReview(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.deleteReview(principal, reviewId));
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MyReviewResponse> getReviewById(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.getReviewById(principal, reviewId));
    }

    @GetMapping("/my/course/{courseId}")
    @Operation(summary = "Get my review for a course", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> getMyReviewForCourse(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long courseId) {
        MyReviewResponse review = reviewService.getMyReviewForCourse(principal.getUserId(), courseId);
        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Review retrieved successfully.")
                .data(review)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get public course reviews (ACTIVE only)")
    public ResponseEntity<CourseReviewPageResponse> getCourseReviews(
            @PathVariable Long courseId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getCourseReviews(courseId, pageable));
    }

    @GetMapping("/course/{courseId}/summary")
    @Operation(summary = "Get course rating summary (ACTIVE only)")
    public ResponseEntity<CourseRatingSummaryResponse> getCourseRatingSummary(@PathVariable Long courseId) {
        return ResponseEntity.ok(reviewService.getCourseRatingSummary(courseId));
    }
}
