package com.lms.review.client;

import com.lms.review.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "course-service",
        url = "${course.service.url:http://localhost:8083}",
        configuration = FeignConfig.class
)
public interface CourseClient {

    @GetMapping("/api/v1/courses/{courseId}")
    CourseCheckResponse getCourseById(@PathVariable("courseId") Long courseId);
}
