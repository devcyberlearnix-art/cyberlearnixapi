package com.lms.cart_service.client;

import com.lms.cart_service.dto.CourseDetails;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "course-service", url = "${course-service.url:http://localhost:8083}")
public interface CourseClient {
    @GetMapping("/api/v1/courses/{courseId}")
    CourseDetails getCourseById(
            @PathVariable("courseId") Long courseId);
}