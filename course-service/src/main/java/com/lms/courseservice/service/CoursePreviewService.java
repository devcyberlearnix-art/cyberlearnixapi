package com.lms.courseservice.service;

import com.lms.courseservice.dto.PreviewInfo;
import com.lms.courseservice.dto.UpdatePreviewRequest;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.CoursePreview;
import com.lms.courseservice.repository.CoursePreviewRepository;
import com.lms.courseservice.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoursePreviewService {

    private final CoursePreviewRepository previewRepository;
    private final CourseRepository courseRepository;

    public CoursePreview createPreview(Long courseId, CoursePreview preview, UUID userId){
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Verify course ownership
        if (!isCourseOwner(course, userId)) {
            throw new RuntimeException("Unauthorized: You can only add previews to your own courses");
        }

        preview.setCourse(course);

        return previewRepository.save(preview);
    }

    public List<PreviewInfo> getCoursePreview(Long courseId){
        // Validate course exists first
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        return previewRepository.findByCourseId(courseId)
                .stream()
                .map(p -> new PreviewInfo(
                        p.getId(),
                        p.getTitle(),
                        p.getVideoUrl(),
                        p.getDuration(),
                        course.getId(),
                        course.getTitle()
                ))
                .collect(Collectors.toList());
    }

    public PreviewInfo updatePreview(Long courseId, UpdatePreviewRequest request, UUID userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        // Verify course ownership
        if (!isCourseOwner(course, userId)) {
            throw new RuntimeException("Unauthorized: You can only update previews for your own courses");
        }

        List<CoursePreview> previews = previewRepository.findByCourseId(courseId);
        CoursePreview preview;
        if (previews.isEmpty()) {
            preview = new CoursePreview();
            preview.setCourse(course);
        } else {
            preview = previews.get(0);
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            preview.setTitle(request.getTitle());
        }
        if (request.getVideoUrl() != null && !request.getVideoUrl().isBlank()) {
            preview.setVideoUrl(request.getVideoUrl());
        }
        if (request.getDuration() != null) {
            preview.setDuration(request.getDuration());
        }

        CoursePreview saved = previewRepository.save(preview);

        return new PreviewInfo(
                saved.getId(),
                saved.getTitle(),
                saved.getVideoUrl(),
                saved.getDuration(),
                course.getId(),
                course.getTitle()
        );
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
