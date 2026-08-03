package com.example.admin.controller;
import com.example.admin.dto.ApiResponse;
import com.example.admin.dto.ReviewDto;
import com.example.admin.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ApiResponse<List<ReviewDto>> getAllReviews() {

        List<ReviewDto> reviews = reviewService.getAllReviews();

        return new ApiResponse<>(
                true,
                "Reviews fetched successfully",
                reviews,
                LocalDateTime.now().toString()
        );
    }
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteReview(@PathVariable UUID id) {

        boolean isDeleted = reviewService.deleteReview(id);

        if (!isDeleted) {
            return new ApiResponse<>(
                    false,
                    "Review not found or deletion failed",
                    null,
                    LocalDateTime.now().toString()
            );
        }

        return new ApiResponse<>(
                true,
                "Review deleted successfully",
                "Review ID: " + id,
                LocalDateTime.now().toString()
        );
    }
}