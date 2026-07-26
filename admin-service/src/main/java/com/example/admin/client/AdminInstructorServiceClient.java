package com.example.admin.client;

import com.example.admin.security.JwtService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AdminInstructorServiceClient {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    @Value("${instructor-service.url:http://localhost:8088}")
    private String instructorServiceUrl;

    public AdminInstructorServiceClient(RestTemplate restTemplate, JwtService jwtService) {
        this.restTemplate = restTemplate;
        this.jwtService = jwtService;
    }

    private HttpHeaders createHeaders() {
        return jwtService.createServiceAuthHeaders();
    }

    public List<InstructorDTO> getAllInstructors() {
        try {
            String url = instructorServiceUrl + "/api/v1/instructors";
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Object[].class
            );
            return parseInstructorList(response.getBody());
        } catch (Exception e) {
            System.err.println("✗ Failed to get instructors from Instructor Service: " + e.getMessage());
            return List.of();
        }
    }

    public InstructorDTO getInstructorById(Long instructorId) {
        try {
            String url = instructorServiceUrl + "/api/v1/instructors/" + instructorId;
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Map.class
            );
            return mapToInstructorDto(response.getBody());
        } catch (Exception e) {
            System.err.println("✗ Failed to get instructor from Instructor Service: " + e.getMessage());
            return null;
        }
    }

    public List<CourseDTO> getCoursesByInstructor(Long instructorId) {
        try {
            String url = instructorServiceUrl + "/api/v1/instructors/" + instructorId + "/courses";
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Object[].class
            );
            return parseCourseList(response.getBody());
        } catch (Exception e) {
            System.err.println("✗ Failed to get courses for instructor: " + e.getMessage());
            return List.of();
        }
    }

    private List<InstructorDTO> parseInstructorList(Object[] body) {
        if (body == null) {
            return List.of();
        }
        List<InstructorDTO> result = new ArrayList<>();
        for (Object item : body) {
            result.add(mapToInstructorDto(item));
        }
        return result;
    }

    private InstructorDTO mapToInstructorDto(Object body) {
        if (body == null) {
            return null;
        }
        if (body instanceof Map<?, ?> map) {
            Object data = map.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                return mapFromMap(dataMap);
            }
            return mapFromMap(map);
        }
        return null;
    }

    private InstructorDTO mapFromMap(Map<?, ?> map) {
        InstructorDTO dto = new InstructorDTO();
        Object id = map.get("id");
        if (id instanceof Number number) {
            dto.setId(number.longValue());
        }
        dto.setUserId(getString(map.get("userId")));
        dto.setFirstName(getString(map.get("firstName")));
        dto.setLastName(getString(map.get("lastName")));
        dto.setEmail(getString(map.get("email")));
        dto.setSpecialization(getString(map.get("specialization")));
        dto.setBio(getString(map.get("bio")));
        dto.setStatus(getString(map.get("status")));
        return dto;
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
                return mapCourseFromMap(dataMap);
            }
            return mapCourseFromMap(map);
        }
        return null;
    }

    private CourseDTO mapCourseFromMap(Map<?, ?> map) {
        CourseDTO dto = new CourseDTO();
        Object id = map.get("id");
        if (id instanceof Number number) {
            dto.setId(number.longValue());
        }
        dto.setTitle(getString(map.get("title")));
        dto.setDescription(getString(map.get("description")));
        dto.setCategory(getString(map.get("category")));
        dto.setLevel(getString(map.get("level")));
        dto.setStatus(getString(map.get("status")));
        return dto;
    }

    private String getString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InstructorDTO {
        private Long id;
        private String userId;
        private String firstName;
        private String lastName;
        private String email;
        private String specialization;
        private String bio;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CourseDTO {
        private Long id;
        private String title;
        private String description;
        private String category;
        private String level;
        private String status;
    }
}
