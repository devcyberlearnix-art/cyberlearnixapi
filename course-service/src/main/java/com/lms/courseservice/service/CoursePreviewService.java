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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoursePreviewService {

    private final CoursePreviewRepository previewRepository;
    private final CourseRepository courseRepository;

    public CoursePreview createPreview(Long courseId, CoursePreview preview){

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

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

    public PreviewInfo updatePreview(Long courseId, UpdatePreviewRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

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
}