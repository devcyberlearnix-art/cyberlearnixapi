package com.lms.review.client;

import com.lms.review.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "enrollment-service",
        url = "${enrollment.service.url}",
        configuration = FeignConfig.class,
        fallback = EnrollmentClientFallback.class
)
public interface EnrollmentClient {

    @GetMapping("/api/v1/enrollments/check/{courseId}")
    EnrollmentCheckResponse checkEnrollment(@PathVariable("courseId") Long courseId);
}
