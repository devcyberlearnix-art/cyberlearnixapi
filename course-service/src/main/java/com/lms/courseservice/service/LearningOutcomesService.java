package com.lms.courseservice.service;

import com.cyberlearnix.audit.AuditLogger;
import com.lms.courseservice.dto.LearningOutcomesDTO;
import com.lms.courseservice.dto.LearningOutcomesResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.LearningOutcomes;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.LearningOutcomesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningOutcomesService {

    private final LearningOutcomesRepository learningOutcomesRepository;
    private final CourseRepository courseRepository;
    private final CacheInvalidationService cacheInvalidationService;
    private final AuditLogger auditLogger;

    @Transactional
    @CacheEvict(value = "courseDetails", allEntries = true)
    public LearningOutcomesResponse createOutcome(Long courseId, LearningOutcomesDTO dto, UUID userId, String ipAddress) {
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

            // Verify course ownership
            if (!isCourseOwner(course, userId)) {
                auditLogger.logFailure("LEARNING_OUTCOME_CREATE", userId != null ? userId.toString() : "anonymous",
                        "course-service", "POST", "/api/v1/courses/" + courseId + "/outcomes",
                        "Unauthorized: User does not own this course", ipAddress);
                throw new RuntimeException("Unauthorized: You can only add outcomes to your own courses");
            }

            LearningOutcomes outcome = LearningOutcomes.builder()
                    .course(course)
                    .outcomeText(dto.getOutcomeText())
                    .skillCategory(dto.getSkillCategory())
                    .build();

            LearningOutcomes saved = learningOutcomesRepository.save(outcome);

            auditLogger.logSuccess("LEARNING_OUTCOME_CREATE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "POST", "/api/v1/courses/" + courseId + "/outcomes",
                    "Learning outcome created successfully with ID: " + saved.getId(), ipAddress);

            LearningOutcomesResponse.LearningOutcomesData data = LearningOutcomesResponse.LearningOutcomesData.builder()
                    .id(saved.getId())
                    .courseId(course.getId())
                    .courseTitle(course.getTitle())
                    .outcomeText(saved.getOutcomeText())
                    .skillCategory(saved.getSkillCategory())
                    .build();

            return LearningOutcomesResponse.builder()
                    .success(true)
                    .message("Learning outcome created successfully")
                    .data(data)
                    .build();

        } catch (RuntimeException e) {
            auditLogger.logFailure("LEARNING_OUTCOME_CREATE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "POST", "/api/v1/courses/" + courseId + "/outcomes",
                    "Failed to create learning outcome: " + e.getMessage(), ipAddress);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<LearningOutcomesDTO> getOutcomesByCourseId(Long courseId) {
        List<LearningOutcomes> outcomes = learningOutcomesRepository.findByCourseId(courseId);
        return outcomes.stream()
                .map(outcome -> LearningOutcomesDTO.builder()
                        .id(outcome.getId())
                        .courseId(outcome.getCourse().getId())
                        .outcomeText(outcome.getOutcomeText())
                        .skillCategory(outcome.getSkillCategory())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "courseDetails", allEntries = true)
    public LearningOutcomesResponse updateOutcome(Long outcomeId, LearningOutcomesDTO dto, UUID userId, String ipAddress) {
        try {
            LearningOutcomes outcome = learningOutcomesRepository.findById(outcomeId)
                    .orElseThrow(() -> new RuntimeException("Learning outcome not found with ID: " + outcomeId));

            // Verify course ownership
            if (!isCourseOwner(outcome.getCourse(), userId)) {
                auditLogger.logFailure("LEARNING_OUTCOME_UPDATE", userId != null ? userId.toString() : "anonymous",
                        "course-service", "PUT", "/api/v1/courses/outcomes/" + outcomeId,
                        "Unauthorized: User does not own this course", ipAddress);
                throw new RuntimeException("Unauthorized: You can only update outcomes for your own courses");
            }

            if (dto.getOutcomeText() != null) {
                outcome.setOutcomeText(dto.getOutcomeText());
            }
            if (dto.getSkillCategory() != null) {
                outcome.setSkillCategory(dto.getSkillCategory());
            }

            LearningOutcomes updated = learningOutcomesRepository.save(outcome);

            auditLogger.logSuccess("LEARNING_OUTCOME_UPDATE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "PUT", "/api/v1/courses/outcomes/" + outcomeId,
                    "Learning outcome updated successfully with ID: " + updated.getId(), ipAddress);

            LearningOutcomesResponse.LearningOutcomesData data = LearningOutcomesResponse.LearningOutcomesData.builder()
                    .id(updated.getId())
                    .courseId(updated.getCourse().getId())
                    .courseTitle(updated.getCourse().getTitle())
                    .outcomeText(updated.getOutcomeText())
                    .skillCategory(updated.getSkillCategory())
                    .build();

            return LearningOutcomesResponse.builder()
                    .success(true)
                    .message("Learning outcome updated successfully")
                    .data(data)
                    .build();

        } catch (RuntimeException e) {
            auditLogger.logFailure("LEARNING_OUTCOME_UPDATE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "PUT", "/api/v1/courses/outcomes/" + outcomeId,
                    "Failed to update learning outcome: " + e.getMessage(), ipAddress);
            throw e;
        }
    }

    @Transactional
    @CacheEvict(value = "courseDetails", allEntries = true)
    public LearningOutcomesResponse deleteOutcome(Long outcomeId, UUID userId, String ipAddress) {
        try {
            LearningOutcomes outcome = learningOutcomesRepository.findById(outcomeId)
                    .orElseThrow(() -> new RuntimeException("Learning outcome not found with ID: " + outcomeId));

            // Verify course ownership
            if (!isCourseOwner(outcome.getCourse(), userId)) {
                auditLogger.logFailure("LEARNING_OUTCOME_DELETE", userId != null ? userId.toString() : "anonymous",
                        "course-service", "DELETE", "/api/v1/courses/outcomes/" + outcomeId,
                        "Unauthorized: User does not own this course", ipAddress);
                throw new RuntimeException("Unauthorized: You can only delete outcomes from your own courses");
            }

            Long courseId = outcome.getCourse().getId();
            String courseTitle = outcome.getCourse().getTitle();
            String outcomeText = outcome.getOutcomeText();

            learningOutcomesRepository.delete(outcome);

            auditLogger.logSuccess("LEARNING_OUTCOME_DELETE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "DELETE", "/api/v1/courses/outcomes/" + outcomeId,
                    "Learning outcome deleted successfully with ID: " + outcomeId, ipAddress);

            LearningOutcomesResponse.LearningOutcomesData data = LearningOutcomesResponse.LearningOutcomesData.builder()
                    .id(outcomeId)
                    .courseId(courseId)
                    .courseTitle(courseTitle)
                    .outcomeText(outcomeText)
                    .skillCategory(null)
                    .build();

            return LearningOutcomesResponse.builder()
                    .success(true)
                    .message("Learning outcome deleted successfully")
                    .data(data)
                    .build();

        } catch (RuntimeException e) {
            auditLogger.logFailure("LEARNING_OUTCOME_DELETE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "DELETE", "/api/v1/courses/outcomes/" + outcomeId,
                    "Failed to delete learning outcome: " + e.getMessage(), ipAddress);
            throw e;
        }
    }

    private boolean isCourseOwner(Course course, UUID userId) {
        // Check if user is admin by checking security context
        org.springframework.security.core.Authentication auth =
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getAuthorities() != null) {
            for (org.springframework.security.core.GrantedAuthority authority : auth.getAuthorities()) {
                String role = authority.getAuthority();
                if (role.equals("ROLE_MAIN_ADMIN") || role.equals("ROLE_SUB_ADMIN")) {
                    return true; // Admins can modify any course
                }
            }
        }

        // Instructors can only modify their own courses
        if (userId == null) {
            return false;
        }
        return course.getInstructorId().equals(userId);
    }
}
