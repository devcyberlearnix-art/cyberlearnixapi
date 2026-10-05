package com.lms.courseservice.service;

import com.cyberlearnix.audit.AuditLogger;
import com.lms.courseservice.dto.CourseMaterialsDTO;
import com.lms.courseservice.dto.CourseMaterialsResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.CourseMaterials;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.CourseMaterialsRepository;
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
public class CourseMaterialsService {

    private final CourseMaterialsRepository courseMaterialsRepository;
    private final CourseRepository courseRepository;
    private final CacheInvalidationService cacheInvalidationService;
    private final AuditLogger auditLogger;

    @Transactional
    @CacheEvict(value = "courseDetails", allEntries = true)
    public CourseMaterialsResponse createMaterial(Long courseId, CourseMaterialsDTO dto, UUID userId, String ipAddress) {
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

            // Verify course ownership
            if (!isCourseOwner(course, userId)) {
                auditLogger.logFailure("COURSE_MATERIAL_CREATE", userId != null ? userId.toString() : "anonymous",
                        "course-service", "POST", "/api/v1/courses/" + courseId + "/materials",
                        "Unauthorized: User does not own this course", ipAddress);
                throw new RuntimeException("Unauthorized: You can only add materials to your own courses");
            }

            CourseMaterials material = CourseMaterials.builder()
                    .course(course)
                    .materialName(dto.getMaterialName())
                    .materialType(dto.getMaterialType())
                    .fileUrl(dto.getFileUrl())
                    .fileSize(dto.getFileSize())
                    .build();

            CourseMaterials saved = courseMaterialsRepository.save(material);

            auditLogger.logSuccess("COURSE_MATERIAL_CREATE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "POST", "/api/v1/courses/" + courseId + "/materials",
                    "Course material created successfully with ID: " + saved.getId(), ipAddress);

            CourseMaterialsResponse.CourseMaterialsData data = CourseMaterialsResponse.CourseMaterialsData.builder()
                    .id(saved.getId())
                    .courseId(course.getId())
                    .courseTitle(course.getTitle())
                    .materialName(saved.getMaterialName())
                    .materialType(saved.getMaterialType())
                    .fileUrl(saved.getFileUrl())
                    .fileSize(saved.getFileSize())
                    .build();

            return CourseMaterialsResponse.builder()
                    .success(true)
                    .message("Course material created successfully")
                    .data(data)
                    .build();

        } catch (RuntimeException e) {
            auditLogger.logFailure("COURSE_MATERIAL_CREATE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "POST", "/api/v1/courses/" + courseId + "/materials",
                    "Failed to create course material: " + e.getMessage(), ipAddress);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<CourseMaterialsDTO> getMaterialsByCourseId(Long courseId) {
        List<CourseMaterials> materials = courseMaterialsRepository.findByCourseId(courseId);
        return materials.stream()
                .map(material -> CourseMaterialsDTO.builder()
                        .id(material.getId())
                        .courseId(material.getCourse().getId())
                        .materialName(material.getMaterialName())
                        .materialType(material.getMaterialType())
                        .fileUrl(material.getFileUrl())
                        .fileSize(material.getFileSize())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "courseDetails", allEntries = true)
    public CourseMaterialsResponse updateMaterial(Long materialId, CourseMaterialsDTO dto, UUID userId, String ipAddress) {
        try {
            CourseMaterials material = courseMaterialsRepository.findById(materialId)
                    .orElseThrow(() -> new RuntimeException("Course material not found with ID: " + materialId));

            // Verify course ownership
            if (!isCourseOwner(material.getCourse(), userId)) {
                auditLogger.logFailure("COURSE_MATERIAL_UPDATE", userId != null ? userId.toString() : "anonymous",
                        "course-service", "PUT", "/api/v1/courses/materials/" + materialId,
                        "Unauthorized: User does not own this course", ipAddress);
                throw new RuntimeException("Unauthorized: You can only update materials for your own courses");
            }

            if (dto.getMaterialName() != null) {
                material.setMaterialName(dto.getMaterialName());
            }
            if (dto.getMaterialType() != null) {
                material.setMaterialType(dto.getMaterialType());
            }
            if (dto.getFileUrl() != null) {
                material.setFileUrl(dto.getFileUrl());
            }
            if (dto.getFileSize() != null) {
                material.setFileSize(dto.getFileSize());
            }

            CourseMaterials updated = courseMaterialsRepository.save(material);

            auditLogger.logSuccess("COURSE_MATERIAL_UPDATE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "PUT", "/api/v1/courses/materials/" + materialId,
                    "Course material updated successfully with ID: " + updated.getId(), ipAddress);

            CourseMaterialsResponse.CourseMaterialsData data = CourseMaterialsResponse.CourseMaterialsData.builder()
                    .id(updated.getId())
                    .courseId(updated.getCourse().getId())
                    .courseTitle(updated.getCourse().getTitle())
                    .materialName(updated.getMaterialName())
                    .materialType(updated.getMaterialType())
                    .fileUrl(updated.getFileUrl())
                    .fileSize(updated.getFileSize())
                    .build();

            return CourseMaterialsResponse.builder()
                    .success(true)
                    .message("Course material updated successfully")
                    .data(data)
                    .build();

        } catch (RuntimeException e) {
            auditLogger.logFailure("COURSE_MATERIAL_UPDATE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "PUT", "/api/v1/courses/materials/" + materialId,
                    "Failed to update course material: " + e.getMessage(), ipAddress);
            throw e;
        }
    }

    @Transactional
    @CacheEvict(value = "courseDetails", allEntries = true)
    public CourseMaterialsResponse deleteMaterial(Long materialId, UUID userId, String ipAddress) {
        try {
            CourseMaterials material = courseMaterialsRepository.findById(materialId)
                    .orElseThrow(() -> new RuntimeException("Course material not found with ID: " + materialId));

            // Verify course ownership
            if (!isCourseOwner(material.getCourse(), userId)) {
                auditLogger.logFailure("COURSE_MATERIAL_DELETE", userId != null ? userId.toString() : "anonymous",
                        "course-service", "DELETE", "/api/v1/courses/materials/" + materialId,
                        "Unauthorized: User does not own this course", ipAddress);
                throw new RuntimeException("Unauthorized: You can only delete materials from your own courses");
            }

            Long courseId = material.getCourse().getId();
            String courseTitle = material.getCourse().getTitle();
            String materialName = material.getMaterialName();

            courseMaterialsRepository.delete(material);

            auditLogger.logSuccess("COURSE_MATERIAL_DELETE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "DELETE", "/api/v1/courses/materials/" + materialId,
                    "Course material deleted successfully with ID: " + materialId, ipAddress);

            CourseMaterialsResponse.CourseMaterialsData data = CourseMaterialsResponse.CourseMaterialsData.builder()
                    .id(materialId)
                    .courseId(courseId)
                    .courseTitle(courseTitle)
                    .materialName(materialName)
                    .materialType(null)
                    .fileUrl(null)
                    .fileSize(null)
                    .build();

            return CourseMaterialsResponse.builder()
                    .success(true)
                    .message("Course material deleted successfully")
                    .data(data)
                    .build();

        } catch (RuntimeException e) {
            auditLogger.logFailure("COURSE_MATERIAL_DELETE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "DELETE", "/api/v1/courses/materials/" + materialId,
                    "Failed to delete course material: " + e.getMessage(), ipAddress);
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
