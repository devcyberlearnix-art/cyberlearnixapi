package com.lms.review.service;

import com.lms.review.dto.response.AdminReviewPageResponse;
import com.lms.review.dto.response.AdminReviewResponse;
import com.lms.review.dto.response.ApiResponse;
import com.lms.review.entity.Review;
import com.lms.review.enums.ReviewStatus;
import com.lms.review.exception.BusinessException;
import com.lms.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public AdminReviewPageResponse getAllReviews(Pageable pageable) {
        Page<Review> page = reviewRepository.findAll(pageable);

        List<AdminReviewResponse> content = page.getContent().stream()
                .map(this::toAdminResponse)
                .toList();

        return AdminReviewPageResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminReviewResponse getReviewById(String identifier) {
        Review review = getReviewOrThrow(identifier);
        return toAdminResponse(review);
    }

    @Transactional
    public ApiResponse hideReview(String identifier) {
        Review review = getReviewOrThrow(identifier);
        review.setStatus(ReviewStatus.HIDDEN);
        reviewRepository.save(review);

        return ApiResponse.builder()
                .success(true)
                .message("Review hidden successfully")
                .build();
    }

    @Transactional
    public ApiResponse unhideReview(String identifier) {
        Review review = getReviewOrThrow(identifier);
        review.setStatus(ReviewStatus.ACTIVE);
        reviewRepository.save(review);

        return ApiResponse.builder()
                .success(true)
                .message("Review unhidden successfully")
                .build();
    }

    @Transactional
    public ApiResponse deleteReview(String identifier) {
        Review review = getReviewOrThrow(identifier);
        review.setStatus(ReviewStatus.DELETED);
        reviewRepository.save(review);

        return ApiResponse.builder()
                .success(true)
                .message("Review deleted successfully")
                .build();
    }

    private Review getReviewOrThrow(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new BusinessException("Review identifier is required", HttpStatus.BAD_REQUEST);
        }
        try {
            UUID uuid = UUID.fromString(identifier);
            return reviewRepository.findByUuid(uuid)
                    .orElseThrow(() -> new BusinessException("Review not found", HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            try {
                Long id = Long.parseLong(identifier);
                return reviewRepository.findById(id)
                        .orElseThrow(() -> new BusinessException("Review not found", HttpStatus.NOT_FOUND));
            } catch (NumberFormatException nfe) {
                throw new BusinessException("Invalid review identifier format", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private AdminReviewResponse toAdminResponse(Review review) {
        return AdminReviewResponse.builder()
                .id(review.getId())
                .reviewUuid(review.getUuid())
                .userId(review.getUserId())
                .courseId(review.getCourseId())
                .rating(review.getRating())
                .comment(review.getComment())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
