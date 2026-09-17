package com.lms.courseservice.service;

import com.cyberlearnix.audit.AuditLogger;
import com.lms.courseservice.dto.CourseRequirementsDTO;
import com.lms.courseservice.dto.CourseRequirementsResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.CourseRequirements;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.CourseRequirementsRepository;
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
public class CourseRequirementsService {

    private final CourseRequirementsRepository courseRequirementsRepository;
    private final CourseRepository courseRepository;
    private final CacheInvalidationService cacheInvalidationService;
    private final AuditLogger auditLogger;

    @Transactional
    @CacheEvict(value = "courseDetails", allEntries = true)
    public CourseRequirementsResponse createRequirement(Long courseId, CourseRequirementsDTO dto, UUID userId, String ipAddress) {
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

            // Verify course ownership
            if (!isCourseOwner(course, userId)) {
                auditLogger.logFailure("COURSE_REQUIREMENT_CREATE", userId != null ? userId.toString() : "anonymous",
                        "course-service", "POST", "/api/v1/courses/" + courseId + "/requirements",
                        "Unauthorized: User does not own this course", ipAddress);
                throw new RuntimeException("Unauthorized: You can only add requirements to your own courses");
            }

            CourseRequirements requirement = CourseRequirements.builder()
                    .course(course)
                    .requirementType(dto.getRequirementType())
                    .requirementText(dto.getRequirementText())
                    .build();

            CourseRequirements saved = courseRequirementsRepository.save(requirement);

            auditLogger.logSuccess("COURSE_REQUIREMENT_CREATE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "POST", "/api/v1/courses/" + courseId + "/requirements",
                    "Course requirement created successfully with ID: " + saved.getId(), ipAddress);

            CourseRequirementsResponse.CourseRequirementsData data = CourseRequirementsResponse.CourseRequirementsData.builder()
                    .id(saved.getId())
                    .courseId(course.getId())
                    .courseTitle(course.getTitle())
                    .requirementType(saved.getRequirementType())
                    .requirementText(saved.getRequirementText())
                    .build();

            return CourseRequirementsResponse.builder()
                    .success(true)
                    .message("Course requirement created successfully")
                    .data(data)
                    .build();

        } catch (RuntimeException e) {
            auditLogger.logFailure("COURSE_REQUIREMENT_CREATE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "POST", "/api/v1/courses/" + courseId + "/requirements",
                    "Failed to create course requirement: " + e.getMessage(), ipAddress);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<CourseRequirementsDTO> getRequirementsByCourseId(Long courseId) {
        List<CourseRequirements> requirements = courseRequirementsRepository.findByCourseId(courseId);
        return requirements.stream()
                .map(req -> CourseRequirementsDTO.builder()
                        .id(req.getId())
                        .courseId(req.getCourse().getId())
                        .requirementType(req.getRequirementType())
                        .requirementText(req.getRequirementText())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "courseDetails", allEntries = true)
    public CourseRequirementsResponse updateRequirement(Long requirementId, CourseRequirementsDTO dto, UUID userId, String ipAddress) {
        try {
            CourseRequirements requirement = courseRequirementsRepository.findById(requirementId)
                    .orElseThrow(() -> new RuntimeException("Course requirement not found with ID: " + requirementId));

            // Verify course ownership
            if (!isCourseOwner(requirement.getCourse(), userId)) {
                auditLogger.logFailure("COURSE_REQUIREMENT_UPDATE", userId != null ? userId.toString() : "anonymous",
                        "course-service", "PUT", "/api/v1/courses/requirements/" + requirementId,
                        "Unauthorized: User does not own this course", ipAddress);
                throw new RuntimeException("Unauthorized: You can only update requirements for your own courses");
            }

            if (dto.getRequirementType() != null) {
                requirement.setRequirementType(dto.getRequirementType());
            }
            if (dto.getRequirementText() != null) {
                requirement.setRequirementText(dto.getRequirementText());
            }

            CourseRequirements updated = courseRequirementsRepository.save(requirement);

            auditLogger.logSuccess("COURSE_REQUIREMENT_UPDATE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "PUT", "/api/v1/courses/requirements/" + requirementId,
                    "Course requirement updated successfully with ID: " + updated.getId(), ipAddress);

            CourseRequirementsResponse.CourseRequirementsData data = CourseRequirementsResponse.CourseRequirementsData.builder()
                    .id(updated.getId())
                    .courseId(updated.getCourse().getId())
                    .courseTitle(updated.getCourse().getTitle())
                    .requirementType(updated.getRequirementType())
                    .requirementText(updated.getRequirementText())
                    .build();

            return CourseRequirementsResponse.builder()
                    .success(true)
                    .message("Course requirement updated successfully")
                    .data(data)
                    .build();

        } catch (RuntimeException e) {
            auditLogger.logFailure("COURSE_REQUIREMENT_UPDATE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "PUT", "/api/v1/courses/requirements/" + requirementId,
                    "Failed to update course requirement: " + e.getMessage(), ipAddress);
            throw e;
        }
    }

    @Transactional
    @CacheEvict(value = "courseDetails", allEntries = true)
    public CourseRequirementsResponse deleteRequirement(Long requirementId, UUID userId, String ipAddress) {
        try {
            CourseRequirements requirement = courseRequirementsRepository.findById(requirementId)
                    .orElseThrow(() -> new RuntimeException("Course requirement not found with ID: " + requirementId));

            // Verify course ownership
            if (!isCourseOwner(requirement.getCourse(), userId)) {
                auditLogger.logFailure("COURSE_REQUIREMENT_DELETE", userId != null ? userId.toString() : "anonymous",
                        "course-service", "DELETE", "/api/v1/courses/requirements/" + requirementId,
                        "Unauthorized: User does not own this course", ipAddress);
                throw new RuntimeException("Unauthorized: You can only delete requirements from your own courses");
            }

            Long courseId = requirement.getCourse().getId();
            String courseTitle = requirement.getCourse().getTitle();
            String requirementType = requirement.getRequirementType();

            courseRequirementsRepository.delete(requirement);

            auditLogger.logSuccess("COURSE_REQUIREMENT_DELETE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "DELETE", "/api/v1/courses/requirements/" + requirementId,
                    "Course requirement deleted successfully with ID: " + requirementId, ipAddress);

            CourseRequirementsResponse.CourseRequirementsData data = CourseRequirementsResponse.CourseRequirementsData.builder()
                    .id(requirementId)
                    .courseId(courseId)
                    .courseTitle(courseTitle)
                    .requirementType(requirementType)
                    .requirementText(null)
                    .build();

            return CourseRequirementsResponse.builder()
                    .success(true)
                    .message("Course requirement deleted successfully")
                    .data(data)
                    .build();

        } catch (RuntimeException e) {
            auditLogger.logFailure("COURSE_REQUIREMENT_DELETE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "DELETE", "/api/v1/courses/requirements/" + requirementId,
                    "Failed to delete course requirement: " + e.getMessage(), ipAddress);
            throw e;
        }
    }

    private boolean isCourseOwner(Course course, UUID userId) {
        // Admin can modify any course
        // Instructors can only modify their own courses
        if (userId == null) {
            return false;
        }
        
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
        return course.getInstructorId().equals(userId);
    }
}