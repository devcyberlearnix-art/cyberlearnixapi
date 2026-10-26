package com.lms.courseservice.service;

import com.lms.courseservice.dto.LectureProgressRequest;
import com.lms.courseservice.dto.LectureProgressResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Enrollment;
import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.entity.LectureProgress;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.exception.EnrollmentException;
import com.lms.courseservice.repository.EnrollmentRepository;
import com.lms.courseservice.repository.LectureProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private LectureProgressRepository lectureProgressRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private ProgressService progressService;

    private UUID testStudentId;
    private Long testCourseId;
    private Long testLectureId;
    private Long testSectionId;
    private Course testCourse;
    private Section testSection;
    private Lecture testLecture;
    private Enrollment testEnrollment;

    @BeforeEach
    void setUp() {
        testStudentId = UUID.randomUUID();
        testCourseId = 1L;
        testLectureId = 10L;
        testSectionId = 100L;

        testSection = Section.builder()
                .id(testSectionId)
                .title("Test Section")
                .orderIndex(1)
                .build();

        testCourse = Course.builder()
                .id(testCourseId)
                .title("Test Course")
                .sections(new ArrayList<>(List.of(testSection)))
                .build();

        testSection.setCourse(testCourse);

        testLecture = Lecture.builder()
                .id(testLectureId)
                .title("Test Lecture")
                .description("Test Description")
                .videoUrl("http://example.com/video.mp4")
                .duration(3600)
                .orderIndex(1)
                .previewEnabled(false)
                .section(testSection)
                .build();

        testEnrollment = Enrollment.builder()
                .id(1L)
                .studentId(testStudentId)
                .courseId(testCourseId)
                .studentName("Test Student")
                .enrolledAt(LocalDateTime.now())
                .status("ACTIVE")
                .progress(0.0)
                .build();
    }

    @Test
    void startLecture_NewProgress_CreatesRecord() {
        // Arrange
        when(courseService.getLectureById(testLectureId)).thenReturn(testLecture);
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(lectureProgressRepository.findByStudentIdAndLectureId(testStudentId, testLectureId))
                .thenReturn(Optional.empty());
        when(lectureProgressRepository.save(any(LectureProgress.class))).thenAnswer(invocation -> {
            LectureProgress saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(enrollmentRepository.findByStudentIdAndCourseId(testStudentId, testCourseId))
                .thenReturn(Optional.of(testEnrollment));

        // Act
        LectureProgressResponse response = progressService.startLecture(testStudentId, testLectureId);

        // Assert
        assertNotNull(response);
        assertEquals(testLectureId, response.getLectureId());
        assertEquals("Test Lecture", response.getTitle());
        assertEquals("IN_PROGRESS", response.getStatus());
        assertEquals(0, response.getProgressPercentage());
        assertNotNull(response.getStartedAt());
        assertNotNull(response.getLastAccessedAt());
        verify(lectureProgressRepository, times(1)).save(any(LectureProgress.class));
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    @Test
    void startLecture_ExistingProgress_UpdatesLastAccessed() {
        // Arrange
        LectureProgress existingProgress = LectureProgress.builder()
                .id(1L)
                .studentId(testStudentId)
                .courseId(testCourseId)
                .lectureId(testLectureId)
                .status(LectureProgress.LectureStatus.IN_PROGRESS)
                .progressPercentage(50)
                .startedAt(LocalDateTime.now().minusDays(1))
                .lastAccessedAt(LocalDateTime.now().minusHours(1))
                .build();

        when(courseService.getLectureById(testLectureId)).thenReturn(testLecture);
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(lectureProgressRepository.findByStudentIdAndLectureId(testStudentId, testLectureId))
                .thenReturn(Optional.of(existingProgress));
        when(lectureProgressRepository.save(any(LectureProgress.class))).thenReturn(existingProgress);

        // Act
        LectureProgressResponse response = progressService.startLecture(testStudentId, testLectureId);

        // Assert
        assertNotNull(response);
        assertEquals("IN_PROGRESS", response.getStatus());
        verify(lectureProgressRepository, times(1)).save(any(LectureProgress.class));
    }

    @Test
    void startLecture_NotEnrolled_ThrowsException() {
        // Arrange
        when(courseService.getLectureById(testLectureId)).thenReturn(testLecture);
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(false);

        // Act & Assert
        assertThrows(EnrollmentException.class, () -> progressService.startLecture(testStudentId, testLectureId));
        verify(lectureProgressRepository, never()).save(any(LectureProgress.class));
    }

    @Test
    void startLecture_LectureNotFound_ThrowsException() {
        // Arrange
        when(courseService.getLectureById(testLectureId)).thenReturn(null);

        // Act & Assert
        assertThrows(EnrollmentException.class, () -> progressService.startLecture(testStudentId, testLectureId));
    }

    @Test
    void updateProgress_ValidPercentage_UpdatesRecord() {
        // Arrange
        LectureProgress existingProgress = LectureProgress.builder()
                .id(1L)
                .studentId(testStudentId)
                .courseId(testCourseId)
                .lectureId(testLectureId)
                .status(LectureProgress.LectureStatus.IN_PROGRESS)
                .progressPercentage(25)
                .startedAt(LocalDateTime.now().minusDays(1))
                .lastAccessedAt(LocalDateTime.now().minusHours(1))
                .build();

        LectureProgressRequest request = new LectureProgressRequest(75);

        when(courseService.getLectureById(testLectureId)).thenReturn(testLecture);
        when(courseService.getCourseById(testCourseId)).thenReturn(testCourse);
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(lectureProgressRepository.findByStudentIdAndLectureId(testStudentId, testLectureId))
                .thenReturn(Optional.of(existingProgress));
        when(lectureProgressRepository.save(any(LectureProgress.class))).thenReturn(existingProgress);
        when(enrollmentRepository.findByStudentIdAndCourseId(testStudentId, testCourseId))
                .thenReturn(Optional.of(testEnrollment));
        when(lectureProgressRepository.countByStudentIdAndCourseIdAndStatus(testStudentId, testCourseId, LectureProgress.LectureStatus.COMPLETED))
                .thenReturn(0L);

        // Act
        LectureProgressResponse response = progressService.updateProgress(testStudentId, testLectureId, request);

        // Assert
        assertNotNull(response);
        assertEquals(75, response.getProgressPercentage());
        verify(lectureProgressRepository, times(1)).save(any(LectureProgress.class));
    }

    @Test
    void updateProgress_InvalidPercentage_ThrowsException() {
        // Arrange
        LectureProgressRequest request = new LectureProgressRequest(150);

        // Act & Assert
        assertThrows(EnrollmentException.class, () -> progressService.updateProgress(testStudentId, testLectureId, request));
    }

    @Test
    void updateProgress_NotStarted_MovesToInProgress() {
        // Arrange
        LectureProgress existingProgress = LectureProgress.builder()
                .id(1L)
                .studentId(testStudentId)
                .courseId(testCourseId)
                .lectureId(testLectureId)
                .status(LectureProgress.LectureStatus.NOT_STARTED)
                .progressPercentage(0)
                .build();

        LectureProgressRequest request = new LectureProgressRequest(30);

        when(courseService.getLectureById(testLectureId)).thenReturn(testLecture);
        when(courseService.getCourseById(testCourseId)).thenReturn(testCourse);
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(lectureProgressRepository.findByStudentIdAndLectureId(testStudentId, testLectureId))
                .thenReturn(Optional.of(existingProgress));
        when(lectureProgressRepository.save(any(LectureProgress.class))).thenReturn(existingProgress);
        when(enrollmentRepository.findByStudentIdAndCourseId(testStudentId, testCourseId))
                .thenReturn(Optional.of(testEnrollment));
        when(lectureProgressRepository.countByStudentIdAndCourseIdAndStatus(testStudentId, testCourseId, LectureProgress.LectureStatus.COMPLETED))
                .thenReturn(0L);

        // Act
        LectureProgressResponse response = progressService.updateProgress(testStudentId, testLectureId, request);

        // Assert
        assertEquals("IN_PROGRESS", response.getStatus());
        assertNotNull(response.getStartedAt());
    }

    @Test
    void updateProgress_CompletedLecture_ThrowsException() {
        // Arrange
        LectureProgress existingProgress = LectureProgress.builder()
                .id(1L)
                .studentId(testStudentId)
                .courseId(testCourseId)
                .lectureId(testLectureId)
                .status(LectureProgress.LectureStatus.COMPLETED)
                .progressPercentage(100)
                .completedAt(LocalDateTime.now())
                .build();

        LectureProgressRequest request = new LectureProgressRequest(50);

        when(courseService.getLectureById(testLectureId)).thenReturn(testLecture);
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(lectureProgressRepository.findByStudentIdAndLectureId(testStudentId, testLectureId))
                .thenReturn(Optional.of(existingProgress));

        // Act & Assert
        assertThrows(EnrollmentException.class, () -> progressService.updateProgress(testStudentId, testLectureId, request));
    }

    @Test
    void completeLecture_NewProgress_MarksAsCompleted() {
        // Arrange
        when(courseService.getLectureById(testLectureId)).thenReturn(testLecture);
        when(courseService.getCourseById(testCourseId)).thenReturn(testCourse);
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(lectureProgressRepository.findByStudentIdAndLectureId(testStudentId, testLectureId))
                .thenReturn(Optional.empty());
        when(lectureProgressRepository.save(any(LectureProgress.class))).thenAnswer(invocation -> {
            LectureProgress saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(enrollmentRepository.findByStudentIdAndCourseId(testStudentId, testCourseId))
                .thenReturn(Optional.of(testEnrollment));
        when(lectureProgressRepository.countByStudentIdAndCourseIdAndStatus(testStudentId, testCourseId, LectureProgress.LectureStatus.COMPLETED))
                .thenReturn(1L);

        // Act
        LectureProgressResponse response = progressService.completeLecture(testStudentId, testLectureId);

        // Assert
        assertNotNull(response);
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(100, response.getProgressPercentage());
        assertNotNull(response.getCompletedAt());
        verify(lectureProgressRepository, times(1)).save(any(LectureProgress.class));
    }

    @Test
    void completeLecture_ExistingProgress_MarksAsCompleted() {
        // Arrange
        LectureProgress existingProgress = LectureProgress.builder()
                .id(1L)
                .studentId(testStudentId)
                .courseId(testCourseId)
                .lectureId(testLectureId)
                .status(LectureProgress.LectureStatus.IN_PROGRESS)
                .progressPercentage(75)
                .startedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(courseService.getLectureById(testLectureId)).thenReturn(testLecture);
        when(courseService.getCourseById(testCourseId)).thenReturn(testCourse);
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(lectureProgressRepository.findByStudentIdAndLectureId(testStudentId, testLectureId))
                .thenReturn(Optional.of(existingProgress));
        when(lectureProgressRepository.save(any(LectureProgress.class))).thenReturn(existingProgress);
        when(enrollmentRepository.findByStudentIdAndCourseId(testStudentId, testCourseId))
                .thenReturn(Optional.of(testEnrollment));
        when(lectureProgressRepository.countByStudentIdAndCourseIdAndStatus(testStudentId, testCourseId, LectureProgress.LectureStatus.COMPLETED))
                .thenReturn(1L);

        // Act
        LectureProgressResponse response = progressService.completeLecture(testStudentId, testLectureId);

        // Assert
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(100, response.getProgressPercentage());
        assertNotNull(response.getCompletedAt());
    }

    @Test
    void getLectureProgress_ExistingProgress_ReturnsProgress() {
        // Arrange
        LectureProgress existingProgress = LectureProgress.builder()
                .id(1L)
                .studentId(testStudentId)
                .courseId(testCourseId)
                .lectureId(testLectureId)
                .status(LectureProgress.LectureStatus.IN_PROGRESS)
                .progressPercentage(50)
                .startedAt(LocalDateTime.now().minusDays(1))
                .lastAccessedAt(LocalDateTime.now().minusHours(1))
                .build();

        when(courseService.getLectureById(testLectureId)).thenReturn(testLecture);
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(lectureProgressRepository.findByStudentIdAndLectureId(testStudentId, testLectureId))
                .thenReturn(Optional.of(existingProgress));

        // Act
        LectureProgressResponse response = progressService.getLectureProgress(testStudentId, testLectureId);

        // Assert
        assertNotNull(response);
        assertEquals(50, response.getProgressPercentage());
        assertEquals("IN_PROGRESS", response.getStatus());
    }

    @Test
    void getLectureProgress_NoProgress_ReturnsNotStarted() {
        // Arrange
        when(courseService.getLectureById(testLectureId)).thenReturn(testLecture);
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(lectureProgressRepository.findByStudentIdAndLectureId(testStudentId, testLectureId))
                .thenReturn(Optional.empty());

        // Act
        LectureProgressResponse response = progressService.getLectureProgress(testStudentId, testLectureId);

        // Assert
        assertNotNull(response);
        assertEquals("NOT_STARTED", response.getStatus());
        assertEquals(0, response.getProgressPercentage());
    }

    @Test
    void getCourseProgress_CalculatesCorrectPercentage() {
        // Arrange
        when(courseService.getCourseById(testCourseId)).thenReturn(testCourse);
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(enrollmentRepository.findByStudentIdAndCourseId(testStudentId, testCourseId))
                .thenReturn(Optional.of(testEnrollment));
        when(lectureProgressRepository.findByStudentIdAndCourseId(testStudentId, testCourseId))
                .thenReturn(new ArrayList<>());
        when(lectureProgressRepository.countByStudentIdAndCourseIdAndStatus(testStudentId, testCourseId, LectureProgress.LectureStatus.COMPLETED))
                .thenReturn(0L);

        // Act
        var response = progressService.getCourseProgress(testStudentId, testCourseId);

        // Assert
        assertNotNull(response);
        assertEquals(testCourseId, response.getCourseId());
        assertEquals("Test Course", response.getTitle());
        assertEquals(0, response.getTotalLectures()); // Course has sections but no lectures in mock
        assertEquals(0, response.getCompletedLectures());
        assertEquals(0, response.getProgressPercentage());
    }

    @Test
    void resumeLearning_WithProgress_ReturnsLastAndNextLecture() {
        // Arrange
        when(courseService.getCourseById(testCourseId)).thenReturn(testCourse);
        when(courseService.getLectureById(testLectureId)).thenReturn(testLecture);
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(enrollmentRepository.findByStudentIdAndCourseId(testStudentId, testCourseId))
                .thenReturn(Optional.of(testEnrollment));

        LectureProgress lastProgress = LectureProgress.builder()
                .id(1L)
                .studentId(testStudentId)
                .courseId(testCourseId)
                .lectureId(testLectureId)
                .status(LectureProgress.LectureStatus.IN_PROGRESS)
                .lastAccessedAt(LocalDateTime.now())
                .build();

        when(lectureProgressRepository.findByStudentIdAndCourseIdOrderByLastAccessedAtDesc(testStudentId, testCourseId))
                .thenReturn(List.of(lastProgress));

        // Act
        var response = progressService.resumeLearning(testStudentId, testCourseId);

        // Assert
        assertNotNull(response);
        assertEquals(testCourseId, response.getCourseId());
        assertEquals(testLectureId, response.getLastLectureId());
        assertEquals("Test Lecture", response.getLastLectureTitle());
    }

    @Test
    void resumeLearning_NoProgress_ReturnsFirstLecture() {
        // Arrange
        when(courseService.getCourseById(testCourseId)).thenReturn(testCourse);
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(enrollmentRepository.findByStudentIdAndCourseId(testStudentId, testCourseId))
                .thenReturn(Optional.of(testEnrollment));
        when(lectureProgressRepository.findByStudentIdAndCourseIdOrderByLastAccessedAtDesc(testStudentId, testCourseId))
                .thenReturn(new ArrayList<>());

        // Act
        var response = progressService.resumeLearning(testStudentId, testCourseId);

        // Assert
        assertNotNull(response);
        assertEquals(testCourseId, response.getCourseId());
        assertNull(response.getLastLectureId());
        assertNull(response.getLastLectureTitle());
    }
}
