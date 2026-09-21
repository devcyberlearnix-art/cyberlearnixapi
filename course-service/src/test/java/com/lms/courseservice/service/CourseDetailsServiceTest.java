package com.lms.courseservice.service;

import com.lms.courseservice.dto.CourseDetailsDTO;
import com.lms.courseservice.dto.CourseRatingSummary;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CourseDetailsServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRequirementsRepository courseRequirementsRepository;

    @Mock
    private LearningOutcomesRepository learningOutcomesRepository;

    @Mock
    private CourseMaterialsRepository courseMaterialsRepository;

    @Mock
    private CourseFAQRepository courseFAQRepository;

    @Mock
    private ReviewRatingClient reviewRatingClient;

    @Mock
    private InstructorProfileClient instructorProfileClient;

    @InjectMocks
    private CourseDetailsService courseDetailsService;

    private Course testCourse;

    @BeforeEach
    void setUp() {
        testCourse = Course.builder()
                .id(1L)
                .title("Test Course")
                .description("Test Description")
                .category("Programming")
                .level("BEGINNER")
                .language("en")
                .price(java.math.BigDecimal.valueOf(99.99))
                .instructorId(UUID.randomUUID())
                .status("PUBLISHED")
                .build();
    }

    @Test
    void testGetCourseDetails_Success() {
        // Mock repository calls
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(sectionRepository.findByCourseIdWithLectures(1L)).thenReturn(java.util.Arrays.asList());
        when(enrollmentRepository.findByStudentIdAndCourseId(any(), any())).thenReturn(Optional.empty());
        CourseRatingSummary ratingSummary = new CourseRatingSummary();
        ratingSummary.setCourseId(1L);
        ratingSummary.setAverageRating(4.5);
        ratingSummary.setTotalReviews(10L);
        when(reviewRatingClient.getCourseRating(1L)).thenReturn(ratingSummary);
        when(instructorProfileClient.getInstructorProfile(any())).thenReturn(
                CourseDetailsDTO.InstructorProfileDTO.builder()
                        .instructorId(UUID.randomUUID())
                        .name("Test Instructor")
                        .email("test@example.com")
                        .build()
        );
        when(courseRequirementsRepository.findByCourseId(1L)).thenReturn(java.util.Arrays.asList());
        when(learningOutcomesRepository.findByCourseId(1L)).thenReturn(java.util.Arrays.asList());
        when(courseMaterialsRepository.findByCourseId(1L)).thenReturn(java.util.Arrays.asList());
        when(courseFAQRepository.findByCourseIdOrderByDisplayOrderAsc(1L)).thenReturn(java.util.Arrays.asList());

        // Call the method
        CourseDetailsDTO details = courseDetailsService.getCourseDetails(1L, null);

        // Verify results
        assertThat(details).isNotNull();
        assertThat(details.getCourseInfo()).isNotNull();
        assertThat(details.getCourseInfo().getId()).isEqualTo(1L);
        assertThat(details.getCourseInfo().getTitle()).isEqualTo("Test Course");
        assertThat(details.getInstructorProfile()).isNotNull();
        assertThat(details.getCurriculum()).isNotNull();
        assertThat(details.getEnrollmentStatus()).isNotNull();
        assertThat(details.getRatingSummary()).isNotNull();
        assertThat(details.getCourseStats()).isNotNull();

        // Verify repository calls
        verify(courseRepository, times(1)).findById(1L);
        verify(sectionRepository, times(1)).findByCourseIdWithLectures(1L);
        verify(reviewRatingClient, times(1)).getCourseRating(1L);
        verify(instructorProfileClient, times(1)).getInstructorProfile(any());
    }

    @Test
    void testGetCourseDetails_CourseNotFound() {
        // Mock repository call
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Call the method and expect exception
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            courseDetailsService.getCourseDetails(999L, null);
        });

        // Verify repository call
        verify(courseRepository, times(1)).findById(999L);
        verify(sectionRepository, never()).findByCourseIdWithLectures(any());
    }

    @Test
    void testGetCourseDetails_WithEnrolledUser() {
        // Mock repository calls
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(sectionRepository.findByCourseIdWithLectures(1L)).thenReturn(java.util.Arrays.asList());
        UUID userId = UUID.randomUUID();
        when(enrollmentRepository.findByStudentIdAndCourseId(userId, 1L)).thenReturn(Optional.empty());
        CourseRatingSummary ratingSummary = new CourseRatingSummary();
        ratingSummary.setCourseId(1L);
        ratingSummary.setAverageRating(4.5);
        ratingSummary.setTotalReviews(10L);
        when(reviewRatingClient.getCourseRating(1L)).thenReturn(ratingSummary);
        when(instructorProfileClient.getInstructorProfile(any())).thenReturn(
                CourseDetailsDTO.InstructorProfileDTO.builder()
                        .instructorId(UUID.randomUUID())
                        .name("Test Instructor")
                        .email("test@example.com")
                        .build()
        );
        when(courseRequirementsRepository.findByCourseId(1L)).thenReturn(java.util.Arrays.asList());
        when(learningOutcomesRepository.findByCourseId(1L)).thenReturn(java.util.Arrays.asList());
        when(courseMaterialsRepository.findByCourseId(1L)).thenReturn(java.util.Arrays.asList());
        when(courseFAQRepository.findByCourseIdOrderByDisplayOrderAsc(1L)).thenReturn(java.util.Arrays.asList());

        // Call the method with user ID
        CourseDetailsDTO details = courseDetailsService.getCourseDetails(1L, userId);

        // Verify results
        assertThat(details).isNotNull();
        assertThat(details.getEnrollmentStatus()).isNotNull();
        assertThat(details.getEnrollmentStatus().getIsEnrolled()).isFalse();

        // Verify repository calls
        verify(enrollmentRepository, times(1)).findByStudentIdAndCourseId(userId, 1L);
    }
}
