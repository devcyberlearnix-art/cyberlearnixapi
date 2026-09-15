package com.lms.courseservice.service;

import com.lms.courseservice.dto.CourseDetailsDTO;
import com.lms.courseservice.dto.CourseRatingSummary;
import com.lms.courseservice.entity.*;
import com.lms.courseservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseDetailsService {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRequirementsRepository courseRequirementsRepository;
    private final LearningOutcomesRepository learningOutcomesRepository;
    private final CourseMaterialsRepository courseMaterialsRepository;
    private final CourseFAQRepository courseFAQRepository;
    private final ReviewRatingClient reviewRatingClient;
    private final InstructorProfileClient instructorProfileClient;

    @Cacheable(value = "courseDetails", key = "#courseId + '_' + (#userId != null ? #userId : 'anonymous')", unless = "#result == null")
    @Transactional(readOnly = true)
    public CourseDetailsDTO getCourseDetails(Long courseId, UUID userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Build course info
        CourseDetailsDTO.CourseInfoDTO courseInfo = buildCourseInfo(course);

        // Get instructor profile
        CourseDetailsDTO.InstructorProfileDTO instructorProfile = 
                instructorProfileClient.getInstructorProfile(course.getInstructorId());

        // Build curriculum with sections and lectures
        CourseDetailsDTO.CourseCurriculumDTO curriculum = buildCurriculum(course);

        // Get enrollment status
        CourseDetailsDTO.EnrollmentStatusDTO enrollmentStatus = buildEnrollmentStatus(courseId, userId);

        // Get rating summary
        CourseDetailsDTO.RatingSummaryDTO ratingSummary = buildRatingSummary(courseId);

        // Get course statistics
        CourseDetailsDTO.CourseStatsDTO courseStats = buildCourseStats(courseId);

        // Get requirements
        List<CourseDetailsDTO.CourseRequirementsDTO> requirements = buildRequirements(courseId);

        // Get learning outcomes
        List<CourseDetailsDTO.LearningOutcomesDTO> learningOutcomes = buildLearningOutcomes(courseId);

        // Get materials
        List<CourseDetailsDTO.CourseMaterialsDTO> materials = buildMaterials(courseId);

        // Get FAQs
        List<CourseDetailsDTO.CourseFAQDTO> faqs = buildFAQs(courseId);

        return CourseDetailsDTO.builder()
                .courseInfo(courseInfo)
                .instructorProfile(instructorProfile)
                .curriculum(curriculum)
                .enrollmentStatus(enrollmentStatus)
                .ratingSummary(ratingSummary)
                .courseStats(courseStats)
                .requirements(requirements)
                .learningOutcomes(learningOutcomes)
                .materials(materials)
                .faqs(faqs)
                .build();
    }

    private CourseDetailsDTO.CourseInfoDTO buildCourseInfo(Course course) {
        return CourseDetailsDTO.CourseInfoDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .subtitle(course.getSubtitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .level(course.getLevel())
                .language(course.getLanguage())
                .price(course.getPrice())
                .thumbnail(course.getThumbnail())
                .instructorId(course.getInstructorId())
                .status(course.getStatus())
                .premium(course.getPremium())
                .searchCount(course.getSearchCount())
                .viewCount(course.getViewCount())
                .build();
    }

    private CourseDetailsDTO.CourseCurriculumDTO buildCurriculum(Course course) {
        // Use optimized query to prevent N+1 problem
        List<Section> sections = sectionRepository.findByCourseIdWithLectures(course.getId());
        
        List<CourseDetailsDTO.SectionDTO> sectionDTOs = sections.stream()
                .map(section -> {
                    // Lectures are already loaded via JOIN FETCH
                    List<Lecture> lectures = section.getLectures();
                    Integer sectionDuration = lectures.stream()
                            .mapToInt(Lecture::getDuration)
                            .sum();
                    
                    List<CourseDetailsDTO.LectureDTO> lectureDTOs = lectures.stream()
                            .map(this::buildLectureDTO)
                            .collect(Collectors.toList());
                    
                    return CourseDetailsDTO.SectionDTO.builder()
                            .sectionId(section.getId())
                            .title(section.getTitle())
                            .orderIndex(section.getOrderIndex())
                            .sectionDuration(sectionDuration)
                            .lectureCount(lectures.size())
                            .lectures(lectureDTOs)
                            .build();
                })
                .sorted(Comparator.comparing(CourseDetailsDTO.SectionDTO::getOrderIndex))
                .collect(Collectors.toList());

        Integer totalDuration = sectionDTOs.stream()
                .mapToInt(CourseDetailsDTO.SectionDTO::getSectionDuration)
                .sum();
        
        Integer totalLectures = sectionDTOs.stream()
                .mapToInt(CourseDetailsDTO.SectionDTO::getLectureCount)
                .sum();

        return CourseDetailsDTO.CourseCurriculumDTO.builder()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .totalDuration(totalDuration)
                .totalSections(sectionDTOs.size())
                .totalLectures(totalLectures)
                .sections(sectionDTOs)
                .build();
    }

    private CourseDetailsDTO.LectureDTO buildLectureDTO(Lecture lecture) {
        return CourseDetailsDTO.LectureDTO.builder()
                .lectureId(lecture.getId())
                .title(lecture.getTitle())
                .description(lecture.getDescription())
                .videoUrl(lecture.getVideoUrl())
                .duration(lecture.getDuration())
                .orderIndex(lecture.getOrderIndex())
                .previewEnabled(lecture.getPreviewEnabled())
                .resources(lecture.getResources())
                .build();
    }

    private CourseDetailsDTO.EnrollmentStatusDTO buildEnrollmentStatus(Long courseId, UUID userId) {
        if (userId == null) {
            return CourseDetailsDTO.EnrollmentStatusDTO.builder()
                    .isEnrolled(false)
                    .enrollmentDate(null)
                    .progress(0.0)
                    .status("NOT_ENROLLED")
                    .build();
        }

        Optional<Enrollment> enrollment = enrollmentRepository.findByStudentIdAndCourseId(userId, courseId);
        if (enrollment.isPresent()) {
            Enrollment e = enrollment.get();
            return CourseDetailsDTO.EnrollmentStatusDTO.builder()
                    .isEnrolled(true)
                    .enrollmentDate(e.getEnrolledAt().toString())
                    .progress(e.getProgress())
                    .status(e.getStatus())
                    .build();
        }

        return CourseDetailsDTO.EnrollmentStatusDTO.builder()
                .isEnrolled(false)
                .enrollmentDate(null)
                .progress(0.0)
                .status("NOT_ENROLLED")
                .build();
    }

    private CourseDetailsDTO.RatingSummaryDTO buildRatingSummary(Long courseId) {
        CourseRatingSummary summary = reviewRatingClient.getCourseRating(courseId);
        return CourseDetailsDTO.RatingSummaryDTO.builder()
                .averageRating(summary.getAverageRating())
                .totalRatings(summary.getTotalReviews())
                .totalReviews(summary.getTotalReviews())
                .fiveStarCount(0L) // Would come from detailed review service
                .fourStarCount(0L)
                .threeStarCount(0L)
                .twoStarCount(0L)
                .oneStarCount(0L)
                .build();
    }

    private CourseDetailsDTO.CourseStatsDTO buildCourseStats(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        long totalEnrollments = enrollments.size();
        long totalStudents = enrollments.stream()
                .filter(e -> "ACTIVE".equals(e.getStatus()))
                .count();
        double averageProgress = enrollments.stream()
                .mapToDouble(Enrollment::getProgress)
                .average()
                .orElse(0.0);
        long completionCount = enrollments.stream()
                .filter(e -> e.getProgress() >= 100.0)
                .count();

        return CourseDetailsDTO.CourseStatsDTO.builder()
                .totalEnrollments(totalEnrollments)
                .totalStudents(totalStudents)
                .averageProgress(averageProgress)
                .completionCount(completionCount)
                .build();
    }

    private List<CourseDetailsDTO.CourseRequirementsDTO> buildRequirements(Long courseId) {
        return courseRequirementsRepository.findByCourseId(courseId).stream()
                .map(req -> CourseDetailsDTO.CourseRequirementsDTO.builder()
                        .id(req.getId())
                        .requirementType(req.getRequirementType())
                        .requirementText(req.getRequirementText())
                        .build())
                .collect(Collectors.toList());
    }

    private List<CourseDetailsDTO.LearningOutcomesDTO> buildLearningOutcomes(Long courseId) {
        return learningOutcomesRepository.findByCourseId(courseId).stream()
                .map(outcome -> CourseDetailsDTO.LearningOutcomesDTO.builder()
                        .id(outcome.getId())
                        .outcomeText(outcome.getOutcomeText())
                        .skillCategory(outcome.getSkillCategory())
                        .build())
                .collect(Collectors.toList());
    }

    private List<CourseDetailsDTO.CourseMaterialsDTO> buildMaterials(Long courseId) {
        return courseMaterialsRepository.findByCourseId(courseId).stream()
                .map(material -> CourseDetailsDTO.CourseMaterialsDTO.builder()
                        .id(material.getId())
                        .materialName(material.getMaterialName())
                        .materialType(material.getMaterialType())
                        .fileUrl(material.getFileUrl())
                        .fileSize(material.getFileSize())
                        .build())
                .collect(Collectors.toList());
    }

    private List<CourseDetailsDTO.CourseFAQDTO> buildFAQs(Long courseId) {
        return courseFAQRepository.findByCourseIdOrderByDisplayOrderAsc(courseId).stream()
                .map(faq -> CourseDetailsDTO.CourseFAQDTO.builder()
                        .id(faq.getId())
                        .question(faq.getQuestion())
                        .answer(faq.getAnswer())
                        .displayOrder(faq.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "courseDetails", allEntries = true)
    public void evictCourseDetailsCache() {
        log.info("Evicting all course details cache");
    }

    @CacheEvict(value = "courseDetails", allEntries = true)
    public void evictCourseDetailsForCourse(Long courseId) {
        log.info("Evicting course details cache for course: {}", courseId);
    }

    @CacheEvict(value = "courseDetails", allEntries = true)
    public void evictAllCaches() {
        log.info("Evicting all course details and curriculum caches");
    }
}