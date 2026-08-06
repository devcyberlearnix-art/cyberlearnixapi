package com.lms.review.service;

import com.lms.review.client.CourseClient;
import com.lms.review.client.EnrollmentCheckResponse;
import com.lms.review.client.EnrollmentClient;
import com.lms.review.client.StudentNameResolver;
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
                UUID courseId = UUID.randomUUID();
                CreateReviewRequest request = CreateReviewRequest.builder()
                                .courseId(courseId)
                                .rating(5)
                                .comment("Great")
                                .build();

                when(reviewRepository.findByUserIdAndCourseId(userId, courseId))
                                .thenReturn(Optional.of(Review.builder().build()));
                when(courseClient.getCourseById(courseId))
                                .thenReturn(new com.lms.review.client.CourseCheckResponse(courseId, "Course"));

                assertThrows(BusinessException.class, () -> reviewService.createReview(userId, request));
        }

        @Test
        void createReviewRejectsWhenStudentIsNotEnrolled() {
                UUID userId = UUID.randomUUID();
                UUID courseId = UUID.randomUUID();
                CreateReviewRequest request = CreateReviewRequest.builder()
                                .courseId(courseId)
                                .rating(4)
                                .comment("Good")
                                .build();

                when(reviewRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());
                when(courseClient.getCourseById(courseId))
                                .thenReturn(new com.lms.review.client.CourseCheckResponse(courseId, "Course"));
                when(enrollmentClient.checkEnrollment(courseId)).thenReturn(new EnrollmentCheckResponse(false));

                assertThrows(BusinessException.class, () -> reviewService.createReview(userId, request));
        }

        @Test
        void createReviewSavesWhenEnrolledAndCourseExists() {
                UUID userId = UUID.randomUUID();
                UUID courseId = UUID.randomUUID();
                CreateReviewRequest request = CreateReviewRequest.builder()
                                .courseId(courseId)
                                .rating(5)
                                .comment("Excellent")
                                .build();

                when(reviewRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());
                when(courseClient.getCourseById(courseId))
                                .thenReturn(new com.lms.review.client.CourseCheckResponse(courseId, "Course"));
                when(enrollmentClient.checkEnrollment(courseId)).thenReturn(new EnrollmentCheckResponse(true));
                when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
                        Review saved = invocation.getArgument(0);
                        saved.setId(1L);
                        saved.setCreatedAt(LocalDateTime.now());
                        return saved;
                });

                reviewService.createReview(userId, request);

                verify(reviewRepository).save(any(Review.class));
        }

        @Test
        void createReviewSavesWhenCourseServiceIsUnavailable() {
                UUID userId = UUID.randomUUID();
                UUID courseId = UUID.randomUUID();
                CreateReviewRequest request = CreateReviewRequest.builder()
                                .courseId(courseId)
                                .rating(5)
                                .comment("Excellent")
                                .build();

                when(reviewRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());
                when(courseClient.getCourseById(courseId)).thenThrow(new RuntimeException("course service down"));
                when(enrollmentClient.checkEnrollment(courseId)).thenReturn(new EnrollmentCheckResponse(true));
                when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
                        Review saved = invocation.getArgument(0);
                        saved.setId(1L);
                        saved.setCreatedAt(LocalDateTime.now());
                        return saved;
                });

                assertDoesNotThrow(() -> reviewService.createReview(userId, request));
                verify(reviewRepository).save(any(Review.class));
        }

        @Test
        void getMyReviewForCourseReturnsReview() {
                UUID userId = UUID.randomUUID();
                UUID courseId = UUID.randomUUID();
                Review review = Review.builder()
                                .id(1L)
                                .uuid(UUID.fromString("8f4b9f1e-4c0a-4c18-9e74-2d5d9e6d1234"))
                                .rating(5)
                                .comment("Excellent course")
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
        UUID courseId = UUID.randomUUID();
        CreateReviewRequest request = CreateReviewRequest.builder()
                .courseId(courseId)
                .rating(5)
                .comment("Excellent")
                .build();

        when(reviewRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.getMyReviewForCourse(userId, courseId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Review not found for the specified course.", exception.getMessage());
    }
