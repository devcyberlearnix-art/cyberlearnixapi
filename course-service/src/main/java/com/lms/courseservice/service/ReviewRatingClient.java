package com.lms.courseservice.service;

import com.lms.courseservice.dto.CourseRatingSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ReviewRatingClient {

    private final RestClient restClient;

    public ReviewRatingClient(@Value("${review-service.url:http://localhost:8089}") String reviewServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(reviewServiceUrl).build();
    }

    public CourseRatingSummary getCourseRating(Long courseId) {
        try {
            CourseRatingSummary summary = restClient.get()
                    .uri("/api/v1/reviews/course/{courseId}/summary", courseId)
                    .retrieve()
                    .body(CourseRatingSummary.class);
            return summary != null ? summary : emptySummary(courseId);
        } catch (RestClientException exception) {
            return emptySummary(courseId);
        }
    }

    private CourseRatingSummary emptySummary(Long courseId) {
        return new CourseRatingSummary(courseId, 0.0, 0L);
    }
}