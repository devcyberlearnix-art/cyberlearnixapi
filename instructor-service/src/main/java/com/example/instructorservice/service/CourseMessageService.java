package com.example.instructorservice.service;

import com.example.instructorservice.dto.CourseMessageRequest;
import com.example.instructorservice.dto.CourseMessageResponse;
import com.example.instructorservice.entity.Course;
import com.example.instructorservice.entity.CourseMessage;
import com.example.instructorservice.repository.CourseMessageRepository;
import com.example.instructorservice.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseMessageService {

    private final CourseRepository courseRepository;
    private final CourseMessageRepository messageRepository;

    public CourseMessageResponse sendMessage(
            UUID instructorId,
            UUID courseId,
            CourseMessageRequest request
    ) {

        Course course = courseRepository.findById(Long.valueOf(courseId.toString()))
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new RuntimeException("Unauthorized: not your course");
        }

        CourseMessage message = CourseMessage.builder()
                .course(course)
                .subject(request.getSubject())
                .message(request.getMessage())
                .build();

        message = messageRepository.save(message);

        // dynamic response
        return CourseMessageResponse.builder()
                .messageId(message.getId())
                .courseId(Long.valueOf(courseId.toString()))
                .instructorId(instructorId)
                .subject(message.getSubject())
                .message(message.getMessage())
                .sentAt(message.getSentAt())
                .status("SENT")
                .deliveryInfo("Delivered to all enrolled students")
                .build();
    }
}
