package com.lms.courseservice.service;

import com.lms.courseservice.dto.CourseContentResponse;
import com.lms.courseservice.dto.LectureDetailResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.entity.LectureProgress;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.exception.EnrollmentException;
import com.lms.courseservice.repository.EnrollmentRepository;
import com.lms.courseservice.repository.LectureProgressRepository;
import com.lms.courseservice.repository.SectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.lms.courseservice.entity.Enrollment;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CourseContentServiceTest {

    @Mock
    private CourseService courseService;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private LectureProgressRepository lectureProgressRepository;

    @InjectMocks
    private CourseContentService courseContentService;

    private UUID studentId;
    private Long courseId;
    private Course testCourse;
    private Section testSection1;
    private Section testSection2;
    private Lecture testLecture1;
    private Lecture testLecture2;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        courseId = 1L;

        testCourse = Course.builder()
                .id(courseId)
                .title("Test Course")
                .description("Test Description")
                .thumbnail("http://example.com/thumb.jpg")
                .status("PUBLISHED")
                .price(BigDecimal.valueOf(99.99))
                .instructorId(UUID.randomUUID())
                .build();

        testSection1 = Section.builder()
                .id(1L)
                .title("Section 1")
                .orderIndex(1)
                .course(testCourse)
                .lectures(Arrays.asList())
                .build();

        testSection2 = Section.builder()
                .id(2L)
                .title("Section 2")
                .orderIndex(2)
                .course(testCourse)
                .lectures(Arrays.asList())
                .build();

        testLecture1 = Lecture.builder()
                .id(1L)
                .title("Lecture 1")
                .description("Description 1")
                .videoUrl("http://example.com/video1.mp4")
                .duration(30)
                .orderIndex(1)
                .previewEnabled(false)
                .section(testSection1)
                .build();

        testLecture2 = Lecture.builder()
                .id(2L)
                .title("Lecture 2")
                .description("Description 2")
                .videoUrl("http://example.com/video2.mp4")
                .duration(45)
                .orderIndex(2)
                .previewEnabled(true)
                .section(testSection1)
                .build();

        testSection1.setLectures(Arrays.asList(testLecture1, testLecture2));
    }

    private Enrollment createTestEnrollment(UUID studentId, Long courseId) {
        return Enrollment.builder()
                .id(1L)
                .studentId(studentId)
                .courseId(courseId)
                .status("IN_PROGRESS")
                .progress(50.0)
                .enrolledAt(java.time.LocalDateTime.now())
                .build();
    }

    @Test
    void testGetCourseContent() {
        when(courseService.getCourseById(courseId)).thenReturn(testCourse);
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(true);
        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)).thenReturn(Optional.of(createTestEnrollment(studentId, courseId)));
        when(sectionRepository.findByCourseIdWithLectures(courseId)).thenReturn(Arrays.asList(testSection1, testSection2));
        when(lectureProgressRepository.findByStudentIdAndLectureId(studentId, 1L)).thenReturn(Optional.empty());
        when(lectureProgressRepository.findByStudentIdAndLectureId(studentId, 2L)).thenReturn(Optional.empty());

        CourseContentResponse result = courseContentService.getCourseContent(studentId, courseId);

        assertThat(result).isNotNull();
        assertThat(result.getCourseId()).isEqualTo(courseId);
        assertThat(result.getCourseTitle()).isEqualTo("Test Course");
        assertThat(result.getTotalSections()).isEqualTo(2);
        assertThat(result.getTotalLectures()).isEqualTo(2);
        assertThat(result.getSections()).hasSize(2);
    }

    @Test
    void testGetCourseContentCourseNotFound() {
        when(courseService.getCourseById(courseId)).thenReturn(null);

        org.junit.jupiter.api.Assertions.assertThrows(
                EnrollmentException.class,
                () -> courseContentService.getCourseContent(studentId, courseId)
        );
    }

    @Test
    void testGetCourseContentDraftCourse() {
        testCourse.setStatus("DRAFT");
        when(courseService.getCourseById(courseId)).thenReturn(testCourse);

        org.junit.jupiter.api.Assertions.assertThrows(
                EnrollmentException.class,
                () -> courseContentService.getCourseContent(studentId, courseId)
        );
    }

    @Test
    void testGetCourseContentNotEnrolled() {
        when(courseService.getCourseById(courseId)).thenReturn(testCourse);
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(false);

        org.junit.jupiter.api.Assertions.assertThrows(
                EnrollmentException.class,
                () -> courseContentService.getCourseContent(studentId, courseId)
        );
    }

    @Test
    void testGetLectureDetail() {
        when(courseService.getLectureById(1L)).thenReturn(testLecture1);
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(true);
        when(lectureProgressRepository.findByStudentIdAndLectureId(studentId, 1L)).thenReturn(Optional.empty());
        when(sectionRepository.findByCourseIdWithLectures(courseId)).thenReturn(Arrays.asList(testSection1, testSection2));

        LectureDetailResponse result = courseContentService.getLectureDetail(studentId, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getLectureId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Lecture 1");
        assertThat(result.getSection()).isNotNull();
        assertThat(result.getCourse()).isNotNull();
        assertThat(result.getNavigation()).isNotNull();
    }

    @Test
    void testGetLectureDetailPreviewWithoutEnrollment() {
        testLecture2.setPreviewEnabled(true);
        when(courseService.getLectureById(2L)).thenReturn(testLecture2);
        when(lectureProgressRepository.findByStudentIdAndLectureId(studentId, 2L)).thenReturn(Optional.empty());
        when(sectionRepository.findByCourseIdWithLectures(courseId)).thenReturn(Arrays.asList(testSection1, testSection2));

        LectureDetailResponse result = courseContentService.getLectureDetail(studentId, 2L);

        assertThat(result).isNotNull();
        assertThat(result.getLectureId()).isEqualTo(2L);
        assertThat(result.getPreviewEnabled()).isTrue();
    }

    @Test
    void testGetLectureDetailNotEnrolled() {
        when(courseService.getLectureById(1L)).thenReturn(testLecture1);
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(false);

        org.junit.jupiter.api.Assertions.assertThrows(
                EnrollmentException.class,
                () -> courseContentService.getLectureDetail(studentId, 1L)
        );
    }

    @Test
    void testGetLectureDetailDraftCourse() {
        testCourse.setStatus("DRAFT");
        when(courseService.getLectureById(1L)).thenReturn(testLecture1);

        org.junit.jupiter.api.Assertions.assertThrows(
                EnrollmentException.class,
                () -> courseContentService.getLectureDetail(studentId, 1L)
        );
    }

    @Test
    void testGetLectureDetailWithProgress() {
        LectureProgress progress = LectureProgress.builder()
                .id(1L)
                .studentId(studentId)
                .courseId(courseId)
                .lectureId(1L)
                .status(LectureProgress.LectureStatus.IN_PROGRESS)
                .progressPercentage(50)
                .build();

        when(courseService.getLectureById(1L)).thenReturn(testLecture1);
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(true);
        when(lectureProgressRepository.findByStudentIdAndLectureId(studentId, 1L)).thenReturn(Optional.of(progress));
        when(sectionRepository.findByCourseIdWithLectures(courseId)).thenReturn(Arrays.asList(testSection1, testSection2));

        LectureDetailResponse result = courseContentService.getLectureDetail(studentId, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getProgress()).isNotNull();
        assertThat(result.getProgress().getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(result.getProgress().getProgressPercentage()).isEqualTo(50);
    }

    @Test
    void testGetLectureDetailNavigation() {
        when(courseService.getLectureById(1L)).thenReturn(testLecture1);
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(true);
        when(lectureProgressRepository.findByStudentIdAndLectureId(studentId, 1L)).thenReturn(Optional.empty());
        when(sectionRepository.findByCourseIdWithLectures(courseId)).thenReturn(Arrays.asList(testSection1, testSection2));

        LectureDetailResponse result = courseContentService.getLectureDetail(studentId, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getNavigation()).isNotNull();
        // First lecture should have no previous
        assertThat(result.getNavigation().getPreviousLectureId()).isNull();
        // Should have next lecture
        assertThat(result.getNavigation().getNextLectureId()).isEqualTo(2L);
    }

    @Test
    void testGetLectureDetailLastLectureNavigation() {
        when(courseService.getLectureById(2L)).thenReturn(testLecture2);
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(true);
        when(sectionRepository.findByCourseIdWithLectures(courseId)).thenReturn(Arrays.asList(testSection1, testSection2));

        LectureDetailResponse result = courseContentService.getLectureDetail(studentId, 2L);

        assertThat(result).isNotNull();
        assertThat(result.getNavigation()).isNotNull();
        // Last lecture should have no next
        assertThat(result.getNavigation().getNextLectureId()).isNull();
        // Should have previous lecture
        assertThat(result.getNavigation().getPreviousLectureId()).isEqualTo(1L);
    }
}
