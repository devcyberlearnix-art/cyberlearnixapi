package com.lms.courseservice.service;

import com.lms.courseservice.dto.CourseContentResponse;
import com.lms.courseservice.dto.CourseContentResponse.LectureProgressInfo;
import com.lms.courseservice.dto.LectureDetailResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Enrollment;
import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.entity.LectureProgress;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.exception.EnrollmentException;
import com.lms.courseservice.repository.EnrollmentRepository;
import com.lms.courseservice.repository.LectureProgressRepository;
import com.lms.courseservice.repository.SectionRepository;
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
public class CourseContentService {

    private final CourseService courseService;
    private final SectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LectureProgressRepository lectureProgressRepository;

    /**
     * Get course content for enrolled students
     */
    @Transactional(readOnly = true)
    public CourseContentResponse getCourseContent(UUID studentId, Long courseId) {
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            throw new EnrollmentException("Course not found with id: " + courseId);
        }

        // Check if course is published
        if (!"PUBLISHED".equalsIgnoreCase(course.getStatus())) {
            throw new EnrollmentException("Course is not available for learning");
        }

        // Validate enrollment
        validateEnrollment(studentId, courseId);

        // Get sections with lectures (optimized query)
        List<Section> sections = sectionRepository.findByCourseIdWithLectures(courseId);

        // Calculate totals
        int totalSections = sections.size();
        int totalLectures = sections.stream()
                .mapToInt(section -> section.getLectures().size())
                .sum();
        int totalDuration = sections.stream()
                .flatMap(section -> section.getLectures().stream())
                .mapToInt(Lecture::getDuration)
                .sum();

        // Get progress data
        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new EnrollmentException("Enrollment not found"));

        // Build section DTOs with progress
        List<CourseContentResponse.SectionContentDTO> sectionDTOs = sections.stream()
                .map(section -> buildSectionDTO(section, studentId))
                .sorted(Comparator.comparing(CourseContentResponse.SectionContentDTO::getOrderIndex))
                .collect(Collectors.toList());

        return CourseContentResponse.builder()
                .courseId(courseId)
                .courseTitle(course.getTitle())
                .courseDescription(course.getDescription())
                .courseThumbnail(course.getThumbnail())
                .totalSections(totalSections)
                .totalLectures(totalLectures)
                .totalDuration(totalDuration)
                .overallProgress(enrollment.getProgress())
                .sections(sectionDTOs)
                .build();
    }

    /**
     * Get lecture detail for enrolled students
     */
    @Transactional(readOnly = true)
    public LectureDetailResponse getLectureDetail(UUID studentId, Long lectureId) {
        Lecture lecture = courseService.getLectureById(lectureId);
        if (lecture == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Lecture not found with id: " + lectureId);
        }

        Section section = lecture.getSection();
        if (section == null) {
            throw new EnrollmentException("Lecture is not associated with any section");
        }

        Course course = section.getCourse();
        if (course == null) {
            throw new EnrollmentException("Section is not associated with any course");
        }

        // Check if course is published
        if (!"PUBLISHED".equalsIgnoreCase(course.getStatus())) {
            throw new EnrollmentException("Course is not available for learning");
        }

        // For preview lectures, allow access without enrollment
        if (Boolean.TRUE.equals(lecture.getPreviewEnabled())) {
            log.info("Accessing preview lecture {} without enrollment check", lectureId);
        } else {
            // Validate enrollment for non-preview lectures
            validateEnrollment(studentId, course.getId());
        }

        // Get progress
        CourseContentResponse.LectureProgressInfo progressInfo = getLectureProgressInfo(studentId, lectureId, course.getId());

        // Build navigation info
        LectureDetailResponse.NavigationInfo navigation = buildNavigationInfo(section, lecture);

        return LectureDetailResponse.builder()
                .lectureId(lecture.getId())
                .title(lecture.getTitle())
                .description(lecture.getDescription())
                .videoUrl(lecture.getVideoUrl())
                .duration(lecture.getDuration())
                .orderIndex(lecture.getOrderIndex())
                .previewEnabled(lecture.getPreviewEnabled())
                .resources(lecture.getResources())
                .section(LectureDetailResponse.SectionInfo.builder()
                        .sectionId(section.getId())
                        .title(section.getTitle())
                        .orderIndex(section.getOrderIndex())
                        .build())
                .course(LectureDetailResponse.CourseInfo.builder()
                        .courseId(course.getId())
                        .title(course.getTitle())
                        .thumbnail(course.getThumbnail())
                        .build())
                .progress(LectureDetailResponse.LectureProgressInfo.builder()
                        .status(progressInfo.getStatus())
                        .progressPercentage(progressInfo.getProgressPercentage())
                        .isCompleted(progressInfo.getIsCompleted())
                        .build())
                .navigation(navigation)
                .build();
    }

    private CourseContentResponse.SectionContentDTO buildSectionDTO(Section section, UUID studentId) {
        List<CourseContentResponse.LectureContentDTO> lectureDTOs = section.getLectures().stream()
                .map(lecture -> buildLectureDTO(lecture, studentId))
                .sorted(Comparator.comparing(CourseContentResponse.LectureContentDTO::getOrderIndex))
                .collect(Collectors.toList());

        int sectionDuration = lectureDTOs.stream()
                .mapToInt(CourseContentResponse.LectureContentDTO::getDuration)
                .sum();

        return CourseContentResponse.SectionContentDTO.builder()
                .sectionId(section.getId())
                .title(section.getTitle())
                .orderIndex(section.getOrderIndex())
                .sectionDuration(sectionDuration)
                .lectureCount(lectureDTOs.size())
                .lectures(lectureDTOs)
                .build();
    }

    private CourseContentResponse.LectureContentDTO buildLectureDTO(Lecture lecture, UUID studentId) {
        Long courseId = lecture.getSection().getCourse().getId();
        CourseContentResponse.LectureProgressInfo progressInfo = getLectureProgressInfo(studentId, lecture.getId(), courseId);

        return CourseContentResponse.LectureContentDTO.builder()
                .lectureId(lecture.getId())
                .title(lecture.getTitle())
                .description(lecture.getDescription())
                .videoUrl(lecture.getVideoUrl())
                .duration(lecture.getDuration())
                .orderIndex(lecture.getOrderIndex())
                .previewEnabled(lecture.getPreviewEnabled())
                .resources(lecture.getResources())
                .progress(progressInfo)
                .build();
    }

    private CourseContentResponse.LectureProgressInfo getLectureProgressInfo(UUID studentId, Long lectureId, Long courseId) {
        LectureProgress progress = lectureProgressRepository
                .findByStudentIdAndLectureId(studentId, lectureId)
                .orElse(null);

        if (progress == null) {
            return CourseContentResponse.LectureProgressInfo.builder()
                    .status("NOT_STARTED")
                    .progressPercentage(0)
                    .isCompleted(false)
                    .build();
        }

        return CourseContentResponse.LectureProgressInfo.builder()
                .status(progress.getStatus().name())
                .progressPercentage(progress.getProgressPercentage())
                .isCompleted(progress.getStatus() == LectureProgress.LectureStatus.COMPLETED)
                .build();
    }

    private LectureDetailResponse.NavigationInfo buildNavigationInfo(Section currentSection, Lecture currentLecture) {
        Course course = currentSection.getCourse();
        List<Section> allSections = sectionRepository.findByCourseIdWithLectures(course.getId());

        Lecture previousLecture = null;
        Lecture nextLecture = null;

        // Flatten all lectures across sections with ordering
        List<Lecture> allLectures = allSections.stream()
                .sorted(Comparator.comparing(Section::getOrderIndex))
                .flatMap(section -> section.getLectures().stream()
                        .sorted(Comparator.comparing(Lecture::getOrderIndex)))
                .collect(Collectors.toList());

        for (int i = 0; i < allLectures.size(); i++) {
            if (allLectures.get(i).getId().equals(currentLecture.getId())) {
                if (i > 0) {
                    previousLecture = allLectures.get(i - 1);
                }
                if (i < allLectures.size() - 1) {
                    nextLecture = allLectures.get(i + 1);
                }
                break;
            }
        }

        return LectureDetailResponse.NavigationInfo.builder()
                .previousLectureId(previousLecture != null ? previousLecture.getId() : null)
                .previousLectureTitle(previousLecture != null ? previousLecture.getTitle() : null)
                .nextLectureId(nextLecture != null ? nextLecture.getId() : null)
                .nextLectureTitle(nextLecture != null ? nextLecture.getTitle() : null)
                .build();
    }

    private void validateEnrollment(UUID studentId, Long courseId) {
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new EnrollmentException("Student is not enrolled in this course");
        }
    }
}
