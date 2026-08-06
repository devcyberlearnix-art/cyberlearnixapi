package com.lms.paymentservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

/**
 * Enrollment Client for Payment Service
 * Handles student enrollment calls to Course Service (Port 8083)
 * Called after successful payment to enroll student in course
 */
@Component
public class EnrollmentClient {

    private final RestTemplate restTemplate;
    
    @Value("${course-service.url:http://localhost:8083}")
    private String courseServiceUrl;

    public EnrollmentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Enroll student in course after successful payment
     * Called by Payment Service
     */
    public void enrollStudentAfterPayment(Long courseId, UUID studentId) {
        try {
            String url = courseServiceUrl + "/api/v1/enrollments/internal/enroll";
            
            EnrollmentRequest request = new EnrollmentRequest(courseId, studentId);
            HttpHeaders headers = createHeaders();
            HttpEntity<EnrollmentRequest> entity = new HttpEntity<>(request, headers);

            restTemplate.postForObject(url, entity, Void.class);
            System.out.println("✓ Student enrolled in course. CourseId: " + courseId + ", StudentId: " + studentId);

        } catch (RestClientException e) {
            System.err.println("✗ Failed to enroll student in course: " + e.getMessage());
            // Log but don't fail - enrollment can be retried via admin interface
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // ==================== DTOs ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EnrollmentRequest {
        private Long courseId;
        private UUID userId;
    }
}
