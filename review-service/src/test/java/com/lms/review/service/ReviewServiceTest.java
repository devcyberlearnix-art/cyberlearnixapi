package com.lms.review.service;

import com.lms.review.client.CourseClient;
import com.lms.review.client.EnrollmentClient;
import com.lms.review.client.EnrollmentInfo;
import com.lms.review.client.StudentNameResolver;
import com.lms.review.security.UserPrincipal;
import com.lms.review.dto.request.CreateReviewRequest;
import com.lms.review.dto.response.MyReviewResponse;
import com.lms.review.entity.Review;
import com.lms.review.enums.ReviewStatus;
import com.lms.review.exception.BusinessException;
import com.lms.review.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private EnrollmentClient enrollmentClient;

    @Mock
    private CourseClient courseClient;

    @Mock
    private StudentNameResolver studentNameResolver;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReviewRejectsDuplicateReview() {
        UUID userId = UUID.randomUUID();
        Long courseId = 1L;
        CreateReviewRequest request = CreateReviewRequest.builder()
                .courseId(courseId)
                .rating(5)
                .comment("Great")
                .build();

        when(reviewRepository.findByUserIdAndCourseId(userId, courseId))
                .thenReturn(Optional.of(Review.builder()
                        .userId(userId)
                        .courseId(courseId)
                        .status(ReviewStatus.ACTIVE)
                        .build()));
        when(courseClient.getCourseById(courseId))
                .thenReturn(new com.lms.review.client.CourseCheckResponse(courseId, "Course"));

        assertThrows(BusinessException.class, () -> reviewService.createReview(userId, request));
    }

    @Test
    void createReviewRejectsWhenStudentIsNotEnrolled() {
        UUID userId = UUID.randomUUID();
        Long courseId = 1L;
        CreateReviewRequest request = CreateReviewRequest.builder()
                .courseId(courseId)
                .rating(4)
                .comment("Good")
                .build();

        when(reviewRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());
        when(courseClient.getCourseById(courseId))
                .thenReturn(new com.lms.review.client.CourseCheckResponse(courseId, "Course"));
        when(enrollmentClient.getEnrollmentsByUserId(userId)).thenReturn(java.util.Collections.emptyList());

        assertThrows(BusinessException.class, () -> reviewService.createReview(userId, request));
    }

    @Test
    void createReviewSavesWhenEnrolledAndCourseExists() {
        UUID userId = UUID.randomUUID();
        Long courseId = 1L;
        CreateReviewRequest request = CreateReviewRequest.builder()
                .courseId(courseId)
                .rating(5)
                .comment("Excellent")
                .build();

        when(reviewRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());
        when(courseClient.getCourseById(courseId))
                .thenReturn(new com.lms.review.client.CourseCheckResponse(courseId, "Course"));
        when(enrollmentClient.getEnrollmentsByUserId(userId)).thenReturn(java.util.List.of(
                new EnrollmentInfo(courseId, "Course Title", userId, "Category", "ACTIVE", "2024-01-01")
        ));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setUuid(UUID.fromString("8f4b9f1e-4c0a-4c18-9e74-2d5d9e6d1234"));
            saved.setCreatedAt(LocalDateTime.now());
            saved.setStatus(ReviewStatus.ACTIVE);
            return saved;
        });

        reviewService.createReview(userId, request);

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReviewSavesWhenCourseServiceIsUnavailable() {
        UUID userId = UUID.randomUUID();
        Long courseId = 1L;
        CreateReviewRequest request = CreateReviewRequest.builder()
                .courseId(courseId)
                .rating(5)
                .comment("Excellent")
                .build();

        when(reviewRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());
        when(courseClient.getCourseById(courseId)).thenThrow(new RuntimeException("course service down"));
        when(enrollmentClient.getEnrollmentsByUserId(userId)).thenReturn(java.util.List.of(
                new EnrollmentInfo(courseId, "Course Title", userId, "Category", "ACTIVE", "2024-01-01")
        ));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setUuid(UUID.fromString("8f4b9f1e-4c0a-4c18-9e74-2d5d9e6d1234"));
            saved.setCreatedAt(LocalDateTime.now());
            saved.setStatus(ReviewStatus.ACTIVE);
            return saved;
        });

        assertDoesNotThrow(() -> reviewService.createReview(userId, request));
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void getMyReviewForCourseReturnsReview() {
        UUID userId = UUID.randomUUID();
        Long courseId = 1L;
        Review review = Review.builder()
                .id(1L)
                .uuid(UUID.fromString("8f4b9f1e-4c0a-4c18-9e74-2d5d9e6d1234"))
                .userId(userId)
                .courseId(courseId)
                .rating(5)
                .comment("Excellent course")
                .status(ReviewStatus.ACTIVE)
                .createdAt(LocalDateTime.of(2026, 6, 29, 10, 30, 15))
                .updatedAt(LocalDateTime.of(2026, 6, 29, 10, 30, 15))
                .build();

        when(reviewRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.of(review));

        MyReviewResponse response = reviewService.getMyReviewForCourse(userId, courseId);

        assertEquals("8f4b9f1e-4c0a-4c18-9e74-2d5d9e6d1234", response.getReviewId());
        assertEquals(courseId.toString(), response.getCourseId());
        assertEquals(userId.toString(), response.getStudentId());
        assertEquals(5, response.getRating());
        assertEquals("Excellent course", response.getComment());
        assertEquals(Instant.parse("2026-06-29T10:30:15Z"), response.getCreatedAt());
        assertEquals(Instant.parse("2026-06-29T10:30:15Z"), response.getUpdatedAt());
    }

    @Test
    void getMyReviewForCourseThrowsNotFoundWhenMissing() {
        UUID userId = UUID.randomUUID();
        Long courseId = 1L;

        when(reviewRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.getMyReviewForCourse(userId, courseId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Review not found for the specified course.", exception.getMessage());
    }

    @Test
    void createReviewAcceptsRatingBoundaryValues() {
        UUID userId = UUID.randomUUID();
        Long courseId = 1L;

        // Test rating = 1
        CreateReviewRequest request1 = CreateReviewRequest.builder()
                .courseId(courseId)
                .rating(1)
                .comment("Poor")
                .build();

        when(reviewRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());
        when(courseClient.getCourseById(courseId))
                .thenReturn(new com.lms.review.client.CourseCheckResponse(courseId, "Course"));
        when(enrollmentClient.getEnrollmentsByUserId(userId)).thenReturn(java.util.List.of(
                new EnrollmentInfo(courseId, "Course Title", userId, "Category", "ACTIVE", "2024-01-01")
        ));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setUuid(UUID.fromString("8f4b9f1e-4c0a-4c18-9e74-2d5d9e6d1234"));
            saved.setCreatedAt(LocalDateTime.now());
            saved.setStatus(ReviewStatus.ACTIVE);
            return saved;
        });

        assertDoesNotThrow(() -> reviewService.createReview(userId, request1));

        // Test rating = 5
        CreateReviewRequest request5 = CreateReviewRequest.builder()
                .courseId(courseId)
                .rating(5)
                .comment("Excellent")
                .build();

        assertDoesNotThrow(() -> reviewService.createReview(userId, request5));
    }

    @Test
    void createReviewRejectsInvalidRatings() {
        UUID userId = UUID.randomUUID();
        Long courseId = 1L;

        // Test rating = 0
        CreateReviewRequest request0 = CreateReviewRequest.builder()
                .courseId(courseId)
                .rating(0)
                .comment("Test")
                .build();

        BusinessException exception0 = assertThrows(BusinessException.class,
                () -> reviewService.createReview(userId, request0));
        assertEquals("Rating must be between 1 and 5", exception0.getMessage());

        // Test rating = 6
        CreateReviewRequest request6 = CreateReviewRequest.builder()
                .courseId(courseId)
                .rating(6)
                .comment("Test")
                .build();

        BusinessException exception6 = assertThrows(BusinessException.class,
                () -> reviewService.createReview(userId, request6));
        assertEquals("Rating must be between 1 and 5", exception6.getMessage());

        // Test rating = -1
        CreateReviewRequest requestNeg1 = CreateReviewRequest.builder()
                .courseId(courseId)
                .rating(-1)
                .comment("Test")
                .build();

        BusinessException exceptionNeg1 = assertThrows(BusinessException.class,
                () -> reviewService.createReview(userId, requestNeg1));
        assertEquals("Rating must be between 1 and 5", exceptionNeg1.getMessage());
    }

    @Test
    void createReviewRejectsBlankText() {
        UUID userId = UUID.randomUUID();
        Long courseId = 1L;

        // Test blank text
        CreateReviewRequest requestBlank = CreateReviewRequest.builder()
                .courseId(courseId)
                .rating(5)
                .comment("   ")
                .build();

        BusinessException exceptionBlank = assertThrows(BusinessException.class,
                () -> reviewService.createReview(userId, requestBlank));
        assertEquals("Comment cannot be blank", exceptionBlank.getMessage());

        // Test empty string
        CreateReviewRequest requestEmpty = CreateReviewRequest.builder()
                .courseId(courseId)
                .rating(5)
                .comment("")
                .build();

        BusinessException exceptionEmpty = assertThrows(BusinessException.class,
                () -> reviewService.createReview(userId, requestEmpty));
        assertEquals("Comment cannot be blank", exceptionEmpty.getMessage());
    }

    @Test
    void updateReviewRejectsOtherUsersReview() {
        UUID currentUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Long reviewId = 1L;
        Long courseId = 1L;

        Review otherReview = Review.builder()
                .id(reviewId)
                .uuid(UUID.fromString("8f4b9f1e-4c0a-4c18-9e74-2d5d9e6d1234"))
                .userId(otherUserId)
                .courseId(courseId)
                .rating(5)
                .comment("Excellent")
                .status(ReviewStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UserPrincipal principal = UserPrincipal.builder()
                .userId(currentUserId)
                .username("student@example.com")
                .roles(java.util.List.of("STUDENT"))
                .build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(otherReview));

        com.lms.review.dto.request.UpdateReviewRequest request =
                com.lms.review.dto.request.UpdateReviewRequest.builder()
                        .rating(4)
                        .comment("Updated")
                        .build();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.updateReview(principal, reviewId, request));
        assertEquals("You can only modify your own review", exception.getMessage());
    }

    @Test
    void deleteReviewRejectsOtherUsersReview() {
        UUID currentUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Long reviewId = 1L;
        Long courseId = 1L;

        Review otherReview = Review.builder()
                .id(reviewId)
                .uuid(UUID.fromString("8f4b9f1e-4c0a-4c18-9e74-2d5d9e6d1234"))
                .userId(otherUserId)
                .courseId(courseId)
                .rating(5)
                .comment("Excellent")
                .status(ReviewStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UserPrincipal principal = UserPrincipal.builder()
                .userId(currentUserId)
                .username("student@example.com")
                .roles(java.util.List.of("STUDENT"))
                .build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(otherReview));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.deleteReview(principal, reviewId));
        assertEquals("You can only modify your own review", exception.getMessage());
    }

    @Test
    void updateReviewRejectsDeletedReview() {
        UUID userId = UUID.randomUUID();
        Long reviewId = 1L;
        Long courseId = 1L;

        Review deletedReview = Review.builder()
                .id(reviewId)
                .uuid(UUID.fromString("8f4b9f1e-4c0a-4c18-9e74-2d5d9e6d1234"))
                .userId(userId)
                .courseId(courseId)
                .rating(5)
                .comment("Excellent")
                .status(ReviewStatus.DELETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UserPrincipal principal = new UserPrincipal(userId, "student@example.com", java.util.List.of("ROLE_STUDENT"));

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(deletedReview));

        com.lms.review.dto.request.UpdateReviewRequest request =
                com.lms.review.dto.request.UpdateReviewRequest.builder()
                        .rating(4)
                        .comment("Updated")
                        .build();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.updateReview(principal, reviewId, request));
        assertEquals("Cannot modify a deleted review", exception.getMessage());
    }

    @Test
    void getCourseRatingSummaryHandlesZeroReviews() {
        Long courseId = 1L;

        when(reviewRepository.averageRatingByCourseIdAndStatus(courseId, ReviewStatus.ACTIVE))
                .thenReturn(null);
        when(reviewRepository.countByCourseIdAndStatus(courseId, ReviewStatus.ACTIVE))
                .thenReturn(0L);
        when(reviewRepository.countRatingsByCourseIdAndStatus(courseId, ReviewStatus.ACTIVE))
                .thenReturn(java.util.Collections.emptyList());

        com.lms.review.dto.response.CourseRatingSummaryResponse summary =
                reviewService.getCourseRatingSummary(courseId);

        assertEquals(courseId, summary.getCourseId());
        assertEquals(0.0, summary.getAverageRating());
        assertEquals(0L, summary.getTotalReviews());
        assertEquals(java.util.Map.of("5", 0L, "4", 0L, "3", 0L, "2", 0L, "1", 0L), summary.getRatingDistribution());
    }
}
