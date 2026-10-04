package com.lms.courseservice.service;

import com.lms.courseservice.dto.CourseRatingSummary;
import com.lms.courseservice.dto.FeaturedCourseResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.EnrollmentRepository;
import com.lms.courseservice.repository.LectureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private ReviewRatingClient reviewRatingClient;

    @InjectMocks
    private CourseService courseService;

        @Test
        void getAllCoursesReturnsCoursesFromRepository() {
                Course javaCourse = Course.builder().id(1L).title("Java Course").build();
                Course pythonCourse = Course.builder().id(2L).title("Python Course").build();
                org.springframework.data.domain.Page<Course> page = new org.springframework.data.domain.PageImpl<>(List.of(javaCourse, pythonCourse));
                
                when(courseRepository.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

                org.springframework.data.domain.Page<Course> result = courseService.getAllCourses(0, 10, "id", org.springframework.data.domain.Sort.Direction.ASC);

                org.assertj.core.api.Assertions.assertThat(result.getContent()).containsExactly(javaCourse, pythonCourse);
                verify(courseRepository).findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class));
        }

        @Test
        void getCourseByIdReturnsMatchingCourse() {
                Course course = Course.builder().id(253L).title("AI Engineer Bootcamp").build();
                when(courseRepository.findById(253L)).thenReturn(Optional.of(course));

                Course result = courseService.getCourseById(253L);

                assertThat(result).isSameAs(course);
                verify(courseRepository).findById(253L);
        }

    @Test
    void featuredCoursesRankPublishedCoursesByBusinessSignals() {
        Course premiumCourse = Course.builder()
                .id(101L)
                .title("Premium course")
                .status("PUBLISHED")
                .premium(true)
                .searchCount(80L)
                .viewCount(150L)
                .build();
        Course standardCourse = Course.builder()
                .id(102L)
                .title("Standard course")
                .status("PUBLISHED")
                .premium(false)
                .searchCount(5L)
                .viewCount(10L)
                .build();

        when(courseRepository.findByStatusIgnoreCase("PUBLISHED"))
                .thenReturn(List.of(standardCourse, premiumCourse));
        when(enrollmentRepository.countByCourseId(101L)).thenReturn(25L);
        when(enrollmentRepository.countByCourseId(102L)).thenReturn(3L);
        when(reviewRatingClient.getCourseRating(101L))
                .thenReturn(new CourseRatingSummary(101L, 4.8, 30L));
        when(reviewRatingClient.getCourseRating(102L))
                .thenReturn(new CourseRatingSummary(102L, 4.9, 1L));

        List<FeaturedCourseResponse> result = courseService.getFeaturedCourses(1);

        assertThat(result).singleElement().satisfies(course -> {
            assertThat(course.getId()).isEqualTo(101L);
            assertThat(course.isPremium()).isTrue();
            assertThat(course.getStudents()).isEqualTo(25L);
            assertThat(course.getRating()).isEqualTo(4.8);
            assertThat(course.getTag()).isEqualTo("Premium");
        });
    }

        @Test
        void missingCourseLookupReturnsNotFound() {
                when(courseRepository.findById(404L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> courseService.getCourseById(404L))
                                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                                                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
        }

        @Test
        void trackImpression_withSearchSource_incrementsBoth() {
                when(courseRepository.incrementSearchAndViewCount(1L)).thenReturn(1);
                
                courseService.trackImpression(1L, "SEARCH");
                
                verify(courseRepository).incrementSearchAndViewCount(1L);
        }

        @Test
        void trackImpression_withNonSearchSource_incrementsViewOnly() {
                when(courseRepository.incrementViewCount(2L)).thenReturn(1);
                
                courseService.trackImpression(2L, "OTHER");
                
                verify(courseRepository).incrementViewCount(2L);
        }

        @Test
        void trackImpression_courseNotFound_throwsException() {
                when(courseRepository.incrementViewCount(99L)).thenReturn(0);
                
                assertThatThrownBy(() -> courseService.trackImpression(99L, "OTHER"))
                                .isInstanceOf(RuntimeException.class)
                                .hasMessageContaining("Course not found with id: 99");
        }
}
