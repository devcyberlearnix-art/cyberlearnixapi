package com.lms.courseservice.service;

import com.lms.courseservice.dto.CourseProgressResponse;
import com.lms.courseservice.dto.LectureProgressRequest;
import com.lms.courseservice.dto.LectureProgressResponse;
import com.lms.courseservice.dto.ResumeLearningResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Enrollment;
import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.entity.LectureProgress;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.exception.EnrollmentException;
import com.lms.courseservice.repository.EnrollmentRepository;
import com.lms.courseservice.repository.LectureProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressService {

    private final LectureProgressRepository lectureProgressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;

    /**
     * Start a lecture for a student
     */
    @Transactional
    public LectureProgressResponse startLecture(UUID studentId, Long lectureId) {
        // Validate lecture exists and get course info
        Lecture lecture = courseService.getLectureById(lectureId);
        if (lecture == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecture not found with id: " + lectureId);
        }

        Long courseId = getCourseIdFromLecture(lecture);
        validateEnrollment(studentId, courseId);

        // Check if progress already exists
        LectureProgress progress = lectureProgressRepository
                .findByStudentIdAndLectureId(studentId, lectureId)
                .orElse(null);

        if (progress == null) {
            // Create new progress record
            progress = LectureProgress.builder()
                    .studentId(studentId)
                    .courseId(courseId)
                    .lectureId(lectureId)
                    .status(LectureProgress.LectureStatus.IN_PROGRESS)
                    .progressPercentage(0)
                    .startedAt(LocalDateTime.now())
                    .lastAccessedAt(LocalDateTime.now())
                    .build();
            progress = lectureProgressRepository.save(progress);
            
            // Update enrollment status if NOT_STARTED
            updateEnrollmentStatus(studentId, courseId, "IN_PROGRESS");
        } else {
            // Update last accessed time
            progress.setLastAccessedAt(LocalDateTime.now());
            if (progress.getStatus() == LectureProgress.LectureStatus.NOT_STARTED) {
                progress.setStatus(LectureProgress.LectureStatus.IN_PROGRESS);
            }
            progress = lectureProgressRepository.save(progress);
        }

        return mapToLectureProgressResponse(progress, lecture);
    }

    /**
     * Update lecture progress
     */
    @Transactional
    public LectureProgressResponse updateProgress(UUID studentId, Long lectureId, LectureProgressRequest request) {
        Lecture lecture = courseService.getLectureById(lectureId);
        if (lecture == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecture not found with id: " + lectureId);
        }

        if (request == null) {
            throw new IllegalArgumentException("Progress percentage must be between 0 and 100");
        }
        validateProgressPercentage(request.getProgressPercentage());

        Long courseId = getCourseIdFromLecture(lecture);
        validateEnrollment(studentId, courseId);

        LectureProgress progress = lectureProgressRepository
                .findByStudentIdAndLectureId(studentId, lectureId)
                .orElseThrow(() -> new EnrollmentException("No progress record found. Start the lecture first."));

        // Prevent updates to completed lectures
        if (progress.getStatus() == LectureProgress.LectureStatus.COMPLETED) {
            throw new EnrollmentException("Cannot update progress for a completed lecture");
        }

        progress.setProgressPercentage(request.getProgressPercentage());
        progress.setLastAccessedAt(LocalDateTime.now());
        
        if (progress.getStatus() == LectureProgress.LectureStatus.NOT_STARTED) {
            progress.setStatus(LectureProgress.LectureStatus.IN_PROGRESS);
            if (progress.getStartedAt() == null) {
                progress.setStartedAt(LocalDateTime.now());
            }
        }

        progress = lectureProgressRepository.save(progress);
        updateCourseProgress(studentId, courseId);

        return mapToLectureProgressResponse(progress, lecture);
    }

    /**
     * Complete a lecture
     */
    @Transactional
    public LectureProgressResponse completeLecture(UUID studentId, Long lectureId) {
        Lecture lecture = courseService.getLectureById(lectureId);
        if (lecture == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecture not found with id: " + lectureId);
        }

        Long courseId = getCourseIdFromLecture(lecture);
        validateEnrollment(studentId, courseId);

        LectureProgress progress = lectureProgressRepository
                .findByStudentIdAndLectureId(studentId, lectureId)
                .orElse(null);

        if (progress == null) {
            // Create and complete in one operation
            progress = LectureProgress.builder()
                    .studentId(studentId)
                    .courseId(courseId)
                    .lectureId(lectureId)
                    .status(LectureProgress.LectureStatus.COMPLETED)
                    .progressPercentage(100)
                    .startedAt(LocalDateTime.now())
                    .completedAt(LocalDateTime.now())
                    .lastAccessedAt(LocalDateTime.now())
                    .build();
        } else {
            // Mark as completed
            progress.setStatus(LectureProgress.LectureStatus.COMPLETED);
            progress.setProgressPercentage(100);
            progress.setCompletedAt(LocalDateTime.now());
            progress.setLastAccessedAt(LocalDateTime.now());
        }

        progress = lectureProgressRepository.save(progress);
        updateCourseProgress(studentId, courseId);

        return mapToLectureProgressResponse(progress, lecture);
    }

    /**
     * Get lecture progress
     */
    @Transactional(readOnly = true)
    public LectureProgressResponse getLectureProgress(UUID studentId, Long lectureId) {
        Lecture lecture = courseService.getLectureById(lectureId);
        if (lecture == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecture not found with id: " + lectureId);
        }

        Long courseId = getCourseIdFromLecture(lecture);
        validateEnrollment(studentId, courseId);

        LectureProgress progress = lectureProgressRepository
                .findByStudentIdAndLectureId(studentId, lectureId)
                .orElse(null);

        if (progress == null) {
            // Return NOT_STARTED progress
            progress = LectureProgress.builder()
                    .studentId(studentId)
                    .courseId(courseId)
                    .lectureId(lectureId)
                    .status(LectureProgress.LectureStatus.NOT_STARTED)
                    .progressPercentage(0)
                    .build();
        }

        return mapToLectureProgressResponse(progress, lecture);
    }

    /**
     * Get course progress
     */
    @Transactional(readOnly = true)
    public CourseProgressResponse getCourseProgress(UUID studentId, Long courseId) {
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found with id: " + courseId);
        }

        validateEnrollment(studentId, courseId);

        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new EnrollmentException("Enrollment not found"));

        List<LectureProgress> progressList = lectureProgressRepository
                .findByStudentIdAndCourseId(studentId, courseId);

        int totalLectures = countTotalLectures(course);
        int completedLectures = (int) lectureProgressRepository.countByStudentIdAndCourseIdAndStatus(
                studentId, courseId, LectureProgress.LectureStatus.COMPLETED);

        int progressPercentage = totalLectures > 0 ? (completedLectures * 100) / totalLectures : 0;

        Long lastLectureId = enrollment.getLastLectureId();
        String lastLectureTitle = null;
        if (lastLectureId != null) {
            Lecture lastLecture = courseService.getLectureById(lastLectureId);
            if (lastLecture != null) {
                lastLectureTitle = lastLecture.getTitle();
            }
        }

        LocalDateTime lastAccessedAt = progressList.stream()
                .map(LectureProgress::getLastAccessedAt)
                .filter(t -> t != null)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return CourseProgressResponse.builder()
                .courseId(courseId)
                .title(course.getTitle())
                .totalLectures(totalLectures)
                .completedLectures(completedLectures)
                .progressPercentage(progressPercentage)
                .status(enrollment.getStatus())
                .lastLectureId(lastLectureId)
                .lastLectureTitle(lastLectureTitle)
                .startedAt(enrollment.getEnrolledAt())
                .completedAt(enrollment.getCompletedAt())
                .lastAccessedAt(lastAccessedAt)
                .build();
    }

    /**
     * Resume learning - get where to continue
     */
    @Transactional(readOnly = true)
    public ResumeLearningResponse resumeLearning(UUID studentId, Long courseId) {
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found with id: " + courseId);
        }

        validateEnrollment(studentId, courseId);

        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new EnrollmentException("Enrollment not found"));

        List<LectureProgress> progressList = lectureProgressRepository
                .findByStudentIdAndCourseIdOrderByLastAccessedAtDesc(studentId, courseId);

        Long lastLectureId = null;
        String lastLectureTitle = null;
        Long nextLectureId = null;
        String nextLectureTitle = null;

        if (!progressList.isEmpty()) {
            LectureProgress lastProgress = progressList.get(0);
            lastLectureId = lastProgress.getLectureId();
            Lecture lastLecture = courseService.getLectureById(lastLectureId);
            if (lastLecture != null) {
                lastLectureTitle = lastLecture.getTitle();
            }

            // Find next incomplete lecture
            nextLectureId = findNextIncompleteLecture(studentId, courseId, lastLectureId);
            if (nextLectureId != null) {
                Lecture nextLecture = courseService.getLectureById(nextLectureId);
                if (nextLecture != null) {
                    nextLectureTitle = nextLecture.getTitle();
                }
            }
        } else {
            // No progress yet, find first lecture
            nextLectureId = findFirstLecture(courseId);
            if (nextLectureId != null) {
                Lecture nextLecture = courseService.getLectureById(nextLectureId);
                if (nextLecture != null) {
                    nextLectureTitle = nextLecture.getTitle();
                }
            }
        }

        return ResumeLearningResponse.builder()
                .courseId(courseId)
                .title(course.getTitle())
                .lastLectureId(lastLectureId)
                .lastLectureTitle(lastLectureTitle)
                .nextLectureId(nextLectureId)
                .nextLectureTitle(nextLectureTitle)
                .status(enrollment.getStatus())
                .build();
    }

    // Helper methods

    private Long getCourseIdFromLecture(Lecture lecture) {
        if (lecture.getSection() != null && lecture.getSection().getCourse() != null) {
            return lecture.getSection().getCourse().getId();
        }
        throw new EnrollmentException("Lecture is not associated with any course");
    }

    private void validateEnrollment(UUID studentId, Long courseId) {
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new EnrollmentException("Student is not enrolled in this course");
        }
    }

    private void validateProgressPercentage(Integer percentage) {
        if (percentage == null || percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Progress percentage must be between 0 and 100");
        }
    }

    private void updateEnrollmentStatus(UUID studentId, Long courseId, String status) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElse(null);
        if (enrollment != null) {
            enrollment.setStatus(status);
            enrollmentRepository.save(enrollment);
        }
    }

    private void updateCourseProgress(UUID studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElse(null);
        if (enrollment == null) {
            return;
        }

        int totalLectures = countTotalLecturesByCourseId(courseId);
        int completedLectures = (int) lectureProgressRepository.countByStudentIdAndCourseIdAndStatus(
                studentId, courseId, LectureProgress.LectureStatus.COMPLETED);

        double progressPercentage = totalLectures > 0 ? (completedLectures * 100.0) / totalLectures : 0.0;
        enrollment.setProgress(progressPercentage);

        // Update last lecture
        List<LectureProgress> progressList = lectureProgressRepository
                .findByStudentIdAndCourseIdOrderByLastAccessedAtDesc(studentId, courseId);
        if (!progressList.isEmpty()) {
            enrollment.setLastLectureId(progressList.get(0).getLectureId());
        }

        // Check if course is completed
        if (completedLectures == totalLectures && totalLectures > 0) {
            enrollment.setStatus("COMPLETED");
            if (enrollment.getCompletedAt() == null) {
                enrollment.setCompletedAt(LocalDateTime.now());
            }
        }

        enrollmentRepository.save(enrollment);
    }

    private int countTotalLectures(Course course) {
        return course.getSections().stream()
                .mapToInt(section -> section.getLectures() != null ? section.getLectures().size() : 0)
                .sum();
    }

    private int countTotalLecturesByCourseId(Long courseId) {
        Course course = courseService.getCourseById(courseId);
        return countTotalLectures(course);
    }

    private Long findNextIncompleteLecture(UUID studentId, Long courseId, Long currentLectureId) {
        Course course = courseService.getCourseById(courseId);
        return course.getSections().stream()
                .flatMap(section -> section.getLectures().stream())
                .filter(lecture -> lecture.getId() > currentLectureId)
                .filter(lecture -> {
                    LectureProgress progress = lectureProgressRepository
                            .findByStudentIdAndLectureId(studentId, lecture.getId())
                            .orElse(null);
                    return progress == null || progress.getStatus() != LectureProgress.LectureStatus.COMPLETED;
                })
                .map(Lecture::getId)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private Long findFirstLecture(Long courseId) {
        Course course = courseService.getCourseById(courseId);
        return course.getSections().stream()
                .flatMap(section -> section.getLectures().stream())
                .map(Lecture::getId)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private LectureProgressResponse mapToLectureProgressResponse(LectureProgress progress, Lecture lecture) {
        return LectureProgressResponse.builder()
                .lectureId(progress.getLectureId())
                .title(lecture.getTitle())
                .progressPercentage(progress.getProgressPercentage())
                .status(progress.getStatus().name())
                .startedAt(progress.getStartedAt())
                .completedAt(progress.getCompletedAt())
                .lastAccessedAt(progress.getLastAccessedAt())
                .timeSpentSeconds(progress.getTimeSpentSeconds())
                .build();
    }
}
