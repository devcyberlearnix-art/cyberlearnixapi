package com.example.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "course-service",
        url = "${course.service.url:http://localhost:8083}",
        fallback = CourseClientFallback.class
)
public interface CourseClient {

    @GetMapping("/api/v1/enrollments/course/{courseId}/students")
    List<UUID> getEnrolledStudentsByCourseId(@PathVariable("courseId") Long courseId);
}
