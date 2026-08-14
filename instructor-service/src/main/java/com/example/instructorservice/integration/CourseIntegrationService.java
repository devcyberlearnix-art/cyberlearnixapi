package com.example.instructorservice.integration;

import com.example.instructorservice.dto.CourseRequestDTO;
import com.example.instructorservice.entity.Course;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
public class CourseIntegrationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${course.service.url:http://localhost:8083}")
    private String courseServiceUrl;

    public Long syncCourseCreation(Course course, CourseRequestDTO request, String authorization) {
        try {
            Map<String, Object> payload = buildCoursePayload(course, request);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    courseServiceUrl + "/api/v1/courses", entity, Map.class);
            Object id = response.getBody() != null ? response.getBody().get("id") : null;
            if (!(id instanceof Number number)) {
                throw new IllegalStateException("Course service response did not contain a numeric id");
            }
            log.info("Synced course creation to course-service for courseId={}", course.getId());
            return number.longValue();
        } catch (Exception ex) {
            log.warn("Unable to sync course creation to course-service for courseId={}", course.getId(), ex);
            throw ex;
        }
    }

    public void syncCoursePublish(Course course) {
        log.info("Course publish sync requested for courseId={}", course.getId());
    }

    public void syncCourseDeletion(Course course) {
        log.info("Course deletion sync requested for courseId={}", course.getId());
    }

    public void syncCourseUpdate(Course course, CourseRequestDTO request) {
        log.info("Course update sync requested for courseId={}", course.getId());
    }

    private Map<String, Object> buildCoursePayload(Course course, CourseRequestDTO request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", request.getTitle());
        payload.put("subtitle", request.getSubtitle());
        payload.put("description", request.getDescription());
        payload.put("category", request.getCategory());
        payload.put("level", null);
        payload.put("language", null);
        payload.put("price", request.getPrice());
        payload.put("thumbnail", request.getThumbnailUrl());
        payload.put("instructorId", course.getInstructor() != null ? course.getInstructor().getUserId() : null);
        payload.put("status", course.getStatus() != null ? course.getStatus().name() : "DRAFT");
        return payload;
    }
}
