package com.example.notification.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class CourseClientFallback implements CourseClient {

    @Override
    public List<UUID> getEnrolledStudentsByCourseId(Long courseId) {
        log.warn("Course service unavailable for courseId={}. Returning empty list.", courseId);
        return Collections.emptyList();
    }
}
