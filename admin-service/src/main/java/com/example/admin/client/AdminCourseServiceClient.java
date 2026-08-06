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

    public boolean deleteCourse(Long courseId) {
        try {
            String url = courseServiceUrl + "/api/v1/courses/" + courseId;
            restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, new HttpEntity<>(createHeaders()), Void.class);
            System.out.println("✓ Course deleted: " + courseId);
            return true;
        } catch (RestClientException e) {
            System.err.println("✗ Failed to delete course: " + e.getMessage());
            return false;
        }
    }

    public List<CourseDTO> getCoursesByInstructor(Long instructorId) {
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

    public List<Object> getCourseContent(Long courseId) {
        try {
            String url = courseServiceUrl + "/api/v1/courses/" + courseId + "/sections";
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Object[].class
            );
            List<Object> data = new ArrayList<>();
            if (response.getBody() != null) {
                for (Object item : response.getBody()) {
                    if (item instanceof Map<?, ?> map) {
                        Map<String, Object> normalized = new java.util.LinkedHashMap<>();
                        normalized.put("id", map.get("id"));
                        normalized.put("title", map.get("title"));
                        normalized.put("orderIndex", map.get("orderIndex"));
                        normalized.put("courseId", map.get("course") != null && map.get("course") instanceof Map<?, ?> courseMap ? courseMap.get("id") : null);
                        data.add(normalized);
                    } else {
                        data.add(item);
                    }
                }
            }
            return data;
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

    public boolean deleteSection(Long sectionId) {
        try {
            // course-service security expects DELETE on /courses/sections/{id}
            String url = courseServiceUrl + "/api/v1/courses/sections/" + sectionId;
            restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, new HttpEntity<>(createHeaders()), Void.class);
            return true;
        } catch (RestClientException e) {
            System.err.println("✗ Failed to delete section: " + e.getMessage());
            return false;
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

    public boolean deleteLecture(Long sectionId, Long lectureId) {
        try {
            String url = courseServiceUrl + "/api/v1/sections/" + sectionId + "/lectures/" + lectureId;
            restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, new HttpEntity<>(createHeaders()), Void.class);
            return true;
        } catch (RestClientException e) {
            System.err.println("✗ Failed to delete lecture: " + e.getMessage());
            return false;
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
        if (instructorId instanceof Number number) {
            dto.setInstructorId(number.longValue());
        } else if (instructorId instanceof String instructorIdText) {
            dto.setInstructorId(Long.parseLong(instructorIdText));
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
        private Long instructorId;
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
