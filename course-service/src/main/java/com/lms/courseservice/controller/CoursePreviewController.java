package com.lms.courseservice.controller;

import com.lms.courseservice.dto.CreatePreviewResponse;
import com.lms.courseservice.dto.PreviewInfo;
import com.lms.courseservice.entity.CoursePreview;
import com.lms.courseservice.service.CoursePreviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CoursePreviewController {

    private final CoursePreviewService previewService;

    /**
     * Create Course Preview (Instructor/Admin only)
     */
    @PostMapping("/{courseId}/preview")
    public CreatePreviewResponse createPreview(@PathVariable Long courseId,
            @RequestBody CoursePreview preview) {
        CoursePreview saved = previewService.createPreview(courseId, preview);

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
     */
    @GetMapping("/{courseId}/preview")
    public List<CoursePreview> getPreview(@PathVariable Long courseId) {
        return previewService.getCoursePreview(courseId);
    }
}