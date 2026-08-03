package com.lms.review.service;

import com.lms.review.client.CourseCheckResponse;
import com.lms.review.client.CourseClient;
import com.lms.review.client.EnrollmentCheckResponse;
import com.lms.review.client.EnrollmentClient;
import com.lms.review.client.StudentNameResolver;
import com.lms.review.dto.request.CreateReviewRequest;
import com.lms.review.dto.request.UpdateReviewRequest;
import com.lms.review.dto.response.ApiResponse;
import com.lms.review.dto.response.CourseRatingSummaryResponse;
import com.lms.review.dto.response.CourseReviewPageResponse;
import com.lms.review.dto.response.CourseReviewResponse;
import com.lms.review.dto.response.MyReviewResponse;
import com.lms.review.dto.response.ReviewSubmissionResponse;
import com.lms.review.entity.Review;
import java.time.Instant;
import java.time.ZoneOffset;
import com.lms.review.enums.ReviewStatus;
import com.lms.review.exception.BusinessException;
import com.lms.review.repository.ReviewRepository;
import com.lms.review.security.UserPrincipal;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final EnrollmentClient enrollmentClient;
    private final CourseClient courseClient;
    private final StudentNameResolver studentNameResolver;

    @Transactional
    public ApiResponse createReview(UUID userId, CreateReviewRequest request) {
        validateReviewRequest(request);
        validateCourseExists(request.getCourseId());

        reviewRepository.findByUserIdAndCourseId(userId, request.getCourseId())
                .ifPresent(review -> {
                    throw new BusinessException("You have already reviewed this course", HttpStatus.CONFLICT);
                });

        EnrollmentCheckResponse enrollment = enrollmentClient.checkEnrollment(request.getCourseId());
        if (enrollment == null || !enrollment.isEnrolled()) {
            throw new BusinessException("Only enrolled students can review this course", HttpStatus.FORBIDDEN);
        }

        Review review = Review.builder()
                .userId(userId)
                .courseId(request.getCourseId())
                .rating(request.getRating())
                .comment(request.getComment())
                .status(ReviewStatus.ACTIVE)
                .build();

        Review savedReview = reviewRepository.save(review);
        Instant createdAt = savedReview.getCreatedAt() != null
                ? savedReview.getCreatedAt().toInstant(ZoneOffset.UTC)
                : Instant.now();

        String reviewId = savedReview.getUuid() != null ? savedReview.getUuid().toString() : null;

        ReviewSubmissionResponse responseData = new ReviewSubmissionResponse(
                reviewId,
                savedReview.getCourseId().toString(),
                savedReview.getUserId().toString(),
                savedReview.getRating(),
                savedReview.getComment(),
                createdAt);

        return ApiResponse.builder()
                .success(true)
                .message("Review submitted successfully.")
                .data(responseData)
                .timestamp(createdAt)
                .build();
    }

    @Transactional
    public ApiResponse updateReview(UserPrincipal principal, Long reviewId, UpdateReviewRequest request) {
        validateReviewUpdateRequest(request);
        Review review = getReviewForModification(principal, reviewId);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);

        return ApiResponse.builder()
                .success(true)
                .message("Review updated successfully")
                .build();
    }

    @Transactional
    public ApiResponse deleteReview(UserPrincipal principal, Long reviewId) {
        Review review = getReviewForModification(principal, reviewId);
        reviewRepository.delete(review);

        return ApiResponse.builder()
                .success(true)
                .message("Review deleted successfully")
                .build();
    }

    @Transactional(readOnly = true)
    public MyReviewResponse getReviewById(UserPrincipal principal, Long reviewId) {
        Review review = getReviewForModification(principal, reviewId);

        return MyReviewResponse.builder()
                .reviewId(review.getUuid() != null ? review.getUuid().toString() : null)
                .courseId(review.getCourseId() != null ? review.getCourseId().toString() : null)
                .studentId(review.getUserId() != null ? review.getUserId().toString() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().toInstant(ZoneOffset.UTC) : null)
                .updatedAt(review.getUpdatedAt() != null ? review.getUpdatedAt().toInstant(ZoneOffset.UTC) : null)
                .build();
    }

    @Transactional(readOnly = true)
    public MyReviewResponse getMyReviewForCourse(UUID userId, Long courseId) {
        Review review = reviewRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new BusinessException("Review not found for the specified course.",
                        HttpStatus.NOT_FOUND));

        return MyReviewResponse.builder()
                .reviewId(review.getUuid() != null ? review.getUuid().toString() : null)
                .courseId(review.getCourseId() != null ? review.getCourseId().toString() : null)
                .studentId(review.getUserId() != null ? review.getUserId().toString() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().toInstant(ZoneOffset.UTC) : null)
                .updatedAt(review.getUpdatedAt() != null ? review.getUpdatedAt().toInstant(ZoneOffset.UTC) : null)
                .build();
    }

    @Transactional(readOnly = true)
    public CourseReviewPageResponse getCourseReviews(Long courseId, Pageable pageable) {
        try {
            log.info("Fetching reviews for courseId: {}", courseId);
            Page<Review> page = reviewRepository.findByCourseIdAndStatus(courseId, ReviewStatus.ACTIVE, pageable);
            log.info("Found {} reviews for courseId: {}", page.getTotalElements(), courseId);

            List<CourseReviewResponse> content = new java.util.ArrayList<>();
            for (Review review : page.getContent()) {
                log.debug("Processing review: userId={}, rating={}", review.getUserId(), review.getRating());
                String studentName = studentNameResolver.resolve(review.getUserId());
                log.debug("Resolved studentName: {}", studentName);
                CourseReviewResponse response = CourseReviewResponse.builder()
                        .studentName(studentName)
                        .rating(review.getRating())
                        .comment(review.getComment())
                        .createdAt(review.getCreatedAt())
                        .build();
                content.add(response);
            }

            return CourseReviewPageResponse.builder()
                    .content(content)
                    .page(page.getNumber())
                    .size(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .last(page.isLast())
                    .build();
        } catch (Exception ex) {
            log.error("Error fetching reviews for courseId: {}", courseId, ex);
            throw new BusinessException("Failed to fetch reviews: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public CourseRatingSummaryResponse getCourseRatingSummary(Long courseId) {
        Double average = reviewRepository.averageRatingByCourseIdAndStatus(courseId, ReviewStatus.ACTIVE);
        long total = reviewRepository.countByCourseIdAndStatus(courseId, ReviewStatus.ACTIVE);
        List<Object[]> distributionRows = reviewRepository.countRatingsByCourseIdAndStatus(courseId,
                ReviewStatus.ACTIVE);

        Map<String, Long> distribution = new LinkedHashMap<>();
        for (int rating = 5; rating >= 1; rating--) {
            distribution.put(String.valueOf(rating), 0L);
        }
        for (Object[] row : distributionRows) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            distribution.put(String.valueOf(rating), count);
        }

        return CourseRatingSummaryResponse.builder()
                .courseId(courseId)
                .averageRating(average == null ? 0.0 : Math.round(average * 10.0) / 10.0)
                .totalReviews(total)
                .ratingDistribution(distribution)
                .build();
    }

    private Review getReviewForModification(UserPrincipal principal, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("Review not found", HttpStatus.NOT_FOUND));

        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        if (!isAdmin && !review.getUserId().equals(principal.getUserId())) {
            throw new BusinessException("You can only modify your own review", HttpStatus.FORBIDDEN);
        }
        return review;
    }

    private void validateReviewRequest(CreateReviewRequest request) {
        if (request == null) {
            throw new BusinessException("Review request is required", HttpStatus.BAD_REQUEST);
        }
        if (request.getCourseId() == null) {
            throw new BusinessException("Course ID is required", HttpStatus.BAD_REQUEST);
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BusinessException("Rating must be between 1 and 5", HttpStatus.BAD_REQUEST);
        }
        if (request.getComment() != null && request.getComment().length() > 2000) {
            throw new BusinessException("Comment must not exceed 2000 characters", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateReviewUpdateRequest(UpdateReviewRequest request) {
        if (request == null) {
            throw new BusinessException("Review request is required", HttpStatus.BAD_REQUEST);
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BusinessException("Rating must be between 1 and 5", HttpStatus.BAD_REQUEST);
        }
        if (request.getComment() != null && request.getComment().length() > 2000) {
            throw new BusinessException("Comment must not exceed 2000 characters", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateCourseExists(Long courseId) {
        try {
            CourseCheckResponse course = courseClient.getCourseById(courseId);
            if (course == null || course.getId() == null) {
                throw new BusinessException("Course not found", HttpStatus.NOT_FOUND);
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (FeignException.NotFound ex) {
            throw new BusinessException("Course not found", HttpStatus.NOT_FOUND);
        } catch (FeignException ex) {
            String body = ex.contentUTF8();
            if (ex.status() == 404 || (body != null && body.contains("Course not found"))) {
                throw new BusinessException("Course not found", HttpStatus.NOT_FOUND);
            }
            log.warn("Course service validation failed for courseId={}, continuing without blocking review submission",
                    courseId, ex);
        } catch (Exception ex) {
            log.warn("Unable to validate courseId={}, continuing without blocking review submission", courseId, ex);
        }
    }
}
