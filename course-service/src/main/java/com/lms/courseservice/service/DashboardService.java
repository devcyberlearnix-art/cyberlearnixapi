package com.lms.courseservice.service;

import com.lms.courseservice.dto.*;
import com.lms.courseservice.entity.*;
import com.lms.courseservice.exception.EnrollmentException;
import com.lms.courseservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EnrollmentRepository enrollmentRepository;
    private final LectureProgressRepository lectureProgressRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final LiveClassAttendanceRepository liveClassAttendanceRepository;
    private final LiveClassRepository liveClassRepository;
    private final AssignmentRepository assignmentRepository;
    private final CertificateRepository certificateRepository;
    private final QuizRepository quizRepository;
    private final CourseService courseService;
    private final ProgressService progressService;
    private final CertificateService certificateService;

    /**
     * Get student dashboard overview
     */
    @Transactional(readOnly = true)
    public StudentDashboardResponse getStudentDashboard(UUID studentId) {
        // Get all enrollments for the student
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<Long> courseIds = enrollments.stream()
                .map(Enrollment::getCourseId)
                .collect(Collectors.toList());

        // Build overview
        DashboardOverview overview = buildOverview(studentId, enrollments, courseIds);

        // Build courses list
        List<DashboardCourse> courses = buildCourses(studentId, enrollments);

        // Build assignments statistics
        DashboardAssignments assignments = buildAssignments(studentId, courseIds);

        // Build quizzes statistics
        DashboardQuizzes quizzes = buildQuizzes(studentId);

        // Build live classes statistics
        DashboardLiveClasses liveClasses = buildLiveClasses(studentId, courseIds);

        // Build certificates
        List<DashboardCertificate> certificates = buildCertificates(studentId);

        // Recent activity - for now, return empty list (can be enhanced later)
        List<DashboardRecentActivity> recentActivity = List.of();

        return StudentDashboardResponse.builder()
                .overview(overview)
                .courses(courses)
                .assignments(assignments)
                .quizzes(quizzes)
                .liveClasses(liveClasses)
                .certificates(certificates)
                .recentActivity(recentActivity)
                .build();
    }

    /**
     * Get student's enrolled courses with progress
     */
    @Transactional(readOnly = true)
    public DashboardCourseResponse getStudentCourses(UUID studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<DashboardCourse> courses = buildCourses(studentId, enrollments);

        return DashboardCourseResponse.builder()
                .courses(courses)
                .build();
    }

    /**
     * Get course-specific dashboard for a student
     */
    @Transactional(readOnly = true)
    public DashboardCourseDetailResponse getCourseDashboard(UUID studentId, Long courseId) {
        // Get course details first
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Course not found with id: " + courseId);
        }

        // Validate enrollment
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new EnrollmentException("You are not enrolled in this course"));

        // Get course progress
        CourseProgressResponse courseProgress = progressService.getCourseProgress(studentId, courseId);

        // Get resume learning info
        ResumeLearningResponse resumeLearning = progressService.resumeLearning(studentId, courseId);

        // Build assignments statistics for this course
        DashboardAssignments assignments = buildCourseAssignments(studentId, courseId);

        // Build quizzes statistics for this course
        DashboardQuizzes quizzes = buildCourseQuizzes(studentId, courseId);

        // Build live classes statistics for this course
        DashboardLiveClasses liveClasses = buildCourseLiveClasses(studentId, courseId);

        // Get certificate for this course if exists
        DashboardCertificate certificate = buildCourseCertificate(studentId, courseId);

        return DashboardCourseDetailResponse.builder()
                .courseId(courseId)
                .courseTitle(course.getTitle())
                .progressPercentage(courseProgress.getProgressPercentage() != null 
                    ? courseProgress.getProgressPercentage().doubleValue() 
                    : enrollment.getProgress())
                .completedLectures(courseProgress.getCompletedLectures())
                .totalLectures(courseProgress.getTotalLectures())
                .status(enrollment.getStatus())
                .enrolledAt(enrollment.getEnrolledAt())
                .completedAt(enrollment.getCompletedAt())
                .lastAccessedAt(courseProgress.getLastAccessedAt())
                .lastLectureId(courseProgress.getLastLectureId())
                .resumeLectureId(resumeLearning.getNextLectureId())
                .lastLectureTitle(courseProgress.getLastLectureTitle())
                .resumeLectureTitle(resumeLearning.getNextLectureTitle())
                .assignments(assignments)
                .quizzes(quizzes)
                .liveClasses(liveClasses)
                .certificate(certificate)
                .build();
    }

    // Helper methods

    private DashboardOverview buildOverview(UUID studentId, List<Enrollment> enrollments, List<Long> courseIds) {
        int totalEnrolledCourses = enrollments.size();
        int completedCourses = (int) enrollments.stream()
                .filter(e -> "COMPLETED".equals(e.getStatus()))
                .count();
        int inProgressCourses = totalEnrolledCourses - completedCourses;

        int totalCertificates = (int) certificateRepository.findByStudentId(studentId).stream()
                .filter(c -> c.getStatus() == Certificate.CertificateStatus.ACTIVE)
                .count();

        Double averageQuizScore = quizAttemptRepository.averagePercentageByStudentId(studentId);

        long totalLiveClasses = courseIds.isEmpty() ? 0 : liveClassRepository.countByCourseIds(courseIds);
        long attendedClasses = liveClassAttendanceRepository.countAttendedByStudentId(studentId);
        double overallAttendancePercentage = totalLiveClasses > 0 
                ? ((double) attendedClasses / totalLiveClasses) * 100.0 
                : 0.0;

        return DashboardOverview.builder()
                .totalEnrolledCourses(totalEnrolledCourses)
                .completedCourses(completedCourses)
                .inProgressCourses(inProgressCourses)
                .totalCertificates(totalCertificates)
                .averageQuizScore(averageQuizScore)
                .overallAttendancePercentage(overallAttendancePercentage)
                .build();
    }

    private List<DashboardCourse> buildCourses(UUID studentId, List<Enrollment> enrollments) {
        return enrollments.stream()
                .map(enrollment -> {
                    Course course = courseService.getCourseById(enrollment.getCourseId());
                    if (course == null) {
                        return null;
                    }

                    CourseProgressResponse progress = progressService.getCourseProgress(studentId, enrollment.getCourseId());
                    ResumeLearningResponse resume = progressService.resumeLearning(studentId, enrollment.getCourseId());

                    return DashboardCourse.builder()
                            .courseId(enrollment.getCourseId())
                            .courseTitle(course.getTitle())
                            .progressPercentage(enrollment.getProgress())
                            .completedLectures(progress.getCompletedLectures())
                            .totalLectures(progress.getTotalLectures())
                            .status(enrollment.getStatus())
                            .lastLectureId(enrollment.getLastLectureId())
                            .resumeLectureId(resume.getNextLectureId())
                            .enrolledAt(enrollment.getEnrolledAt())
                            .completedAt(enrollment.getCompletedAt())
                            .lastAccessedAt(progress.getLastAccessedAt())
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(DashboardCourse::getLastAccessedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    private DashboardAssignments buildAssignments(UUID studentId, List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return DashboardAssignments.builder()
                    .total(0)
                    .submitted(0)
                    .graded(0)
                    .pending(0)
                    .build();
        }

        long total = assignmentRepository.countPublishedByCourseIds(courseIds);
        long submitted = assignmentSubmissionRepository.countSubmittedByStudentId(studentId);
        long graded = assignmentSubmissionRepository.countGradedByStudentId(studentId);
        long pending = assignmentSubmissionRepository.countPendingByStudentId(studentId);

        return DashboardAssignments.builder()
                .total((int) total)
                .submitted((int) submitted)
                .graded((int) graded)
                .pending((int) pending)
                .build();
    }

    private DashboardQuizzes buildQuizzes(UUID studentId) {
        long totalAttempts = quizAttemptRepository.countByStudentId(studentId);
        Double averageScore = quizAttemptRepository.averagePercentageByStudentId(studentId);
        long passed = quizAttemptRepository.countPassedByStudentId(studentId);
        long failed = quizAttemptRepository.countFailedByStudentId(studentId);

        return DashboardQuizzes.builder()
                .totalAttempts((int) totalAttempts)
                .averageScore(averageScore)
                .passed((int) passed)
                .failed((int) failed)
                .build();
    }

    private DashboardLiveClasses buildLiveClasses(UUID studentId, List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return DashboardLiveClasses.builder()
                    .totalClasses(0)
                    .attended(0)
                    .attendancePercentage(0.0)
                    .totalDurationSeconds(0L)
                    .build();
        }

        long totalClasses = liveClassRepository.countByCourseIds(courseIds);
        long attended = liveClassAttendanceRepository.countAttendedByStudentId(studentId);
        Long totalDurationSeconds = liveClassAttendanceRepository.sumDurationSecondsByStudentId(studentId);
        double attendancePercentage = totalClasses > 0 
                ? ((double) attended / totalClasses) * 100.0 
                : 0.0;

        return DashboardLiveClasses.builder()
                .totalClasses((int) totalClasses)
                .attended((int) attended)
                .attendancePercentage(attendancePercentage)
                .totalDurationSeconds(totalDurationSeconds != null ? totalDurationSeconds : 0L)
                .build();
    }

    private List<DashboardCertificate> buildCertificates(UUID studentId) {
        return certificateRepository.findByStudentId(studentId).stream()
                .filter(c -> c.getStatus() == Certificate.CertificateStatus.ACTIVE)
                .map(certificate -> DashboardCertificate.builder()
                        .certificateId(certificate.getId())
                        .courseId(certificate.getCourseId())
                        .courseTitle(certificate.getCourseTitle())
                        .certificateNumber(certificate.getCertificateNumber())
                        .issuedAt(certificate.getIssuedAt())
                        .build())
                .sorted(Comparator.comparing(DashboardCertificate::getIssuedAt, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    private DashboardAssignments buildCourseAssignments(UUID studentId, Long courseId) {
        List<Assignment> assignments = assignmentRepository.findByCourseIdAndStatus(courseId, Assignment.AssignmentStatus.PUBLISHED);
        int total = assignments.size();

        List<Long> assignmentIds = assignments.stream()
                .map(Assignment::getId)
                .collect(Collectors.toList());

        int submitted = 0;
        int graded = 0;
        int pending = 0;

        for (Long assignmentId : assignmentIds) {
            var submission = assignmentSubmissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
            if (submission.isPresent()) {
                AssignmentSubmission.SubmissionStatus status = submission.get().getStatus();
                if (status == AssignmentSubmission.SubmissionStatus.SUBMITTED) {
                    submitted++;
                } else if (status == AssignmentSubmission.SubmissionStatus.GRADED) {
                    graded++;
                } else {
                    pending++;
                }
            } else {
                pending++;
            }
        }

        return DashboardAssignments.builder()
                .total(total)
                .submitted(submitted)
                .graded(graded)
                .pending(pending)
                .build();
    }

    private DashboardQuizzes buildCourseQuizzes(UUID studentId, Long courseId) {
        // Get all quiz attempts for the student in this course
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(studentId).stream()
                .filter(attempt -> {
                    Quiz quiz = quizRepository.findById(attempt.getQuizId()).orElse(null);
                    return quiz != null && quiz.getCourseId().equals(courseId);
                })
                .collect(Collectors.toList());

        int totalAttempts = attempts.size();
        double averageScore = attempts.stream()
                .filter(a -> a.getPercentage() != null)
                .mapToInt(QuizAttempt::getPercentage)
                .average()
                .orElse(0.0);

        int passed = (int) attempts.stream()
                .filter(a -> a.getPassed() != null && a.getPassed())
                .count();

        int failed = totalAttempts - passed;

        return DashboardQuizzes.builder()
                .totalAttempts(totalAttempts)
                .averageScore(averageScore)
                .passed(passed)
                .failed(failed)
                .build();
    }

    private DashboardLiveClasses buildCourseLiveClasses(UUID studentId, Long courseId) {
        List<LiveClass> liveClasses = liveClassRepository.findByCourseId(courseId);
        int totalClasses = liveClasses.size();

        int attended = 0;
        long totalDurationSeconds = 0L;

        for (LiveClass liveClass : liveClasses) {
            var attendance = liveClassAttendanceRepository.findByLiveClassIdAndStudentId(liveClass.getId(), studentId);
            if (attendance.isPresent() && attendance.get().getStatus() == LiveClassAttendance.AttendanceStatus.ATTENDED) {
                attended++;
                if (attendance.get().getDurationSeconds() != null) {
                    totalDurationSeconds += attendance.get().getDurationSeconds();
                }
            }
        }

        double attendancePercentage = totalClasses > 0 
                ? ((double) attended / totalClasses) * 100.0 
                : 0.0;

        return DashboardLiveClasses.builder()
                .totalClasses(totalClasses)
                .attended(attended)
                .attendancePercentage(attendancePercentage)
                .totalDurationSeconds(totalDurationSeconds)
                .build();
    }

    private DashboardCertificate buildCourseCertificate(UUID studentId, Long courseId) {
        var certificate = certificateRepository.findByStudentIdAndCourseIdAndStatus(
                studentId, courseId, Certificate.CertificateStatus.ACTIVE);

        if (certificate.isPresent()) {
            Certificate cert = certificate.get();
            return DashboardCertificate.builder()
                    .certificateId(cert.getId())
                    .courseId(cert.getCourseId())
                    .courseTitle(cert.getCourseTitle())
                    .certificateNumber(cert.getCertificateNumber())
                    .issuedAt(cert.getIssuedAt())
                    .build();
        }

        return null;
    }
}
