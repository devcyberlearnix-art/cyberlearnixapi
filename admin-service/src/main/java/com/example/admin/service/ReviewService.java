package com.example.admin.service;

import com.example.admin.dto.ReviewDto;
import com.example.admin.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Value("${review-service.url:http://localhost:8089/api/admin/reviews}")
    private String reviewServiceUrl;

    private HttpHeaders createHeaders() {
        return jwtService.createServiceAuthHeaders();
    }

    private HttpEntity<Void> createEntity() {
        return new HttpEntity<>(null); // No auth headers since /api/admin/reviews is now public
    }

    public List<ReviewDto> getAllReviews() {
        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(
                            reviewServiceUrl,
                            HttpMethod.GET,
                            createEntity(),
                            String.class
                    );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode contentNode = root.path("content");
                
                List<ReviewDto> reviews = new ArrayList<>();
                if (contentNode.isArray()) {
                    for (JsonNode node : contentNode) {
                        ReviewDto dto = new ReviewDto();
                        dto.setId(node.has("reviewUuid") ? UUID.fromString(node.get("reviewUuid").asText()) : null);
                        dto.setUserId(node.has("userId") ? UUID.fromString(node.get("userId").asText()) : null);
                        dto.setCourseId(node.has("courseId") ? node.get("courseId").asLong() : null);
                        dto.setRating(node.has("rating") ? node.get("rating").asInt() : null);
                        dto.setComment(node.has("comment") ? node.get("comment").asText() : null);
                        reviews.add(dto);
                    }
                }
                return reviews;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    public boolean deleteReview(UUID id) {
        try {
            String url = reviewServiceUrl + "/" + id;
            restTemplate.exchange(url, HttpMethod.DELETE, createEntity(), Void.class);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
