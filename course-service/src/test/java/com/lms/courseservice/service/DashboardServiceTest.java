package com.lms.courseservice.service;

import com.lms.courseservice.dto.*;
import com.lms.courseservice.entity.*;
import com.lms.courseservice.exception.EnrollmentException;
import com.lms.courseservice.repository.*;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private LectureProgressRepository lectureProgressRepository;

    @Mock
    private AssignmentSubmissionRepository assignmentSubmissionRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private LiveClassAttendanceRepository liveClassAttendanceRepository;

    @Mock
    private LiveClassRepository liveClassRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private CourseService courseService;

    @Mock
    private ProgressService progressService;

    @Mock
    private CertificateService certificateService;

    @InjectMocks
    private DashboardService dashboardService;

    private UUID testStudentId;
    private Long testCourseId;
    private Long testCourseId2;
    private Course testCourse;
    private Course testCourse2;
    private Enrollment testEnrollment;
    private Enrollment testEnrollment2;

    @BeforeEach
    void setUp() {
        testStudentId = UUID.randomUUID();
        testCourseId = 1L;
        testCourseId2 = 2L;

        testCourse = Course.builder()
                .id(testCourseId)
                .title("Test Course 1")
                .sections(new ArrayList<>())
                .build();

        testCourse2 = Course.builder()
                .id(testCourseId2)
                .title("Test Course 2")
                .sections(new ArrayList<>())
                .build();

        testEnrollment = Enrollment.builder()
                .id(1L)
                .studentId(testStudentId)
                .courseId(testCourseId)
                .studentName("Test Student")
                .enrolledAt(LocalDateTime.now().minusDays(10))
                .status("IN_PROGRESS")
                .progress(50.0)
                .lastLectureId(10L)
                .build();

        testEnrollment2 = Enrollment.builder()
                .id(2L)
                .studentId(testStudentId)
                .courseId(testCourseId2)
                .studentName("Test Student")
                .enrolledAt(LocalDateTime.now().minusDays(5))
                .status("COMPLETED")
                .progress(100.0)
                .completedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void getStudentDashboard_WithEnrollments_ReturnsDashboard() {
        // Arrange
        List<Enrollment> enrollments = List.of(testEnrollment, testEnrollment2);
        List<Long> courseIds = List.of(testCourseId, testCourseId2);

        when(enrollmentRepository.findByStudentId(testStudentId)).thenReturn(enrollments);
        when(certificateRepository.findByStudentId(testStudentId)).thenReturn(new ArrayList<>());
        when(quizAttemptRepository.averagePercentageByStudentId(testStudentId)).thenReturn(75.0);
        when(liveClassRepository.countByCourseIds(courseIds)).thenReturn(5L);
        when(liveClassAttendanceRepository.countAttendedByStudentId(testStudentId)).thenReturn(4L);
        when(assignmentRepository.countPublishedByCourseIds(courseIds)).thenReturn(10L);
        when(assignmentSubmissionRepository.countSubmittedByStudentId(testStudentId)).thenReturn(8L);
        when(assignmentSubmissionRepository.countGradedByStudentId(testStudentId)).thenReturn(6L);
        when(assignmentSubmissionRepository.countPendingByStudentId(testStudentId)).thenReturn(2L);
        when(quizAttemptRepository.countByStudentId(testStudentId)).thenReturn(15L);
        when(quizAttemptRepository.countPassedByStudentId(testStudentId)).thenReturn(12L);
        when(quizAttemptRepository.countFailedByStudentId(testStudentId)).thenReturn(3L);
        when(liveClassAttendanceRepository.sumDurationSecondsByStudentId(testStudentId)).thenReturn(7200L);

        when(courseService.getCourseById(testCourseId)).thenReturn(testCourse);
        when(courseService.getCourseById(testCourseId2)).thenReturn(testCourse2);

        CourseProgressResponse progressResponse1 = CourseProgressResponse.builder()
                .courseId(testCourseId)
                .title("Test Course 1")
                .totalLectures(20)
                .completedLectures(10)
                .progressPercentage(50)
                .status("IN_PROGRESS")
                .lastLectureId(10L)
                .lastLectureTitle("Last Lecture")
                .startedAt(testEnrollment.getEnrolledAt())
                .completedAt(null)
                .lastAccessedAt(LocalDateTime.now())
                .build();

        CourseProgressResponse progressResponse2 = CourseProgressResponse.builder()
                .courseId(testCourseId2)
                .title("Test Course 2")
                .totalLectures(15)
                .completedLectures(15)
                .progressPercentage(100)
                .status("COMPLETED")
                .lastLectureId(15L)
                .lastLectureTitle("Final Lecture")
                .startedAt(testEnrollment2.getEnrolledAt())
                .completedAt(testEnrollment2.getCompletedAt())
                .lastAccessedAt(LocalDateTime.now())
                .build();

        when(progressService.getCourseProgress(testStudentId, testCourseId)).thenReturn(progressResponse1);
        when(progressService.getCourseProgress(testStudentId, testCourseId2)).thenReturn(progressResponse2);

        ResumeLearningResponse resumeResponse1 = ResumeLearningResponse.builder()
                .courseId(testCourseId)
                .title("Test Course 1")
                .lastLectureId(10L)
                .lastLectureTitle("Last Lecture")
                .nextLectureId(11L)
                .nextLectureTitle("Next Lecture")
                .status("IN_PROGRESS")
                .build();

        ResumeLearningResponse resumeResponse2 = ResumeLearningResponse.builder()
                .courseId(testCourseId2)
                .title("Test Course 2")
                .lastLectureId(15L)
                .lastLectureTitle("Final Lecture")
                .nextLectureId(null)
                .nextLectureTitle(null)
                .status("COMPLETED")
                .build();

        when(progressService.resumeLearning(testStudentId, testCourseId)).thenReturn(resumeResponse1);
        when(progressService.resumeLearning(testStudentId, testCourseId2)).thenReturn(resumeResponse2);

        // Act
        StudentDashboardResponse response = dashboardService.getStudentDashboard(testStudentId);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getOverview());
        assertEquals(2, response.getOverview().getTotalEnrolledCourses());
        assertEquals(1, response.getOverview().getCompletedCourses());
        assertEquals(1, response.getOverview().getInProgressCourses());
        assertEquals(0, response.getOverview().getTotalCertificates());
        assertEquals(75.0, response.getOverview().getAverageQuizScore());
        assertEquals(80.0, response.getOverview().getOverallAttendancePercentage());

        assertNotNull(response.getCourses());
        assertEquals(2, response.getCourses().size());

        assertNotNull(response.getAssignments());
        assertEquals(10, response.getAssignments().getTotal());
        assertEquals(8, response.getAssignments().getSubmitted());
        assertEquals(6, response.getAssignments().getGraded());
        assertEquals(2, response.getAssignments().getPending());

        assertNotNull(response.getQuizzes());
        assertEquals(15, response.getQuizzes().getTotalAttempts());
        assertEquals(75.0, response.getQuizzes().getAverageScore());
        assertEquals(12, response.getQuizzes().getPassed());
        assertEquals(3, response.getQuizzes().getFailed());

        assertNotNull(response.getLiveClasses());
        assertEquals(5, response.getLiveClasses().getTotalClasses());
        assertEquals(4, response.getLiveClasses().getAttended());
        assertEquals(80.0, response.getLiveClasses().getAttendancePercentage());
        assertEquals(7200L, response.getLiveClasses().getTotalDurationSeconds());

        assertNotNull(response.getCertificates());
        assertTrue(response.getCertificates().isEmpty());

        assertNotNull(response.getRecentActivity());
        assertTrue(response.getRecentActivity().isEmpty());
    }

    @Test
    void getStudentDashboard_NoEnrollments_ReturnsEmptyDashboard() {
        // Arrange
        when(enrollmentRepository.findByStudentId(testStudentId)).thenReturn(new ArrayList<>());
        when(certificateRepository.findByStudentId(testStudentId)).thenReturn(new ArrayList<>());
        when(quizAttemptRepository.averagePercentageByStudentId(testStudentId)).thenReturn(null);
        when(quizAttemptRepository.countByStudentId(testStudentId)).thenReturn(0L);
        when(quizAttemptRepository.countPassedByStudentId(testStudentId)).thenReturn(0L);
        when(quizAttemptRepository.countFailedByStudentId(testStudentId)).thenReturn(0L);

        // Act
        StudentDashboardResponse response = dashboardService.getStudentDashboard(testStudentId);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getOverview());
        assertEquals(0, response.getOverview().getTotalEnrolledCourses());
        assertEquals(0, response.getOverview().getCompletedCourses());
        assertEquals(0, response.getOverview().getInProgressCourses());
        assertEquals(0, response.getOverview().getTotalCertificates());
        assertNull(response.getOverview().getAverageQuizScore());
        assertEquals(0.0, response.getOverview().getOverallAttendancePercentage());

        assertNotNull(response.getCourses());
        assertTrue(response.getCourses().isEmpty());

        assertNotNull(response.getAssignments());
        assertEquals(0, response.getAssignments().getTotal());
        assertEquals(0, response.getAssignments().getSubmitted());
        assertEquals(0, response.getAssignments().getGraded());
        assertEquals(0, response.getAssignments().getPending());

        assertNotNull(response.getQuizzes());
        assertEquals(0, response.getQuizzes().getTotalAttempts());
        assertNull(response.getQuizzes().getAverageScore());
        assertEquals(0, response.getQuizzes().getPassed());
        assertEquals(0, response.getQuizzes().getFailed());

        assertNotNull(response.getLiveClasses());
        assertEquals(0, response.getLiveClasses().getTotalClasses());
        assertEquals(0, response.getLiveClasses().getAttended());
        assertEquals(0.0, response.getLiveClasses().getAttendancePercentage());
        assertEquals(0L, response.getLiveClasses().getTotalDurationSeconds());
    }

    @Test
    void getStudentCourses_WithEnrollments_ReturnsCourses() {
        // Arrange
        List<Enrollment> enrollments = List.of(testEnrollment, testEnrollment2);

        when(enrollmentRepository.findByStudentId(testStudentId)).thenReturn(enrollments);
        when(courseService.getCourseById(testCourseId)).thenReturn(testCourse);
        when(courseService.getCourseById(testCourseId2)).thenReturn(testCourse2);

        CourseProgressResponse progressResponse1 = CourseProgressResponse.builder()
                .courseId(testCourseId)
                .title("Test Course 1")
                .totalLectures(20)
                .completedLectures(10)
                .progressPercentage(50)
                .status("IN_PROGRESS")
                .lastLectureId(10L)
                .lastLectureTitle("Last Lecture")
                .startedAt(testEnrollment.getEnrolledAt())
                .completedAt(null)
                .lastAccessedAt(LocalDateTime.now())
                .build();

        CourseProgressResponse progressResponse2 = CourseProgressResponse.builder()
                .courseId(testCourseId2)
                .title("Test Course 2")
                .totalLectures(15)
                .completedLectures(15)
                .progressPercentage(100)
                .status("COMPLETED")
                .lastLectureId(15L)
                .lastLectureTitle("Final Lecture")
                .startedAt(testEnrollment2.getEnrolledAt())
                .completedAt(testEnrollment2.getCompletedAt())
                .lastAccessedAt(LocalDateTime.now())
                .build();

        when(progressService.getCourseProgress(testStudentId, testCourseId)).thenReturn(progressResponse1);
        when(progressService.getCourseProgress(testStudentId, testCourseId2)).thenReturn(progressResponse2);

        ResumeLearningResponse resumeResponse1 = ResumeLearningResponse.builder()
                .courseId(testCourseId)
                .title("Test Course 1")
                .lastLectureId(10L)
                .lastLectureTitle("Last Lecture")
                .nextLectureId(11L)
                .nextLectureTitle("Next Lecture")
                .status("IN_PROGRESS")
                .build();

        ResumeLearningResponse resumeResponse2 = ResumeLearningResponse.builder()
                .courseId(testCourseId2)
                .title("Test Course 2")
                .lastLectureId(15L)
                .lastLectureTitle("Final Lecture")
                .nextLectureId(null)
                .nextLectureTitle(null)
                .status("COMPLETED")
                .build();

        when(progressService.resumeLearning(testStudentId, testCourseId)).thenReturn(resumeResponse1);
        when(progressService.resumeLearning(testStudentId, testCourseId2)).thenReturn(resumeResponse2);

        // Act
        DashboardCourseResponse response = dashboardService.getStudentCourses(testStudentId);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getCourses());
        assertEquals(2, response.getCourses().size());

        // Find the course by ID since sorting may change order
        DashboardCourse course1 = response.getCourses().stream()
                .filter(c -> c.getCourseId().equals(testCourseId))
                .findFirst()
                .orElse(null);
        assertNotNull(course1);
        assertEquals(testCourseId, course1.getCourseId());
        assertEquals("Test Course 1", course1.getCourseTitle());
        assertEquals(50.0, course1.getProgressPercentage());
        assertEquals(10, course1.getCompletedLectures());
        assertEquals(20, course1.getTotalLectures());
        assertEquals("IN_PROGRESS", course1.getStatus());
        assertEquals(10L, course1.getLastLectureId());
        assertEquals(11L, course1.getResumeLectureId());
    }

    @Test
    void getCourseDashboard_EnrolledStudent_ReturnsCourseDashboard() {
        // Arrange
        when(enrollmentRepository.findByStudentIdAndCourseId(testStudentId, testCourseId))
                .thenReturn(Optional.of(testEnrollment));
        when(courseService.getCourseById(testCourseId)).thenReturn(testCourse);

        CourseProgressResponse progressResponse = CourseProgressResponse.builder()
                .courseId(testCourseId)
                .title("Test Course 1")
                .totalLectures(20)
                .completedLectures(10)
                .progressPercentage(50)
                .status("IN_PROGRESS")
                .lastLectureId(10L)
                .lastLectureTitle("Last Lecture")
                .startedAt(testEnrollment.getEnrolledAt())
                .completedAt(null)
                .lastAccessedAt(LocalDateTime.now())
                .build();

        when(progressService.getCourseProgress(testStudentId, testCourseId)).thenReturn(progressResponse);

        ResumeLearningResponse resumeResponse = ResumeLearningResponse.builder()
                .courseId(testCourseId)
                .title("Test Course 1")
                .lastLectureId(10L)
                .lastLectureTitle("Last Lecture")
                .nextLectureId(11L)
                .nextLectureTitle("Next Lecture")
                .status("IN_PROGRESS")
                .build();

        when(progressService.resumeLearning(testStudentId, testCourseId)).thenReturn(resumeResponse);

        when(assignmentRepository.findByCourseIdAndStatus(testCourseId, Assignment.AssignmentStatus.PUBLISHED))
                .thenReturn(new ArrayList<>());
        when(quizAttemptRepository.findByStudentId(testStudentId)).thenReturn(new ArrayList<>());
        when(liveClassRepository.findByCourseId(testCourseId)).thenReturn(new ArrayList<>());
        when(certificateRepository.findByStudentIdAndCourseIdAndStatus(
                testStudentId, testCourseId, Certificate.CertificateStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // Act
        DashboardCourseDetailResponse response = dashboardService.getCourseDashboard(testStudentId, testCourseId);

        // Assert
        assertNotNull(response);
        assertEquals(testCourseId, response.getCourseId());
        assertEquals("Test Course 1", response.getCourseTitle());
        assertEquals(50.0, response.getProgressPercentage());
        assertEquals(10, response.getCompletedLectures());
        assertEquals(20, response.getTotalLectures());
        assertEquals("IN_PROGRESS", response.getStatus());
        assertEquals(10L, response.getLastLectureId());
        assertEquals(11L, response.getResumeLectureId());
        assertEquals("Last Lecture", response.getLastLectureTitle());
        assertEquals("Next Lecture", response.getResumeLectureTitle());

        assertNotNull(response.getAssignments());
        assertNotNull(response.getQuizzes());
        assertNotNull(response.getLiveClasses());
        assertNull(response.getCertificate());
    }

    @Test
    void getCourseDashboard_NotEnrolled_ThrowsException() {
        // Arrange
        when(enrollmentRepository.findByStudentIdAndCourseId(testStudentId, testCourseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EnrollmentException.class, 
                () -> dashboardService.getCourseDashboard(testStudentId, testCourseId));
    }

    @Test
    void getCourseDashboard_CourseNotFound_ThrowsException() {
        // Arrange
        when(enrollmentRepository.findByStudentIdAndCourseId(testStudentId, testCourseId))
                .thenReturn(Optional.of(testEnrollment));
        when(courseService.getCourseById(testCourseId)).thenReturn(null);

        // Act & Assert
        assertThrows(EnrollmentException.class, 
                () -> dashboardService.getCourseDashboard(testStudentId, testCourseId));
    }
}
