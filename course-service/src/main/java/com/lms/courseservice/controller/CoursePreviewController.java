package com.lms.courseservice.controller;

import com.lms.courseservice.dto.CreatePreviewResponse;
import com.lms.courseservice.dto.GetPreviewResponse;
import com.lms.courseservice.dto.PreviewInfo;
import com.lms.courseservice.dto.UpdatePreviewRequest;
import com.lms.courseservice.entity.CoursePreview;
import com.lms.courseservice.service.CoursePreviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CoursePreviewController {

    private final CoursePreviewService previewService;

    /**
     * Create Course Preview (Instructor/Admin only)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PostMapping("/{courseId}/preview")
    public CreatePreviewResponse createPreview(@PathVariable Long courseId,
            @RequestBody CoursePreview preview) {
        UUID userId = extractUserIdFromContext();
        CoursePreview saved = previewService.createPreview(courseId, preview, userId);

        PreviewInfo info = new PreviewInfo(
                saved.getId(),
                saved.getTitle(),
                saved.getVideoUrl(),
                saved.getDuration(),
                saved.getCourse().getId(),
                saved.getCourse().getTitle()
        );

        return new CreatePreviewResponse(true,
                "Course preview created successfully",
                info);
    }

    /**
     * Get Course Previews (Public)
     * Returns a detailed structured response with all preview info for the given course.
     */
    @GetMapping("/{courseId}/preview")
    public ResponseEntity<GetPreviewResponse> getPreview(@PathVariable Long courseId) {
        List<PreviewInfo> previews = previewService.getCoursePreview(courseId);

        String message = previews.isEmpty()
                ? "No previews found for course id: " + courseId
                : "Found " + previews.size() + " preview(s) for course id: " + courseId;

        return ResponseEntity.ok(new GetPreviewResponse(true, message, previews));
    }

    /**
     * Update Course Preview - Partial (Instructor/Admin only)
     *
     * Accepts JSON body — all fields are optional, only provided fields are updated:
     * {
     *   "title":    "Intro Video",          // optional
     *   "videoUrl": "https://res.cloudinary.com/...",  // optional — Cloudinary URL
     *   "duration": 180                     // optional — duration in seconds
     * }
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PatchMapping("/{courseId}/preview")
    public ResponseEntity<CreatePreviewResponse> updatePreview(
            @PathVariable Long courseId,
            @RequestBody UpdatePreviewRequest request) {

        UUID userId = extractUserIdFromContext();
        PreviewInfo updated = previewService.updatePreview(courseId, request, userId);

        return ResponseEntity.ok(new CreatePreviewResponse(
                true,
                "Course preview updated successfully",
                updated
        ));
    }

    /**
     * Extract userId from Spring Security context (set by JwtFilter)
     */
    private UUID extractUserIdFromContext() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return null;
        }
        return principal instanceof UUID ? (UUID) principal : UUID.fromString(principal.toString());
    }
}

