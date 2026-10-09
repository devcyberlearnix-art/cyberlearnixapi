package com.lms.courseservice.service;

import com.cyberlearnix.audit.AuditLogger;
import com.lms.courseservice.dto.CourseFAQDTO;
import com.lms.courseservice.dto.CourseFAQResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.CourseFAQ;
import com.lms.courseservice.repository.CourseFAQRepository;
import com.lms.courseservice.repository.CourseRepository;
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
public class CourseFAQService {

    private final CourseFAQRepository courseFAQRepository;
    private final CourseRepository courseRepository;
    private final CacheInvalidationService cacheInvalidationService;
    private final AuditLogger auditLogger;

    @Transactional
    @CacheEvict(value = "courseDetails", allEntries = true)
    public CourseFAQResponse createFAQ(Long courseId, CourseFAQDTO dto, UUID userId, String ipAddress) {
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

            // Verify course ownership
            if (!isCourseOwner(course, userId)) {
                auditLogger.logFailure("COURSE_FAQ_CREATE", userId != null ? userId.toString() : "anonymous",
                        "course-service", "POST", "/api/v1/courses/" + courseId + "/faqs",
                        "Unauthorized: User does not own this course", ipAddress);
                throw new RuntimeException("Unauthorized: You can only add FAQs to your own courses");
            }

            // Auto-increment display order if not provided
            Integer displayOrder = dto.getDisplayOrder();
            if (displayOrder == null) {
                List<CourseFAQ> existingFAQs = courseFAQRepository.findByCourseIdOrderByDisplayOrderAsc(courseId);
                displayOrder = existingFAQs.size() + 1;
            }

            CourseFAQ faq = CourseFAQ.builder()
                    .course(course)
                    .question(dto.getQuestion())
                    .answer(dto.getAnswer())
                    .displayOrder(displayOrder)
                    .build();

            CourseFAQ saved = courseFAQRepository.save(faq);

            auditLogger.logSuccess("COURSE_FAQ_CREATE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "POST", "/api/v1/courses/" + courseId + "/faqs",
                    "Course FAQ created successfully with ID: " + saved.getId(), ipAddress);

            CourseFAQResponse.CourseFAQData data = CourseFAQResponse.CourseFAQData.builder()
                    .id(saved.getId())
                    .courseId(course.getId())
                    .courseTitle(course.getTitle())
                    .question(saved.getQuestion())
                    .answer(saved.getAnswer())
                    .displayOrder(saved.getDisplayOrder())
                    .build();

            return CourseFAQResponse.builder()
                    .success(true)
                    .message("Course FAQ created successfully")
                    .data(data)
                    .build();

        } catch (RuntimeException e) {
            auditLogger.logFailure("COURSE_FAQ_CREATE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "POST", "/api/v1/courses/" + courseId + "/faqs",
                    "Failed to create course FAQ: " + e.getMessage(), ipAddress);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<CourseFAQDTO> getFAQsByCourseId(Long courseId) {
        List<CourseFAQ> faqs = courseFAQRepository.findByCourseIdOrderByDisplayOrderAsc(courseId);
        return faqs.stream()
                .map(faq -> CourseFAQDTO.builder()
                        .id(faq.getId())
                        .courseId(faq.getCourse().getId())
                        .question(faq.getQuestion())
                        .answer(faq.getAnswer())
                        .displayOrder(faq.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "courseDetails", allEntries = true)
    public CourseFAQResponse updateFAQ(Long faqId, CourseFAQDTO dto, UUID userId, String ipAddress) {
        try {
            CourseFAQ faq = courseFAQRepository.findById(faqId)
                    .orElseThrow(() -> new RuntimeException("Course FAQ not found with ID: " + faqId));

            // Verify course ownership
            if (!isCourseOwner(faq.getCourse(), userId)) {
                auditLogger.logFailure("COURSE_FAQ_UPDATE", userId != null ? userId.toString() : "anonymous",
                        "course-service", "PUT", "/api/v1/courses/faqs/" + faqId,
                        "Unauthorized: User does not own this course", ipAddress);
                throw new RuntimeException("Unauthorized: You can only update FAQs for your own courses");
            }

            if (dto.getQuestion() != null) {
                faq.setQuestion(dto.getQuestion());
            }
            if (dto.getAnswer() != null) {
                faq.setAnswer(dto.getAnswer());
            }
            if (dto.getDisplayOrder() != null) {
                faq.setDisplayOrder(dto.getDisplayOrder());
            }

            CourseFAQ updated = courseFAQRepository.save(faq);

            auditLogger.logSuccess("COURSE_FAQ_UPDATE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "PUT", "/api/v1/courses/faqs/" + faqId,
                    "Course FAQ updated successfully with ID: " + updated.getId(), ipAddress);

            CourseFAQResponse.CourseFAQData data = CourseFAQResponse.CourseFAQData.builder()
                    .id(updated.getId())
                    .courseId(updated.getCourse().getId())
                    .courseTitle(updated.getCourse().getTitle())
                    .question(updated.getQuestion())
                    .answer(updated.getAnswer())
                    .displayOrder(updated.getDisplayOrder())
                    .build();

            return CourseFAQResponse.builder()
                    .success(true)
                    .message("Course FAQ updated successfully")
                    .data(data)
                    .build();

        } catch (RuntimeException e) {
            auditLogger.logFailure("COURSE_FAQ_UPDATE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "PUT", "/api/v1/courses/faqs/" + faqId,
                    "Failed to update course FAQ: " + e.getMessage(), ipAddress);
            throw e;
        }
    }

    @Transactional
    @CacheEvict(value = "courseDetails", allEntries = true)
    public CourseFAQResponse deleteFAQ(Long faqId, UUID userId, String ipAddress) {
        try {
            CourseFAQ faq = courseFAQRepository.findById(faqId)
                    .orElseThrow(() -> new RuntimeException("Course FAQ not found with ID: " + faqId));

            // Verify course ownership
            if (!isCourseOwner(faq.getCourse(), userId)) {
                auditLogger.logFailure("COURSE_FAQ_DELETE", userId != null ? userId.toString() : "anonymous",
                        "course-service", "DELETE", "/api/v1/courses/faqs/" + faqId,
                        "Unauthorized: User does not own this course", ipAddress);
                throw new RuntimeException("Unauthorized: You can only delete FAQs from your own courses");
            }

            Long courseId = faq.getCourse().getId();
            String courseTitle = faq.getCourse().getTitle();
            String question = faq.getQuestion();

            courseFAQRepository.delete(faq);

            auditLogger.logSuccess("COURSE_FAQ_DELETE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "DELETE", "/api/v1/courses/faqs/" + faqId,
                    "Course FAQ deleted successfully with ID: " + faqId, ipAddress);

            CourseFAQResponse.CourseFAQData data = CourseFAQResponse.CourseFAQData.builder()
                    .id(faqId)
                    .courseId(courseId)
                    .courseTitle(courseTitle)
                    .question(question)
                    .answer(null)
                    .displayOrder(null)
                    .build();

            return CourseFAQResponse.builder()
                    .success(true)
                    .message("Course FAQ deleted successfully")
                    .data(data)
                    .build();

        } catch (RuntimeException e) {
            auditLogger.logFailure("COURSE_FAQ_DELETE", userId != null ? userId.toString() : "anonymous",
                    "course-service", "DELETE", "/api/v1/courses/faqs/" + faqId,
                    "Failed to delete course FAQ: " + e.getMessage(), ipAddress);
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
