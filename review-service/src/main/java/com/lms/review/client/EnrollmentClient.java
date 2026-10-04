package com.lms.review.client;

import com.lms.review.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "enrollment-service-client",
        url = "${course.service.url:http://localhost:8083}",
        configuration = FeignConfig.class,
        fallback = EnrollmentClientFallback.class
)
public interface EnrollmentClient {

    @GetMapping("/api/v1/enrollments/users/{userId}")
    List<EnrollmentInfo> getEnrollmentsByUserId(@PathVariable("userId") UUID userId);
}
