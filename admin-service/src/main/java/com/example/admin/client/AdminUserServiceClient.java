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
import java.util.HashMap;
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

    public Map<String, Object> getAllUsers(int page, int size) {
        try {
            String url = userServiceUrl + "/api/v1/users?page=" + page + "&size=" + size;
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Map.class
            );
            return parsePaginatedUserList(response.getBody());
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get users from User Service: " + e.getMessage());
            return Map.of("users", List.of(), "totalUsers", 0, "currentPage", 0, "totalPages", 0);
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
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            String body = e.getResponseBodyAsString();
            String message = extractMessageFromJson(body);
            if (message == null) {
                message = e.getStatusText();
            }
            throw new RuntimeException(message);
        } catch (RestClientException e) {
            System.err.println("✗ Failed to update user status: " + e.getMessage());
            throw new RuntimeException("Failed to connect to user service: " + e.getMessage());
        }
    }

    private String extractMessageFromJson(String json) {
        if (json == null) return null;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<?, ?> map = mapper.readValue(json, Map.class);
            if (map.containsKey("message")) {
                return String.valueOf(map.get("message"));
            }
        } catch (Exception ignored) {}
        return null;
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

        public List<UserDTO> getAllInstructors(String authorization) {
        try {
            String url = userServiceUrl + "/api/v1/admin/instructors";
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class
            );
            return parseUserList(response.getBody());
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get instructors from User Service: " + e.getMessage());
            return List.of();
        }
    }

        public List<InstructorApplicationDTO> getAllInstructorApplications(String authorization) {
        try {
            String url = userServiceUrl + "/api/v1/admin/instructors/applications";
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class
            );
            return parseApplicationList(response.getBody());
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get instructor applications from User Service: " + e.getMessage());
            return List.of();
        }
    }

    public Map<String, Object> getAllInstructorApplicationsPaginated(String authorization, int page, int size) {
        try {
            String url = userServiceUrl + "/api/v1/admin/instructors/applications?page=" + page + "&size=" + size;
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
            );
            return parsePaginatedApplicationList(response.getBody());
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get instructor applications from User Service: " + e.getMessage());
            return Map.of("applications", List.of(), "currentPage", 0, "totalPages", 0, "totalApplications", 0, "pageSize", size);
        }
    }

    public Map<String, Object> getInstructorApplicationsByStatusPaginated(String authorization, String status, int page, int size) {
        try {
            // Updated to use industry-standard query parameter approach
            String url = userServiceUrl + "/api/v1/admin/instructors/applications?status=" + status + "&page=" + page + "&size=" + size;
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
            );
            return parsePaginatedApplicationList(response.getBody());
        } catch (RestClientException e) {
            System.err.println("✗ Failed to get instructor applications by status from User Service: " + e.getMessage());
            return Map.of("applications", List.of(), "currentPage", 0, "totalPages", 0, "totalApplications", 0, "pageSize", size, "status", status);
        }
    }

    public InstructorApplicationDTO approveInstructorApplication(UUID applicationId) {
        try {
            String url = userServiceUrl + "/api/v1/admin/instructors/applications/" + applicationId + "/approve";
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

    public InstructorApplicationDTO approveInstructorApplication(UUID applicationId, String authorizationHeader) {
        try {
            String url = userServiceUrl + "/api/v1/admin/instructors/applications/" + applicationId + "/approve";
            HttpHeaders headers = createHeaders();
            headers.set("X-User-Authorization", authorizationHeader);
            System.out.println("=== AdminUserServiceClient Debug ===");
            System.out.println("URL: " + url);
            System.out.println("Authorization header: " + headers.getFirst("Authorization"));
            System.out.println("X-User-Authorization header: " + headers.getFirst("X-User-Authorization"));
            System.out.println("====================================");
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.PUT,
                    new HttpEntity<>(headers),
                    Map.class
            );
            return mapToApplicationDto(response.getBody());
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            System.err.println("✗ Failed to approve instructor application:");
            System.err.println("  Status: " + e.getStatusCode());
            System.err.println("  Response: " + e.getResponseBodyAsString());
            return null;
        } catch (RestClientException e) {
            System.err.println("✗ Failed to approve instructor application: " + e.getMessage());
            return null;
        }
    }

    public InstructorApplicationDTO rejectInstructorApplication(UUID applicationId) {
        try {
            String url = userServiceUrl + "/api/v1/admin/instructors/applications/" + applicationId + "/reject";
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

    public InstructorApplicationDTO rejectInstructorApplication(UUID applicationId, String authorizationHeader) {
        try {
            String url = userServiceUrl + "/api/v1/admin/instructors/applications/" + applicationId + "/reject";
            HttpHeaders headers = createHeaders();
            headers.set("X-User-Authorization", authorizationHeader);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.PUT,
                    new HttpEntity<>(headers),
                    Map.class
            );
            return mapToApplicationDto(response.getBody());
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            System.err.println("✗ Failed to reject instructor application:");
            System.err.println("  Status: " + e.getStatusCode());
            System.err.println("  Response: " + e.getResponseBodyAsString());
            return null;
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

    private Map<String, Object> parsePaginatedUserList(Object body) {
        if (body == null) {
            return Map.of("users", List.of(), "totalUsers", 0, "currentPage", 0, "totalPages", 0);
        }
        if (body instanceof Map<?, ?> map) {
            Object data = map.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                Object users = dataMap.get("users");
                List<UserDTO> userList = new ArrayList<>();
                
                if (users instanceof List<?> list) {
                    for (Object item : list) {
                        userList.add(mapToUserDto(item));
                    }
                } else if (users instanceof Object[] arr) {
                    for (Object item : arr) {
                        userList.add(mapToUserDto(item));
                    }
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("users", userList);
                result.put("totalUsers", getNumber(dataMap.get("totalUsers")));
                result.put("currentPage", getNumber(dataMap.get("currentPage")));
                result.put("totalPages", getNumber(dataMap.get("totalPages")));
                result.put("pageSize", getNumber(dataMap.get("pageSize")));
                return result;
            }
        }
        return Map.of("users", List.of(), "totalUsers", 0, "currentPage", 0, "totalPages", 0);
    }

    private int getNumber(Object value) {
        if (value == null) return 0;
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
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

    private Map<String, Object> parsePaginatedApplicationList(Object body) {
        if (body == null) {
            return Map.of("applications", List.of(), "currentPage", 0, "totalPages", 0, "totalApplications", 0, "pageSize", 10);
        }
        if (body instanceof Map<?, ?> map) {
            Object data = map.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                Object applications = dataMap.get("applications");
                List<InstructorApplicationDTO> applicationList = new ArrayList<>();

                if (applications instanceof List<?> list) {
                    for (Object item : list) {
                        applicationList.add(mapToApplicationDto(item));
                    }
                } else if (applications instanceof Object[] arr) {
                    for (Object item : arr) {
                        applicationList.add(mapToApplicationDto(item));
                    }
                }

                Map<String, Object> result = new HashMap<>();
                result.put("applications", applicationList);
                result.put("currentPage", getNumber(dataMap.get("currentPage")));
                result.put("totalPages", getNumber(dataMap.get("totalPages")));
                result.put("totalApplications", getNumber(dataMap.get("totalApplications")));
                result.put("pageSize", getNumber(dataMap.get("pageSize")));
                if (dataMap.get("status") != null) {
                    result.put("status", String.valueOf(dataMap.get("status")));
                }
                return result;
            }
        }
        return Map.of("applications", List.of(), "currentPage", 0, "totalPages", 0, "totalApplications", 0, "pageSize", 10);
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
        // Handle both 'id' and 'userId' field names
        UUID id = getUuid(map.get("id"));
        if (id == null) {
            id = getUuid(map.get("userId"));
        }
        dto.setId(id);
        dto.setEmail(getString(map.get("email")));
        dto.setRole(getString(map.get("role")));
        dto.setStatus(getString(map.get("status")));
        dto.setCreatedAt(getString(map.get("createdAt")));
        dto.setFirstName(getString(map.get("firstName")));
        dto.setLastName(getString(map.get("lastName")));
        dto.setMobileNumber(getString(map.get("mobileNumber") != null ? map.get("mobileNumber") : map.get("mobile")));
        dto.setProfilePhoto(getString(map.get("profilePhoto")));
        dto.setBio(getString(map.get("bio")));
        dto.setSpecialization(getString(map.get("specialization")));
        dto.setSkills(getString(map.get("skills")));
        dto.setHighestQualification(getString(map.get("highestQualification")));
        dto.setOrganization(getString(map.get("organization")));
        dto.setFieldOfStudy(getString(map.get("fieldOfStudy")));
        dto.setCity(getString(map.get("city")));
        dto.setState(getString(map.get("state")));
        dto.setCountry(getString(map.get("country")));
        dto.setPreferredLanguage(getString(map.get("preferredLanguage")));
        dto.setAppliedRole(getString(map.get("appliedRole")));
        Object isAppr = map.get("isInstructorApproved");
        if (isAppr instanceof Boolean b) {
            dto.setIsInstructorApproved(b);
        } else if (isAppr != null) {
            dto.setIsInstructorApproved(Boolean.parseBoolean(String.valueOf(isAppr)));
        }
        return dto;
    }

    private InstructorApplicationDTO mapApplicationFromMap(Map<?, ?> map) {
        InstructorApplicationDTO dto = new InstructorApplicationDTO();

        // The user service returns the current nested application contract. Preserve
        // that structure before falling back to the legacy flattened response shape.
        if (map.get("application") instanceof Map<?, ?> application) {
            dto.setApplicationId(getUuid(application.get("applicationId")));
            dto.setStatus(getString(application.get("status")));
            dto.setReviewMessage(getString(application.get("reviewMessage")));
            dto.setAppliedAt(getString(application.get("submittedAt")));

            if (map.get("user") instanceof Map<?, ?> user) {
                dto.setUserId(getUuid(user.get("userId")));
                dto.setEmail(getString(user.get("email")));
                dto.setCurrentRole(getString(user.get("currentRole")));
                dto.setAppliedRole(getString(user.get("appliedRole")));
                dto.setAccountStatus(getString(user.get("accountStatus")));
                dto.setIsInstructorApproved(getBoolean(user.get("isInstructorApproved")));
            }

            if (map.get("documents") instanceof Map<?, ?> documents) {
                dto.setRequiredDocuments(getBooleanMap(documents.get("required")));
                dto.setOptionalDocuments(getBooleanMap(documents.get("optional")));
            }

            dto.setNextSteps(getStringList(map.get("nextSteps")));
            return dto;
        }

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

    private Boolean getBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return value == null ? null : Boolean.parseBoolean(String.valueOf(value));
    }

    private Map<String, Boolean> getBooleanMap(Object value) {
        if (!(value instanceof Map<?, ?> source)) {
            return null;
        }

        Map<String, Boolean> result = new java.util.LinkedHashMap<>();
        source.forEach((key, item) -> {
            if (key != null) {
                result.put(String.valueOf(key), getBoolean(item));
            }
        });
        return result;
    }

    private List<String> getStringList(Object value) {
        if (!(value instanceof List<?> source)) {
            return null;
        }
        return source.stream().map(String::valueOf).toList();
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
        private String bio;
        private String specialization;
        private String skills;
        private String highestQualification;
        private String organization;
        private String fieldOfStudy;
        private String city;
        private String state;
        private String country;
        private String preferredLanguage;
        private String appliedRole;
        private Boolean isInstructorApproved;

        public String getMobile() {
            return mobileNumber;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InstructorApplicationDTO {
        private UUID applicationId;
        private UUID userId;
        private String email;
        private String firstName;
        private String lastName;
        private String status;
        private String reviewMessage;
        private String appliedAt;
        private String currentRole;
        private String appliedRole;
        private String accountStatus;
        private Boolean isInstructorApproved;
        private Map<String, Boolean> requiredDocuments;
        private Map<String, Boolean> optionalDocuments;
        private List<String> nextSteps;
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
