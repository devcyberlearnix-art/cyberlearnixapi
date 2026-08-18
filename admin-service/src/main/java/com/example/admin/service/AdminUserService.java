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

}