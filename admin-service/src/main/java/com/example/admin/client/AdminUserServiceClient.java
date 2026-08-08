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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AdminUserServiceClient {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    @Value("${user-service.url:http://localhost:8091}")
    private String userServiceUrl;

    public AdminUserServiceClient(RestTemplate restTemplate, JwtService jwtService) {
        this.restTemplate = restTemplate;
        this.jwtService = jwtService;
    }

    private HttpHeaders createHeaders() {
        return jwtService.createServiceAuthHeaders();
    }

    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public List<UserDTO> getAllUsers() {
        try {
            String url = userServiceUrl + "/api/v1/users";
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Map.class
            );
            return parseUserList(response.getBody());
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get users from User Service: " + e.getMessage());
            return List.of();
        }
    }

    public UserDTO getUserById(UUID id) {
        try {
            String url = userServiceUrl + "/api/v1/users/" + id;
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Map.class
            );
            return mapToUserDto(response.getBody());
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get user from User Service: " + e.getMessage());
            return null;
        }
    }

    public UserDTO updateUserStatus(UUID id, String status) {
        try {
            String url = userServiceUrl + "/api/v1/users/" + id + "/status";
            Map<String, String> requestBody = Map.of("status", status);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.PUT,
                    new HttpEntity<>(requestBody, createJsonHeaders()),
                    Map.class
            );
            return mapToUserDto(response.getBody());
        } catch (RestClientException e) {
            System.err.println("✗ Failed to update user status: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteUser(UUID id) {
        try {
            String url = userServiceUrl + "/api/v1/users/" + id;
            restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, new HttpEntity<>(createHeaders()), Void.class);
            System.out.println("✓ User deleted: " + id);
            return true;
        } catch (RestClientException e) {
            System.err.println("✗ Failed to delete user: " + e.getMessage());
            return false;
        }
    }

    public List<UserDTO> getAllInstructors() {
        try {
            String url = userServiceUrl + "/api/v1/admin/instructors";
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Object[].class
            );
            return parseUserList(response.getBody());
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get instructors from User Service: " + e.getMessage());
            return List.of();
        }
    }

    public List<InstructorApplicationDTO> getAllInstructorApplications() {
        try {
            String url = userServiceUrl + "/api/v1/admin/instructors/applications";
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Object[].class
            );
            return parseApplicationList(response.getBody());
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get instructor applications from User Service: " + e.getMessage());
            return List.of();
        }
    }

    public InstructorApplicationDTO approveInstructorApplication(UUID userId) {
        try {
            String url = userServiceUrl + "/api/v1/admin/instructors/applications/" + userId + "/approve";
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.PUT,
                    new HttpEntity<>(createHeaders()),
                    Map.class
            );
            return mapToApplicationDto(response.getBody());
        } catch (RestClientException e) {
            System.err.println("✗ Failed to approve instructor application: " + e.getMessage());
            return null;
        }
    }

    public InstructorApplicationDTO approveInstructorApplication(UUID userId, String authorizationHeader) {
        try {
            String url = userServiceUrl + "/api/v1/admin/instructors/applications/" + userId + "/approve";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorizationHeader);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.PUT,
                    new HttpEntity<>(headers),
                    Map.class
            );
            return mapToApplicationDto(response.getBody());
        } catch (RestClientException e) {
            System.err.println("✗ Failed to approve instructor application: " + e.getMessage());
            return null;
        }
    }

    public InstructorApplicationDTO rejectInstructorApplication(UUID userId) {
        try {
            String url = userServiceUrl + "/api/v1/admin/instructors/applications/" + userId + "/reject";
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.PUT,
                    new HttpEntity<>(createHeaders()),
                    Map.class
            );
            return mapToApplicationDto(response.getBody());
        } catch (RestClientException e) {
            System.err.println("✗ Failed to reject instructor application: " + e.getMessage());
            return null;
        }
    }

    public InstructorApplicationDTO rejectInstructorApplication(UUID userId, String authorizationHeader) {
        try {
            String url = userServiceUrl + "/api/v1/admin/instructors/applications/" + userId + "/reject";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorizationHeader);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.PUT,
                    new HttpEntity<>(headers),
                    Map.class
            );
            return mapToApplicationDto(response.getBody());
        } catch (RestClientException e) {
            System.err.println("✗ Failed to reject instructor application: " + e.getMessage());
            return null;
        }
    }

    private List<UserDTO> parseUserList(Object body) {
        if (body == null) {
            return List.of();
        }
        if (body instanceof Map<?, ?> map) {
            Object data = map.get("data");
            if (data instanceof List<?> list) {
                List<UserDTO> result = new ArrayList<>();
                for (Object item : list) {
                    result.add(mapToUserDto(item));
                }
                return result;
            }
            if (data instanceof Object[] arr) {
                List<UserDTO> result = new ArrayList<>();
                for (Object item : arr) {
                    result.add(mapToUserDto(item));
                }
                return result;
            }
        }
        if (body instanceof Object[] arr) {
            List<UserDTO> result = new ArrayList<>();
            for (Object item : arr) {
                result.add(mapToUserDto(item));
            }
            return result;
        }
        return List.of();
    }

    private List<InstructorApplicationDTO> parseApplicationList(Object body) {
        if (body == null) {
            return List.of();
        }
        if (body instanceof Map<?, ?> map) {
            Object data = map.get("data");
            if (data instanceof List<?> list) {
                List<InstructorApplicationDTO> result = new ArrayList<>();
                for (Object item : list) {
                    result.add(mapToApplicationDto(item));
                }
                return result;
            }
            if (data instanceof Object[] arr) {
                List<InstructorApplicationDTO> result = new ArrayList<>();
                for (Object item : arr) {
                    result.add(mapToApplicationDto(item));
                }
                return result;
            }
        }
        if (body instanceof Object[] arr) {
            List<InstructorApplicationDTO> result = new ArrayList<>();
            for (Object item : arr) {
                result.add(mapToApplicationDto(item));
            }
            return result;
        }
        return List.of();
    }

    private UserDTO mapToUserDto(Object body) {
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

    private InstructorApplicationDTO mapToApplicationDto(Object body) {
        if (body == null) {
            return null;
        }
        if (body instanceof Map<?, ?> map) {
            Object data = map.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                return mapApplicationFromMap(dataMap);
            }
            return mapApplicationFromMap(map);
        }
        return null;
    }

    private UserDTO mapFromMap(Map<?, ?> map) {
        UserDTO dto = new UserDTO();
        dto.setId(getUuid(map.get("id")));
        dto.setEmail(getString(map.get("email")));
        dto.setRole(getString(map.get("role")));
        dto.setStatus(getString(map.get("status")));
        dto.setCreatedAt(getString(map.get("createdAt")));
        dto.setFirstName(getString(map.get("firstName")));
        dto.setLastName(getString(map.get("lastName")));
        dto.setMobileNumber(getString(map.get("mobileNumber")));
        dto.setProfilePhoto(getString(map.get("profilePhoto")));
        return dto;
    }

    private InstructorApplicationDTO mapApplicationFromMap(Map<?, ?> map) {
        InstructorApplicationDTO dto = new InstructorApplicationDTO();
        dto.setUserId(getUuid(map.get("userId")));
        dto.setEmail(getString(map.get("email")));
        dto.setFirstName(getString(map.get("firstName")));
        dto.setLastName(getString(map.get("lastName")));
        dto.setStatus(getString(map.get("status")));
        dto.setAppliedAt(getString(map.get("appliedAt")));
        dto.setResumeUrl(getString(map.get("resumeUrl")));
        dto.setEducationalCertificatesUrl(getString(map.get("educationalCertificatesUrl")));
        dto.setGovernmentIdProofUrl(getString(map.get("governmentIdProofUrl")));
        dto.setExperienceLetterUrl(getString(map.get("experienceLetterUrl")));
        dto.setInternshipCertificateUrl(getString(map.get("internshipCertificateUrl")));
        dto.setSkillCertificatesUrl(getString(map.get("skillCertificatesUrl")));
        dto.setPortfolioUrl(getString(map.get("portfolioUrl")));
        dto.setDemoLecturePptUrl(getString(map.get("demoLecturePptUrl")));
        dto.setDemoLectureRecordingUrl(getString(map.get("demoLectureRecordingUrl")));
        dto.setProjectsUrl(getString(map.get("projectsUrl")));
        dto.setPassportPhotoUrl(getString(map.get("passportPhotoUrl")));
        dto.setBankDetailsUrl(getString(map.get("bankDetailsUrl")));
        dto.setPanDocumentUrl(getString(map.get("panDocumentUrl")));
        dto.setApplicationFormUrl(getString(map.get("applicationFormUrl")));
        dto.setBankAccountNumber(getString(map.get("bankAccountNumber")));
        dto.setBankIfsc(getString(map.get("bankIfsc")));
        dto.setBankName(getString(map.get("bankName")));
        dto.setPanNumber(getString(map.get("panNumber")));
        dto.setAdditionalNotes(getString(map.get("additionalNotes")));
        return dto;
    }

    private String getString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private UUID getUuid(Object value) {
        if (value == null) return null;
        if (value instanceof UUID uuid) return uuid;
        try {
            return UUID.fromString(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserDTO {
        private UUID id;
        private String email;
        private String role;
        private String status;
        private String createdAt;
        private String firstName;
        private String lastName;
        private String mobileNumber;
        private String profilePhoto;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InstructorApplicationDTO {
        private UUID userId;
        private String email;
        private String firstName;
        private String lastName;
        private String status;
        private String appliedAt;
        private String resumeUrl;
        private String educationalCertificatesUrl;
        private String governmentIdProofUrl;
        private String experienceLetterUrl;
        private String internshipCertificateUrl;
        private String skillCertificatesUrl;
        private String portfolioUrl;
        private String demoLecturePptUrl;
        private String demoLectureRecordingUrl;
        private String projectsUrl;
        private String passportPhotoUrl;
        private String bankDetailsUrl;
        private String panDocumentUrl;
        private String applicationFormUrl;
        private String bankAccountNumber;
        private String bankIfsc;
        private String bankName;
        private String panNumber;
        private String additionalNotes;
    }
}
