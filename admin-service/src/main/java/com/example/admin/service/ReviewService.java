package com.example.admin.service;

import com.example.admin.dto.ReviewDto;
import com.example.admin.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Value("${review-service.url:http://localhost:8089}")
    private String reviewServiceBaseUrl;

    private String getReviewsUrl() {
        String base = (reviewServiceBaseUrl == null || reviewServiceBaseUrl.isBlank())
                ? "http://localhost:8089"
                : reviewServiceBaseUrl;
        // strip trailing slash
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        // strip if someone accidentally put the full path in the property
        if (base.endsWith("/api/admin/reviews")) base = base.substring(0, base.length() - "/api/admin/reviews".length());
        return base + "/api/v1/admin/reviews";
    }

    private HttpHeaders createHeaders() {
        return jwtService.createServiceAuthHeaders();
    }

    private HttpEntity<Void> createEntity() {
        return new HttpEntity<>(createHeaders());
    }

    public List<ReviewDto> getAllReviews() {
        String url = getReviewsUrl();
        log.info("[ReviewService] Calling review-service at: {}", url);
        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, createEntity(), String.class);

            log.info("[ReviewService] Response status: {}", response.getStatusCode());
            log.info("[ReviewService] Response body: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode contentNode = root.path("content");

                if (!contentNode.isArray()) {
                    log.warn("[ReviewService] 'content' field is missing or not an array. Full JSON: {}", root);
                    return new ArrayList<>();
                }

                List<ReviewDto> reviews = new ArrayList<>();
                for (JsonNode node : contentNode) {
                    ReviewDto dto = new ReviewDto();
                    dto.setId(node.has("reviewUuid") ? UUID.fromString(node.get("reviewUuid").asText()) : null);
                    dto.setUserId(node.has("userId") ? UUID.fromString(node.get("userId").asText()) : null);
                    dto.setCourseId(node.has("courseId") ? node.get("courseId").asLong() : null);
                    dto.setRating(node.has("rating") ? node.get("rating").asInt() : 0);
                    dto.setComment(node.has("comment") ? node.get("comment").asText() : null);
                    dto.setStatus(node.has("status") ? node.get("status").asText() : null);
                    if (node.has("createdAt") && !node.get("createdAt").isNull()) {
                        try {
                            dto.setCreatedAt(LocalDateTime.parse(node.get("createdAt").asText()));
                        } catch (Exception ex) {
                            log.warn("[ReviewService] Could not parse createdAt: {}", node.get("createdAt").asText());
                        }
                    }
                    reviews.add(dto);
                }
                log.info("[ReviewService] Returning {} reviews", reviews.size());
                return reviews;
            } else {
                log.warn("[ReviewService] Unexpected status {} or empty body", response.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("[ReviewService] HTTP error calling review-service: status={}, body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[ReviewService] Exception calling review-service at {}: {}", url, e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public boolean deleteReview(UUID id) {
        String url = getReviewsUrl() + "/" + id;
        log.info("[ReviewService] DELETE review at: {}", url);
        try {
            restTemplate.exchange(url, HttpMethod.DELETE, createEntity(), Void.class);
            return true;
        } catch (Exception ex) {
            log.error("[ReviewService] Failed to delete review {}: {}", id, ex.getMessage());
            return false;
        }
    }
}
