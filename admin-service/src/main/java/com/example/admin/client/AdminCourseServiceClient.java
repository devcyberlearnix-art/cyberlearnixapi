package com.example.admin.client;

import com.example.admin.security.JwtService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.example.admin.dto.EnrollmentInfoDTO;
import java.util.UUID;
import java.util.Map;

@Component
public class AdminCourseServiceClient {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    @Value("${course-service.url:http://localhost:8083}")
    private String courseServiceUrl;

    public AdminCourseServiceClient(RestTemplate restTemplate, JwtService jwtService) {
        this.restTemplate = restTemplate;
        this.jwtService = jwtService;
    }

    public List<CourseDTO> getAllCourses() {
        try {
            String url = courseServiceUrl + "/api/v1/courses";
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Object[].class
            );
            return parseCourseList(response.getBody());
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get courses from Course Service: " + e.getMessage());
            return List.of();
        }
    }

    public CourseDTO getCourseById(Long courseId) {
        try {
            String url = courseServiceUrl + "/api/v1/courses/" + courseId;
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Map.class
            );
            return mapToCourseDto(response.getBody());
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get course from Course Service: " + e.getMessage());
            return null;
        }
    }

    public CourseDTO updateCourseStatus(Long courseId, String status) {
        try {
            String url = courseServiceUrl + "/api/v1/courses/" + courseId + "/status";
            String json = "{\"status\":\"" + status + "\"}";

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest.Builder reqBuilder = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .method("PATCH", java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json");

            // propagate headers from createHeaders()
            HttpHeaders headers = createHeaders();
            headers.forEach((k, v) -> {
                if (v != null && !v.isEmpty()) {
                    reqBuilder.header(k, String.join(",", v));
                }
            });

            java.net.http.HttpRequest req = reqBuilder.build();
            java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map responseBody = mapper.readValue(resp.body(), Map.class);
                return mapToCourseDto(responseBody);
            } else {
                System.err.println("✗ Failed to update course status: " + resp.statusCode() + " - " + resp.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("✗ Failed to update course status: " + e.getMessage());
            return null;
        }
    }

    public Map deleteCourse(Long courseId) {
        try {
            String url = courseServiceUrl + "/api/v1/courses/" + courseId;
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, new HttpEntity<>(createHeaders()), Map.class);
            System.out.println("✓ Course deleted: " + courseId);
            Map result = response.getBody();
            return result != null ? result : Map.of("deleted", true, "courseId", courseId);
        } catch (HttpStatusCodeException e) {
            System.err.println("✗ Failed to delete course: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return null;
        } catch (RestClientException e) {
            System.err.println("✗ Failed to delete course: " + e.getMessage());
            return null;
        }
    }

    public List<CourseDTO> getCoursesByInstructor(String instructorId) {
        try {
            String courseUrl = courseServiceUrl + "/api/v1/courses?instructorId=" + instructorId;
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    courseUrl,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Object[].class
            );
            return parseCourseList(response.getBody());
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get courses for instructor: " + e.getMessage());
            return List.of();
        }
    }

    public List<EnrollmentInfoDTO> getEnrollmentsByUserId(UUID userId) {
        try {
            String url = courseServiceUrl + "/api/v1/enrollments/users/" + userId;
            ResponseEntity<EnrollmentInfoDTO[]> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    EnrollmentInfoDTO[].class
            );
            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get enrollments for user: " + e.getMessage());
            return List.of();
        }
}


    public List<Object> getCourseContent(Long courseId) {
        try {
            String url = courseServiceUrl + "/api/v1/courses/" + courseId + "/sections";
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Object[].class
            );
            if (response.getBody() != null) {
                return List.of(response.getBody());
            } else {
                return List.of();
            }
        } catch (HttpStatusCodeException e) {
            System.err.println("✗ Failed to get course content: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return List.of();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get course content: " + e.getMessage());
            return List.of();
        }
    }


    // --- Section & Lecture management ---
    public Map createSection(Long courseId, Map<String, Object> sectionPayload) {
        try {
            String url = courseServiceUrl + "/api/v1/courses/" + courseId + "/sections";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(sectionPayload, createHeaders()), Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to create section: " + e.getMessage());
            return null;
        }
    }

    public Map deleteSection(Long sectionId) {
        try {
            String url = courseServiceUrl + "/api/v1/courses/sections/" + sectionId;
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, new HttpEntity<>(createHeaders()), Map.class);
            System.out.println("✓ Section deleted: " + sectionId);
            Map result = response.getBody();
            return result != null ? result : Map.of("deleted", true, "sectionId", sectionId);
        } catch (HttpStatusCodeException e) {
            System.err.println("✗ Failed to delete section: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return null;
        } catch (RestClientException e) {
            System.err.println("✗ Failed to delete section: " + e.getMessage());
            return null;
        }
    }

    public Map createLecture(Long sectionId, Map<String, Object> lecturePayload) {
        try {
            String url = courseServiceUrl + "/api/v1/sections/" + sectionId + "/lectures";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(lecturePayload, createHeaders()), Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to create lecture: " + e.getMessage());
            return null;
        }
    }

    public Map updateLecturePreview(Long sectionId, Long lectureId, boolean previewEnabled) {
        try {
            String url = courseServiceUrl + "/api/v1/sections/" + sectionId + "/lectures/" + lectureId;
            String json = "{\"previewEnabled\":" + previewEnabled + "}";

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest.Builder reqBuilder = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .method("PATCH", java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json");

            HttpHeaders headers = createHeaders();
            headers.forEach((k, v) -> {
                if (v != null && !v.isEmpty()) {
                    reqBuilder.header(k, String.join(",", v));
                }
            });

            java.net.http.HttpRequest req = reqBuilder.build();
            java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map responseBody = mapper.readValue(resp.body(), Map.class);
                return responseBody;
            } else {
                System.err.println("✗ Failed to update lecture preview: " + resp.statusCode() + " - " + resp.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("✗ Failed to update lecture preview: " + e.getMessage());
            return null;
        }
    }

    public Map deleteLecture(Long sectionId, Long lectureId) {
        try {
            String url = courseServiceUrl + "/api/v1/sections/" + sectionId + "/lectures/" + lectureId;
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, new HttpEntity<>(createHeaders()), Map.class);
            System.out.println("✓ Lecture deleted: sectionId=" + sectionId + " lectureId=" + lectureId);
            Map result = response.getBody();
            return result != null ? result : Map.of("deleted", true, "sectionId", sectionId, "lectureId", lectureId);
        } catch (HttpStatusCodeException e) {
            System.err.println("✗ Failed to delete lecture: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return null;
        } catch (RestClientException e) {
            System.err.println("✗ Failed to delete lecture: " + e.getMessage());
            return null;
        }
    }

    private List<CourseDTO> parseCourseList(Object[] body) {
        if (body == null) {
            return List.of();
        }
        List<CourseDTO> result = new ArrayList<>();
        for (Object item : body) {
            result.add(mapToCourseDto(item));
        }
        return result;
    }

    private CourseDTO mapToCourseDto(Object body) {
        if (body == null) {
            return null;
        }

        if (body instanceof Map<?, ?> map) {
            Object data = map.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                return mapToCourseDto(dataMap);
            }
            if (data instanceof List<?> list) {
                if (list.isEmpty()) {
                    return null;
                }
                return mapToCourseDto(list.get(0));
            }
            if (data instanceof Object[] arr) {
                if (arr.length == 0) {
                    return null;
                }
                return mapToCourseDto(arr[0]);
            }

            return mapFromMap(map);
        }

        if (body instanceof Map.Entry<?, ?> entry) {
            return mapToCourseDto(entry.getValue());
        }

        return null;
    }

    private CourseDTO mapFromMap(Map<?, ?> map) {
        CourseDTO dto = new CourseDTO();
        Object id = map.get("id");
        if (id instanceof Number number) {
            dto.setId(number.longValue());
        }

        dto.setTitle(getString(map.get("title")));
        dto.setSubtitle(getString(map.get("subtitle")));
        dto.setDescription(getString(map.get("description")));
        dto.setCategory(getString(map.get("category")));
        dto.setLevel(getString(map.get("level")));
        dto.setLanguage(getString(map.get("language")));
        dto.setThumbnail(getString(map.get("thumbnail")));

        Object price = map.get("price");
        if (price instanceof Number number) {
            dto.setPrice(BigDecimal.valueOf(number.doubleValue()));
        } else if (price instanceof String priceText) {
            dto.setPrice(new BigDecimal(priceText));
        }

        Object instructorId = map.get("instructorId");
        if (instructorId != null) {
            // Courses created by the current user service identify instructors with UUIDs,
            // while legacy course records can still contain numeric IDs. Preserve either
            // representation instead of assuming a numeric value.
            dto.setInstructorId(String.valueOf(instructorId));
        }

        dto.setStatus(getString(map.get("status")));
        dto.setSlug(getString(map.get("slug")));
        return dto;
    }

    private String getString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = jwtService.createServiceAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // --- Banner Management ---
    public Map createBanner(Map<String, Object> bannerPayload) {
        try {
            String url = courseServiceUrl + "/api/v1/admin/banners";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(bannerPayload, createHeaders()), Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to create banner: " + e.getMessage());
            return null;
        }
    }

    public List<Map> getAllBanners() {
        try {
            String url = courseServiceUrl + "/api/v1/admin/banners";
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, new HttpEntity<>(createHeaders()), Map.class);
            Map body = response.getBody();
            if (body != null && body.get("data") instanceof List) {
                return (List<Map>) body.get("data");
            }
            return List.of();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get banners: " + e.getMessage());
            return List.of();
        }
    }

    public Map getBannerById(Long bannerId) {
        try {
            String url = courseServiceUrl + "/api/v1/admin/banners/" + bannerId;
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, new HttpEntity<>(createHeaders()), Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get banner: " + e.getMessage());
            return null;
        }
    }

    public Map updateBanner(Long bannerId, Map<String, Object> bannerPayload) {
        try {
            String url = courseServiceUrl + "/api/v1/admin/banners/" + bannerId;
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.PUT, new HttpEntity<>(bannerPayload, createHeaders()), Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to update banner: " + e.getMessage());
            return null;
        }
    }

    public Map partialUpdateBanner(Long bannerId, Map<String, Object> bannerPayload) {
        try {
            String url = courseServiceUrl + "/api/v1/admin/banners/" + bannerId;
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.PATCH, new HttpEntity<>(bannerPayload, createHeaders()), Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to partially update banner: " + e.getMessage());
            return null;
        }
    }

    public Map updateBannerStatus(Long bannerId, Map<String, Object> statusPayload) {
        try {
            String url = courseServiceUrl + "/api/v1/admin/banners/" + bannerId + "/status";
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.PATCH, new HttpEntity<>(statusPayload, createHeaders()), Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to update banner status: " + e.getMessage());
            return null;
        }
    }

    public List<Map> reorderBanners(Map<String, Object> reorderPayload) {
        try {
            String url = courseServiceUrl + "/api/v1/admin/banners/reorder";
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.PATCH, new HttpEntity<>(reorderPayload, createHeaders()), Map.class);
            Map body = response.getBody();
            if (body != null && body.get("data") instanceof List) {
                return (List<Map>) body.get("data");
            }
            return List.of();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to reorder banners: " + e.getMessage());
            return List.of();
        }
    }

    public Map deleteBanner(Long bannerId) {
        try {
            String url = courseServiceUrl + "/api/v1/admin/banners/" + bannerId;
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, new HttpEntity<>(createHeaders()), Map.class);
            System.out.println("✓ Banner deleted: " + bannerId);
            Map result = response.getBody();
            return result != null ? result : Map.of("deleted", true, "bannerId", bannerId);
        } catch (RestClientException e) {
            System.err.println("✗ Failed to delete banner: " + e.getMessage());
            return null;
        }
    }

    public Map getBannerAnalytics(Long bannerId) {
        try {
            String url = courseServiceUrl + "/api/v1/admin/banners/" + bannerId + "/analytics";
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, new HttpEntity<>(createHeaders()), Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get banner analytics: " + e.getMessage());
            return null;
        }
    }

    public String getCourseServiceUrl() {
        return courseServiceUrl;
    }

    // --- New Banner Features ---

    public Map uploadBannerImage(byte[] imageData, String imageType) {
        try {
            String url = courseServiceUrl + "/api/v1/admin/banners/upload-image?imageType=" + imageType;

            HttpHeaders headers = createHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            // Create a simple JSON payload for now
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("imageData", java.util.Base64.getEncoder().encodeToString(imageData));
            payload.put("imageType", imageType);

            org.springframework.http.HttpEntity<Map<String, Object>> requestEntity =
                    new org.springframework.http.HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to upload banner image: " + e.getMessage());
            return null;
        }
    }

    public Map validateBannerImage(byte[] imageData) {
        try {
            String url = courseServiceUrl + "/api/v1/admin/banners/validate-image";

            HttpHeaders headers = createHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("imageData", java.util.Base64.getEncoder().encodeToString(imageData));

            org.springframework.http.HttpEntity<Map<String, Object>> requestEntity =
                    new org.springframework.http.HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to validate banner image: " + e.getMessage());
            return null;
        }
    }

    public List<Map> getDeletedBanners() {
        try {
            String url = courseServiceUrl + "/api/v1/admin/banners/deleted";
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, new HttpEntity<>(createHeaders()), Map.class);
            Map body = response.getBody();
            if (body != null && body.get("data") instanceof List) {
                return (List<Map>) body.get("data");
            }
            return List.of();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get deleted banners: " + e.getMessage());
            return List.of();
        }
    }

    public Map restoreBanner(Long bannerId) {
        try {
            String url = courseServiceUrl + "/api/v1/admin/banners/" + bannerId + "/restore";
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.POST, new HttpEntity<>(createHeaders()), Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to restore banner: " + e.getMessage());
            return null;
        }
    }

    public List<Map> getActiveBannersByTargetType(String targetType) {
        try {
            String url = courseServiceUrl + "/api/v1/banners/target/" + targetType;
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, new HttpEntity<>(createHeaders()), Map.class);
            Map body = response.getBody();
            if (body != null && body.get("data") instanceof List) {
                return (List<Map>) body.get("data");
            }
            return List.of();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get banners by target type: " + e.getMessage());
            return List.of();
        }
    }

    public Map trackBannerConversion(Long bannerId) {
        try {
            String url = courseServiceUrl + "/api/v1/banners/" + bannerId + "/conversion";
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.POST, new HttpEntity<>(createHeaders()), Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("✗ Failed to track banner conversion: " + e.getMessage());
            return null;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CourseDTO {
        private Long id;
        private String title;
        private String subtitle;
        private String description;
        private String category;
        private String level;
        private String language;
        private BigDecimal price;
        private String thumbnail;
        private String instructorId;
        private String status;
        private String slug;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateCourseRequest {
        private String status;
    }
}
