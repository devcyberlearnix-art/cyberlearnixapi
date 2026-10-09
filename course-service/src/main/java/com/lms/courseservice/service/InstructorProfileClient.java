package com.lms.courseservice.service;

import com.cyberlearnix.security.ServiceAuthUtil;
import com.lms.courseservice.dto.CourseDetailsDTO;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

@Component
@Slf4j
public class InstructorProfileClient {

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final ServiceAuthUtil serviceAuthUtil;

    public InstructorProfileClient(
            @Value("${user-service.url:http://localhost:8091}") String userServiceUrl,
            @Qualifier("instructorProfileCircuitBreaker") CircuitBreaker circuitBreaker,
            @Qualifier("instructorProfileRetry") Retry retry,
            ServiceAuthUtil serviceAuthUtil) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(userServiceUrl)
                .requestFactory(factory)
                .defaultHeader(serviceAuthUtil.getAuthHeaderName(), serviceAuthUtil.generateServiceToken())
                .build();

        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
        this.serviceAuthUtil = serviceAuthUtil;
    }

    public CourseDetailsDTO.InstructorProfileDTO getInstructorProfile(UUID instructorId) {
        Supplier<CourseDetailsDTO.InstructorProfileDTO> supplier = CircuitBreaker.decorateSupplier(
                circuitBreaker,
                Retry.decorateSupplier(
                        retry,
                        () -> fetchInstructorProfile(instructorId)
                )
        );

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Failed to fetch instructor profile for instructorId: {} after circuit breaker and retry", instructorId, e);
            return defaultProfile(instructorId);
        }
    }

    private CourseDetailsDTO.InstructorProfileDTO fetchInstructorProfile(UUID instructorId) {
        try {
            CourseDetailsDTO.InstructorProfileDTO profile = restClient.get()
                    .uri("/api/v1/instructors/{instructorId}/profile", instructorId)
                    .retrieve()
                    .body(CourseDetailsDTO.InstructorProfileDTO.class);
            return profile != null ? profile : defaultProfile(instructorId);
        } catch (RestClientException e) {
            log.error("Failed to fetch instructor profile for instructorId: {}", instructorId, e);
            throw e; // Let circuit breaker handle the exception
        }
    }

    private CourseDetailsDTO.InstructorProfileDTO defaultProfile(UUID instructorId) {
        return CourseDetailsDTO.InstructorProfileDTO.builder()
                .instructorId(instructorId)
                .name("Instructor")
                .email("contact@cyberlearnix.com")
                .headline("Expert Instructor")
                .bio("Professional instructor with expertise in their field.")
                .rating(0.0)
                .totalCourses(0)
                .totalStudents(0)
                .verified(false)
                .build();
    }
}
