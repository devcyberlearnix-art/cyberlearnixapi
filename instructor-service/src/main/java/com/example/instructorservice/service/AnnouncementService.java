package com.example.instructorservice.service;
import com.example.instructorservice.dto.AnnouncementRequest;
import com.example.instructorservice.dto.AnnouncementResponse;
import com.example.instructorservice.entity.Announcement;
import com.example.instructorservice.entity.Course;
import com.example.instructorservice.repository.AnnouncementRepository;
import com.example.instructorservice.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final CourseRepository courseRepository;
    private final AnnouncementRepository announcementRepository;

    public AnnouncementResponse createAnnouncement(
            UUID instructorId,
            UUID courseId,
            AnnouncementRequest request
    ) {

        Course course = courseRepository.findById(Long.valueOf(courseId.toString()))
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new RuntimeException("Unauthorized: course does not belong to instructor");
        }

        Announcement announcement = Announcement.builder()
                .course(course)
                .title(request.getTitle())
                .message(request.getMessage())
                .build();

        announcement = announcementRepository.save(announcement);

        return AnnouncementResponse.builder()
                .announcementId(announcement.getId())
                .courseId(course.getId())
                .instructorId(instructorId)
                .title(announcement.getTitle())
                .message(announcement.getMessage())
                .createdAt(announcement.getCreatedAt())
                .status("PUBLISHED")
                .build();
    }
}