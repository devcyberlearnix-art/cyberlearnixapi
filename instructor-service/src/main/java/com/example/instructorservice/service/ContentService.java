package com.example.instructorservice.service;

import com.example.instructorservice.dto.ContentResponse;
import com.example.instructorservice.entity.Content;
import com.example.instructorservice.entity.Course;
import com.example.instructorservice.entity.Instructor;
import com.example.instructorservice.repository.ContentRepository;
import com.example.instructorservice.repository.CourseRepository;
import com.example.instructorservice.repository.InstructorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;

    // ✅ CREATE CONTENT (VERY IMPORTANT)
    @Transactional
    public ContentResponse createContent(UUID instructorId, UUID courseId, String title, String type) {

        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        Course course = courseRepository.findById(Long.valueOf(courseId.toString()))
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Content content = Content.builder()
                .title(title)
                .type(type)
                .course(course)
                .instructor(instructor) // 🔥 CRITICAL FIX
                .status(Course.CourseStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        contentRepository.save(content);

        return buildResponse(content, "Content created successfully");
    }

    // ✅ PUBLISH CONTENT (YOUR API)
    @Transactional
    public ContentResponse publishContent(UUID instructorId, UUID contentId, boolean publish) {

        Content content = contentRepository
                .findByIdAndInstructorId(contentId, instructorId)
                .orElseThrow(() -> new RuntimeException("Content not found for this instructor"));

        content.setStatus(publish ? Course.CourseStatus.PUBLISHED : Course.CourseStatus.DRAFT);
        content.setUpdatedAt(LocalDateTime.now());

        contentRepository.save(content);

        return buildResponse(
                content,
                publish ? "Content published successfully" : "Content unpublished successfully"
        );
    }

    // ✅ COMMON RESPONSE BUILDER
    private ContentResponse buildResponse(Content content, String message) {
        return ContentResponse.builder()
                .contentId(content.getId())
                .contentTitle(content.getTitle())
                .contentType(content.getType())
                .courseId(content.getCourse().getId())
                .instructorId(content.getInstructor().getId().toString())
                .status(content.getStatus().name())
                .message(message)
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();
    }
}