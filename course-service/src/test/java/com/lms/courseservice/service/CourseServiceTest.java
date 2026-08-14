package com.lms.courseservice.service;

import com.lms.courseservice.dto.CourseRatingSummary;
import com.lms.courseservice.dto.FeaturedCourseResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.EnrollmentRepository;
import com.lms.courseservice.repository.LectureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
}