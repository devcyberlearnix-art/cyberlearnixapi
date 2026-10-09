package com.lms.courseservice.service;

import com.lms.courseservice.dto.CourseDetailsDTO;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.LectureRepository;
import com.lms.courseservice.repository.SectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurriculumServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private LectureRepository lectureRepository;

    @InjectMocks
    private CurriculumService curriculumService;

    private Course testCourse;
    private Section testSection1;
    private Section testSection2;
    private Lecture testLecture1;
    private Lecture testLecture2;

    @BeforeEach
    void setUp() {
        // Setup test data
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

        testSection1 = Section.builder()
                .id(1L)
                .title("Introduction")
                .orderIndex(1)
                .course(testCourse)
                .build();

        testSection2 = Section.builder()
                .id(2L)
                .title("Advanced Topics")
                .orderIndex(2)
                .course(testCourse)
                .build();

        testLecture1 = Lecture.builder()
                .id(1L)
                .title("Getting Started")
                .description("Introduction to the course")
                .videoUrl("http://example.com/video1.mp4")
                .duration(30)
                .orderIndex(1)
                .previewEnabled(true)
                .section(testSection1)
                .build();

        testLecture2 = Lecture.builder()
                .id(2L)
                .title("Deep Dive")
                .description("Advanced concepts")
                .videoUrl("http://example.com/video2.mp4")
                .duration(45)
                .orderIndex(1)
                .previewEnabled(false)
                .section(testSection2)
                .build();

        // Set up relationships
        testSection1.setLectures(Arrays.asList(testLecture1));
        testSection2.setLectures(Arrays.asList(testLecture2));
    }

    @Test
    void testGetCourseCurriculum_Success() {
        // Mock repository calls
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(sectionRepository.findByCourseIdWithLectures(1L)).thenReturn(Arrays.asList(testSection1, testSection2));

        // Call the method
        CourseDetailsDTO.CourseCurriculumDTO curriculum = curriculumService.getCourseCurriculum(1L);

        // Verify results
        assertThat(curriculum).isNotNull();
        assertThat(curriculum.getCourseId()).isEqualTo(1L);
        assertThat(curriculum.getCourseTitle()).isEqualTo("Test Course");
        assertThat(curriculum.getTotalSections()).isEqualTo(2);
        assertThat(curriculum.getTotalLectures()).isEqualTo(2);
        assertThat(curriculum.getTotalDuration()).isEqualTo(75); // 30 + 45
        assertThat(curriculum.getSections()).hasSize(2);

        // Verify sections are ordered correctly
        assertThat(curriculum.getSections().get(0).getOrderIndex()).isEqualTo(1);
        assertThat(curriculum.getSections().get(1).getOrderIndex()).isEqualTo(2);

        // Verify repository calls
        verify(courseRepository, times(1)).findById(1L);
        verify(sectionRepository, times(1)).findByCourseIdWithLectures(1L);
    }

    @Test
    void testGetCourseCurriculum_CourseNotFound() {
        // Mock repository call
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Call the method and expect exception
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            curriculumService.getCourseCurriculum(999L);
        });

        // Verify repository call
        verify(courseRepository, times(1)).findById(999L);
        verify(sectionRepository, never()).findByCourseIdWithLectures(any());
    }

    @Test
    void testGetCourseCurriculum_EmptySections() {
        // Mock repository calls
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(sectionRepository.findByCourseIdWithLectures(1L)).thenReturn(Arrays.asList());

        // Call the method
        CourseDetailsDTO.CourseCurriculumDTO curriculum = curriculumService.getCourseCurriculum(1L);

        // Verify results
        assertThat(curriculum).isNotNull();
        assertThat(curriculum.getTotalSections()).isEqualTo(0);
        assertThat(curriculum.getTotalLectures()).isEqualTo(0);
        assertThat(curriculum.getTotalDuration()).isEqualTo(0);
        assertThat(curriculum.getSections()).isEmpty();
    }
}
