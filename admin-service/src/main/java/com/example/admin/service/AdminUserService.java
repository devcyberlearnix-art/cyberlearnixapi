package com.example.admin.service;

import com.example.admin.client.AdminCourseServiceClient;
import com.example.admin.client.AdminUserServiceClient;
import com.example.admin.dto.*;
import com.example.admin.entity.Admin;
import com.example.admin.entity.AdminApprovalStatus;
import com.example.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserServiceClient userClient;
    private final AdminCourseServiceClient courseClient;
    private final com.example.admin.client.AdminInstructorServiceClient instructorClient;
    private final AdminRepository adminRepository;



    public AdminUsersResponse getAllUsers(int page, int size) {

        Map<String, Object> usersData = userClient.getAllUsers(page, size);

        @SuppressWarnings("unchecked")
        List<AdminUserServiceClient.UserDTO> users = (List<AdminUserServiceClient.UserDTO>) usersData.get("users");

        List<AdminUsersResponse.UserInfo> userList = users.stream()

                .map(user -> AdminUsersResponse.UserInfo.builder()

                        .id(user.getId())

                        .email(user.getEmail())

                        .role(user.getRole())

                        .status(user.getStatus())

                        .createdAt(user.getCreatedAt())

                        .build())

                .toList();

        int totalUsers = (int) usersData.getOrDefault("totalUsers", 0);

        return AdminUsersResponse.builder()

                .success(true)

                .message("Users fetched successfully")

                .timestamp(LocalDateTime.now().toString())

                .data(AdminUsersResponse.DataInfo.builder()

                        .totalUsers(totalUsers)

                        .users(userList)

                        .build())

                .build();

    }



    public AdminSingleUserResponse getUserById(UUID id) {

        AdminUserServiceClient.UserDTO user = userClient.getUserById(id);

        

        if (user == null) {

            return AdminSingleUserResponse.builder()

                    .success(false)

                    .message("User not found")

                    .timestamp(LocalDateTime.now().toString())

                    .build();

        }



        UserProfileResponse profile = convertToProfileResponse(user);

        return AdminSingleUserResponse.builder()

                .success(true)

                .message("User fetched successfully")

                .data(profile)

                .timestamp(LocalDateTime.now().toString())

                .build();

    }

    public AdminSingleUserResponse updateUserStatus(UUID id, UpdateUserStatusRequest request) {
        // 1. First, check if the user is an Admin stored in admin-service DB
        Optional<Admin> adminOpt = adminRepository.findById(id);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            String status = request.getStatus().toUpperCase();
            switch (status) {
                case "ACTIVE", "APPROVED" -> admin.setApprovalStatus(AdminApprovalStatus.APPROVED);
                case "INACTIVE", "SUSPENDED", "REJECTED" -> admin.setApprovalStatus(AdminApprovalStatus.REJECTED);
                default -> admin.setApprovalStatus(AdminApprovalStatus.PENDING);
            }
            adminRepository.save(admin);

            UserProfileResponse profile = new UserProfileResponse();
            profile.setUserId(admin.getId());
            profile.setEmail(admin.getEmail());
            profile.setFirstName(admin.getFirstName());
            profile.setLastName(admin.getLastName());
            profile.setMobile(admin.getMobileNumber());
            profile.setRole(admin.getRole());
            profile.setStatus(admin.getApprovalStatus().name());
            profile.setCreatedAt(admin.getCreatedAt() != null ? admin.getCreatedAt().toString() : null);
            profile.setEnrollments(List.of());
            profile.setEnrollmentCount(0);

            return AdminSingleUserResponse.builder()
                    .success(true)
                    .message("Admin status updated successfully")
                    .data(profile)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        // 2. Otherwise, update via user-service (regular users: students, instructors)
        try {
            AdminUserServiceClient.UserDTO user = userClient.updateUserStatus(id, request.getStatus());
            if (user == null) {
                return AdminSingleUserResponse.builder()
                        .success(false)
                        .message("Error updating user status: User not found")
                        .timestamp(LocalDateTime.now().toString())
                        .build();
            }
            UserProfileResponse profile = convertToProfileResponse(user);
            return AdminSingleUserResponse.builder()
                    .success(true)
                    .message("User status updated successfully")
                    .data(profile)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        } catch (Exception e) {
            return AdminSingleUserResponse.builder()
                    .success(false)
                    .message("Error updating user status: " + e.getMessage())
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }
    }

    public AdminDeleteUserResponse deleteUser(UUID id) {

        boolean deleted = userClient.deleteUser(id);

        

        if (!deleted) {

            return new AdminDeleteUserResponse(

                    false,

                    "Failed to delete user",

                    null,

                    LocalDateTime.now()

            );

        }



        return new AdminDeleteUserResponse(

                true,

                "User deleted successfully",

                null,

                LocalDateTime.now()

        );

    }

        public AdminUsersResponse getAllInstructors(String authorization) {

                List<AdminUserServiceClient.UserDTO> instructors = userClient.getAllInstructors(authorization);

        

        if (instructors.isEmpty()) {

            return AdminUsersResponse.builder()

                    .success(true)

                    .message("Instructors fetched successfully")

                    .timestamp(LocalDateTime.now().toString())

                    .data(AdminUsersResponse.DataInfo.builder()

                            .totalUsers(0)

                            .users(List.of())

                            .build())

                    .build();

        }



        List<AdminUsersResponse.UserInfo> instructorList = instructors.stream()

                .map(user -> AdminUsersResponse.UserInfo.builder()

                        .id(user.getId())

                        .email(user.getEmail())

                        .role(user.getRole())

                        .status(user.getStatus())

                        .createdAt(user.getCreatedAt())

                        .build())

                .toList();



        return AdminUsersResponse.builder()

                .success(true)

                .message("Instructors fetched successfully")

                .timestamp(LocalDateTime.now().toString())

                .data(AdminUsersResponse.DataInfo.builder()

                        .totalUsers(instructorList.size())

                        .users(instructorList)

                        .build())

                .build();

    }



        public AdminInstructorApplicationsResponse getAllInstructorApplicationsDetailed(String authorization) {

                List<AdminUserServiceClient.InstructorApplicationDTO> applications = userClient.getAllInstructorApplications(authorization);

        

        if (applications.isEmpty()) {

            return AdminInstructorApplicationsResponse.builder()

                    .success(false)

                    .message("Failed to fetch instructor applications")

                    .timestamp(LocalDateTime.now().toString())

                    .build();

        }



        AdminInstructorApplicationsResponse.InstructorApplicationDetail[] detailsArray = applications.stream()

                .map(this::convertToApplicationDetail)

                .toArray(AdminInstructorApplicationsResponse.InstructorApplicationDetail[]::new);



        return AdminInstructorApplicationsResponse.builder()

                .success(true)

                .message("Applications fetched successfully")

                .data(java.util.Arrays.asList(detailsArray))

                .timestamp(LocalDateTime.now().toString())

                .build();

    }

    public AdminApproveInstructorResponse approveInstructorApplicationByUserId(UUID userId) {

        AdminUserServiceClient.InstructorApplicationDTO application = userClient.approveInstructorApplication(userId);

        

        if (application == null) {

            return AdminApproveInstructorResponse.builder()

                    .success(false)

                    .message("Failed to approve instructor application")

                    .timestamp(LocalDateTime.now().toString())

                    .build();

        }



        AdminApproveInstructorResponse.ApprovedApplicationDetail detail = convertToApprovedDetail(application);

        return AdminApproveInstructorResponse.builder()

                .success(true)

                .message("Instructor application approved successfully")

                .data(detail)

                .timestamp(LocalDateTime.now().toString())

                .build();

    }

    public AdminApproveInstructorResponse approveInstructorApplicationByUserId(UUID userId, String authorizationHeader) {

        AdminUserServiceClient.InstructorApplicationDTO application = userClient.approveInstructorApplication(userId, authorizationHeader);

        

        if (application == null) {

            return AdminApproveInstructorResponse.builder()

                    .success(false)

                    .message("Failed to approve instructor application")

                    .timestamp(LocalDateTime.now().toString())

                    .build();

        }



        AdminApproveInstructorResponse.ApprovedApplicationDetail detail = convertToApprovedDetail(application);

        return AdminApproveInstructorResponse.builder()

                .success(true)

                .message("Instructor application approved successfully")

                .data(detail)

                .timestamp(LocalDateTime.now().toString())

                .build();

    }



    public AdminApproveInstructorResponse rejectInstructorApplicationByUserId(UUID userId) {

        AdminUserServiceClient.InstructorApplicationDTO application = userClient.rejectInstructorApplication(userId);

        

        if (application == null) {

            return AdminApproveInstructorResponse.builder()

                    .success(false)

                    .message("Failed to reject instructor application")

                    .timestamp(LocalDateTime.now().toString())

                    .build();

        }



        AdminApproveInstructorResponse.ApprovedApplicationDetail detail = convertToApprovedDetail(application);

        return AdminApproveInstructorResponse.builder()

                .success(true)

                .message("Instructor application rejected successfully")

                .data(detail)

                .timestamp(LocalDateTime.now().toString())

                .build();

    }

    public AdminApproveInstructorResponse rejectInstructorApplicationByUserId(UUID userId, String authorizationHeader) {

        AdminUserServiceClient.InstructorApplicationDTO application = userClient.rejectInstructorApplication(userId, authorizationHeader);



        if (application == null) {

            return AdminApproveInstructorResponse.builder()

                    .success(false)

                    .message("Failed to reject instructor application")

                    .timestamp(LocalDateTime.now().toString())

                    .build();

        }

        AdminApproveInstructorResponse.ApprovedApplicationDetail detail = convertToApprovedDetail(application);

        return AdminApproveInstructorResponse.builder()

                .success(true)

                .message("Instructor application rejected successfully")

                .data(detail)

                .timestamp(LocalDateTime.now().toString())

                .build();

    }



    public AdminDeleteUserResponse deleteInstructor(UUID id) {

        boolean deleted = userClient.deleteUser(id);

        

        if (!deleted) {

            return new AdminDeleteUserResponse(

                    false,

                    "Failed to delete instructor",

                    null,

                    LocalDateTime.now()

            );

        }



        return new AdminDeleteUserResponse(

                true,

                "Instructor deleted successfully",

                null,

                LocalDateTime.now()

        );

    }



    private UserProfileResponse convertToProfileResponse(AdminUserServiceClient.UserDTO user) {
        // Initialize response object
        UserProfileResponse profile = new UserProfileResponse();

        // Populate basic user fields
        profile.setUserId(user.getId());
        profile.setEmail(user.getEmail());
        profile.setRole(user.getRole());
        profile.setStatus(user.getStatus());
        profile.setCreatedAt(user.getCreatedAt());
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setMobile(user.getMobileNumber());
        profile.setProfilePhoto(user.getProfilePhoto());

        // Fetch enrollment details for the user
        java.util.List<com.example.admin.dto.EnrollmentInfoDTO> enrollments =
                courseClient.getEnrollmentsByUserId(user.getId());
        profile.setEnrollments(enrollments);
        profile.setEnrollmentCount(enrollments != null ? enrollments.size() : 0);

        return profile;
    }









    private AdminInstructorApplicationsResponse.InstructorApplicationDetail convertToApplicationDetail(AdminUserServiceClient.InstructorApplicationDTO dto) {

        AdminInstructorApplicationsResponse.InstructorApplicationDetail detail = new AdminInstructorApplicationsResponse.InstructorApplicationDetail();

        

        AdminInstructorApplicationsResponse.ApplicationInfo application = AdminInstructorApplicationsResponse.ApplicationInfo.builder()

                .applicationId(dto.getUserId())

                .status(dto.getStatus())

                .submittedAt(dto.getAppliedAt())

                .build();

        detail.setApplication(application);

        

        AdminInstructorApplicationsResponse.UserInfo user = AdminInstructorApplicationsResponse.UserInfo.builder()

                .userId(dto.getUserId())

                .email(dto.getEmail())

                .currentRole("USER")

                .appliedRole("INSTRUCTOR")

                .accountStatus("ACTIVE")

                .isInstructorApproved(false)

                .build();

        detail.setUser(user);

        

        Map<String, Boolean> requiredDocs = new java.util.HashMap<>();

        requiredDocs.put("resumeUrl", dto.getResumeUrl() != null);

        requiredDocs.put("educationalCertificatesUrl", dto.getEducationalCertificatesUrl() != null);

        requiredDocs.put("governmentIdProofUrl", dto.getGovernmentIdProofUrl() != null);

        requiredDocs.put("experienceLetterUrl", dto.getExperienceLetterUrl() != null);

        requiredDocs.put("internshipCertificateUrl", dto.getInternshipCertificateUrl() != null);

        requiredDocs.put("skillCertificatesUrl", dto.getSkillCertificatesUrl() != null);

        requiredDocs.put("portfolioUrl", dto.getPortfolioUrl() != null);

        requiredDocs.put("demoLecturePptUrl", dto.getDemoLecturePptUrl() != null);

        requiredDocs.put("demoLectureRecordingUrl", dto.getDemoLectureRecordingUrl() != null);

        requiredDocs.put("projectsUrl", dto.getProjectsUrl() != null);

        requiredDocs.put("passportPhotoUrl", dto.getPassportPhotoUrl() != null);

        requiredDocs.put("bankDetailsUrl", dto.getBankDetailsUrl() != null);

        requiredDocs.put("panDocumentUrl", dto.getPanDocumentUrl() != null);

        requiredDocs.put("applicationFormUrl", dto.getApplicationFormUrl() != null);

        

        Map<String, Boolean> optionalDocs = new java.util.HashMap<>();

        optionalDocs.put("bankAccountNumber", dto.getBankAccountNumber() != null);

        optionalDocs.put("bankIfsc", dto.getBankIfsc() != null);

        optionalDocs.put("bankName", dto.getBankName() != null);

        optionalDocs.put("panNumber", dto.getPanNumber() != null);

        optionalDocs.put("additionalNotes", dto.getAdditionalNotes() != null);

        

        AdminInstructorApplicationsResponse.DocumentsInfo documents = AdminInstructorApplicationsResponse.DocumentsInfo.builder()

                .required(requiredDocs)

                .optional(optionalDocs)

                .build();

        detail.setDocuments(documents);

        

        return detail;

    }



    private AdminApproveInstructorResponse.ApprovedApplicationDetail convertToApprovedDetail(AdminUserServiceClient.InstructorApplicationDTO dto) {

        AdminApproveInstructorResponse.ApprovedApplicationDetail detail = new AdminApproveInstructorResponse.ApprovedApplicationDetail();

        

        AdminApproveInstructorResponse.ApplicationInfo application = AdminApproveInstructorResponse.ApplicationInfo.builder()

                .applicationId(dto.getUserId())

                .status(dto.getStatus())

                .submittedAt(dto.getAppliedAt())

                .reviewedAt(java.time.LocalDateTime.now().toString())

                .build();

        detail.setApplication(application);

        

        AdminApproveInstructorResponse.UserInfo user = AdminApproveInstructorResponse.UserInfo.builder()

                .userId(dto.getUserId())

                .email(dto.getEmail())

                .currentRole("USER")

                .appliedRole("INSTRUCTOR")

                .accountStatus("ACTIVE")

                .isInstructorApproved(true)

                .build();

        detail.setUser(user);

        

        Map<String, Boolean> requiredDocs = new java.util.HashMap<>();

        requiredDocs.put("resumeUrl", dto.getResumeUrl() != null);

        requiredDocs.put("educationalCertificatesUrl", dto.getEducationalCertificatesUrl() != null);

        requiredDocs.put("governmentIdProofUrl", dto.getGovernmentIdProofUrl() != null);

        requiredDocs.put("experienceLetterUrl", dto.getExperienceLetterUrl() != null);

        requiredDocs.put("internshipCertificateUrl", dto.getInternshipCertificateUrl() != null);

        requiredDocs.put("skillCertificatesUrl", dto.getSkillCertificatesUrl() != null);

        requiredDocs.put("portfolioUrl", dto.getPortfolioUrl() != null);

        requiredDocs.put("demoLecturePptUrl", dto.getDemoLecturePptUrl() != null);

        requiredDocs.put("demoLectureRecordingUrl", dto.getDemoLectureRecordingUrl() != null);

        requiredDocs.put("projectsUrl", dto.getProjectsUrl() != null);

        requiredDocs.put("passportPhotoUrl", dto.getPassportPhotoUrl() != null);

        requiredDocs.put("bankDetailsUrl", dto.getBankDetailsUrl() != null);

        requiredDocs.put("panDocumentUrl", dto.getPanDocumentUrl() != null);

        requiredDocs.put("applicationFormUrl", dto.getApplicationFormUrl() != null);

        

        Map<String, Boolean> optionalDocs = new java.util.HashMap<>();

        optionalDocs.put("bankAccountNumber", dto.getBankAccountNumber() != null);

        optionalDocs.put("bankIfsc", dto.getBankIfsc() != null);

        optionalDocs.put("bankName", dto.getBankName() != null);

        optionalDocs.put("panNumber", dto.getPanNumber() != null);

        optionalDocs.put("additionalNotes", dto.getAdditionalNotes() != null);

        

        AdminApproveInstructorResponse.DocumentsInfo documents = AdminApproveInstructorResponse.DocumentsInfo.builder()
                .required(requiredDocs)
                .optional(optionalDocs)
                .build();

        detail.setDocuments(documents);
        return detail;
    }

    public AdminInstructorDetailResponse getInstructorDetailedById(String idStr, String authorization) {
        if (idStr == null || idStr.trim().isEmpty()) {
            return AdminInstructorDetailResponse.builder()
                    .success(false)
                    .message("Instructor ID must not be empty")
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        UUID userUuid = null;
        try {
            userUuid = UUID.fromString(idStr.trim());
        } catch (IllegalArgumentException ignored) {
            // Not a UUID, might be numeric or other format
        }

        AdminUserServiceClient.UserDTO userDto = null;
        if (userUuid != null) {
            userDto = userClient.getUserById(userUuid);
        }

        // If not found by direct ID, search in all instructors list
        if (userDto == null && authorization != null) {
            List<AdminUserServiceClient.UserDTO> allInstructors = userClient.getAllInstructors(authorization);
            userDto = allInstructors.stream()
                    .filter(u -> (u.getId() != null && u.getId().toString().equalsIgnoreCase(idStr.trim()))
                            || (u.getEmail() != null && u.getEmail().equalsIgnoreCase(idStr.trim())))
                    .findFirst()
                    .orElse(null);
        }

        if (userDto == null) {
            return AdminInstructorDetailResponse.builder()
                    .success(false)
                    .message("Instructor not found with ID: " + idStr)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        UUID finalUserId = userDto.getId();

        // 1. Build Instructor Profile
        AdminInstructorDetailResponse.InstructorProfile profile = AdminInstructorDetailResponse.InstructorProfile.builder()
                .userId(userDto.getId())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .email(userDto.getEmail())
                .mobile(userDto.getMobile())
                .profilePhoto(userDto.getProfilePhoto())
                .bio(userDto.getBio())
                .specialization(userDto.getSpecialization())
                .skills(userDto.getSkills())
                .highestQualification(userDto.getHighestQualification())
                .organization(userDto.getOrganization())
                .fieldOfStudy(userDto.getFieldOfStudy())
                .city(userDto.getCity())
                .state(userDto.getState())
                .country(userDto.getCountry())
                .preferredLanguage(userDto.getPreferredLanguage())
                .status(userDto.getStatus())
                .appliedRole(userDto.getAppliedRole())
                .isInstructorApproved(userDto.getIsInstructorApproved())
                .createdAt(userDto.getCreatedAt())
                .build();

        // 2. Fetch Dashboard & Course Analytics from instructor-service
        Map<String, Object> dashboardResponse = null;
        if (finalUserId != null) {
            dashboardResponse = instructorClient.getInstructorDashboard(finalUserId);
        }

        int totalCourses = 0;
        int publishedCourses = 0;
        int draftCourses = 0;
        int archivedCourses = 0;
        int totalStudents = 0;
        double totalRevenue = 0.0;
        double averageRating = 0.0;
        int totalReviews = 0;

        List<AdminInstructorDetailResponse.InstructorCourseData> courseDataList = new java.util.ArrayList<>();

        if (dashboardResponse != null && dashboardResponse.get("data") instanceof Map<?, ?> dataMap) {
            totalCourses = getIntValue(dataMap.get("totalCourses"));
            publishedCourses = getIntValue(dataMap.get("publishedCourses"));
            draftCourses = getIntValue(dataMap.get("draftCourses"));
            archivedCourses = getIntValue(dataMap.get("archivedCourses"));
            totalStudents = getIntValue(dataMap.get("totalStudents"));
            totalRevenue = getDoubleValue(dataMap.get("totalRevenue"));
            averageRating = getDoubleValue(dataMap.get("averageRating"));

            if (dataMap.get("courses") instanceof List<?> rawCourses) {
                for (Object cObj : rawCourses) {
                    if (cObj instanceof Map<?, ?> cMap) {
                        AdminInstructorDetailResponse.CourseContentSummary contentSummary = null;
                        if (cMap.get("contentSummary") instanceof Map<?, ?> csMap) {
                            contentSummary = AdminInstructorDetailResponse.CourseContentSummary.builder()
                                    .sections(getIntValue(csMap.get("sections")))
                                    .lectures(getIntValue(csMap.get("lectures")))
                                    .assignments(getIntValue(csMap.get("assignments")))
                                    .quizzes(getIntValue(csMap.get("quizzes")))
                                    .totalDurationMinutes(getIntValue(csMap.get("totalDurationMinutes")))
                                    .build();
                        }

                        AdminInstructorDetailResponse.InstructorCourseData cData = AdminInstructorDetailResponse.InstructorCourseData.builder()
                                .courseId(getLongValue(cMap.get("courseId")))
                                .title(getStringValue(cMap.get("title")))
                                .slug(getStringValue(cMap.get("slug")))
                                .status(getStringValue(cMap.get("status")))
                                .enrolledStudents(getIntValue(cMap.get("enrolledStudents")))
                                .revenue(getDoubleValue(cMap.get("revenue")))
                                .averageRating(getDoubleValue(cMap.get("averageRating")))
                                .completionRate(getDoubleValue(cMap.get("completionRate")))
                                .createdAt(getStringValue(cMap.get("createdAt")))
                                .publishedAt(getStringValue(cMap.get("publishedAt")))
                                .contentSummary(contentSummary)
                                .build();
                        courseDataList.add(cData);
                    }
                }
            }
        }

        // 3. Fallback/Enrichment from course-service if instructor-service had no courses
        if (courseDataList.isEmpty()) {
            List<AdminCourseServiceClient.CourseDTO> allCourses = courseClient.getAllCourses();
            List<AdminCourseServiceClient.CourseDTO> instructorCourses = allCourses.stream()
                    .filter(c -> c.getInstructorId() != null && finalUserId != null
                            && (c.getInstructorId().toString().equalsIgnoreCase(finalUserId.toString())
                                || String.valueOf(c.getInstructorId()).equalsIgnoreCase(idStr.trim())))
                    .toList();

            totalCourses = instructorCourses.size();
            for (AdminCourseServiceClient.CourseDTO c : instructorCourses) {
                boolean isPublished = "PUBLISHED".equalsIgnoreCase(c.getStatus()) || "ACTIVE".equalsIgnoreCase(c.getStatus());
                if (isPublished) publishedCourses++;
                else draftCourses++;

                courseDataList.add(AdminInstructorDetailResponse.InstructorCourseData.builder()
                        .courseId(c.getId())
                        .title(c.getTitle())
                        .subtitle(c.getSubtitle())
                        .description(c.getDescription())
                        .category(c.getCategory())
                        .level(c.getLevel())
                        .language(c.getLanguage())
                        .price(c.getPrice())
                        .thumbnail(c.getThumbnail())
                        .status(c.getStatus())
                        .slug(c.getSlug())
                        .enrolledStudents(0)
                        .revenue(0.0)
                        .averageRating(0.0)
                        .totalReviews(0)
                        .completionRate(0.0)
                        .build());
            }
        }

        // 4. Assemble Final Response
        AdminInstructorDetailResponse.InstructorMetrics metrics = AdminInstructorDetailResponse.InstructorMetrics.builder()
                .totalCourses(totalCourses)
                .publishedCourses(publishedCourses)
                .draftCourses(draftCourses)
                .archivedCourses(archivedCourses)
                .totalStudents(totalStudents)
                .totalRevenue(totalRevenue)
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .build();

        return AdminInstructorDetailResponse.builder()
                .success(true)
                .message("Instructor details fetched successfully")
                .timestamp(LocalDateTime.now().toString())
                .data(AdminInstructorDetailResponse.InstructorDetailData.builder()
                        .instructor(profile)
                        .metrics(metrics)
                        .courses(courseDataList)
                        .build())
                .build();
    }

    private int getIntValue(Object obj) {
        if (obj instanceof Number num) return num.intValue();
        if (obj instanceof String str) {
            try { return Integer.parseInt(str); } catch (Exception ignored) {}
        }
        return 0;
    }

    private long getLongValue(Object obj) {
        if (obj instanceof Number num) return num.longValue();
        if (obj instanceof String str) {
            try { return Long.parseLong(str); } catch (Exception ignored) {}
        }
        return 0L;
    }

    private double getDoubleValue(Object obj) {
        if (obj instanceof Number num) return num.doubleValue();
        if (obj instanceof String str) {
            try { return Double.parseDouble(str); } catch (Exception ignored) {}
        }
        return 0.0;
    }

    private String getStringValue(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }
}