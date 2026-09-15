package com.user.register.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.user.register.dto.InstructorApplyDetailedResponse;
import com.user.register.entity.InstructorApplication;
import com.user.register.entity.User;
import com.user.register.repository.InstructorApplicationRepository;
import com.user.register.repository.UserRepository;
import com.user.register.security.JwtUtil;
import com.user.register.util.BearerTokenResolver;
import com.user.register.util.SecurityUtils;

@Service
public class InstructorService {

    private final UserRepository userRepository;
    private final InstructorApplicationRepository applicationRepository;
    private final JwtUtil jwtUtil;
    private final DocumentStorageService documentStorageService;

    @Value("${app.encryption.key:1234567890123456}")
    private String encryptionKey;

    public InstructorService(UserRepository userRepository,
                             InstructorApplicationRepository applicationRepository,
                             JwtUtil jwtUtil,
                             DocumentStorageService documentStorageService) {
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.jwtUtil = jwtUtil;
        this.documentStorageService = documentStorageService;
    }

    public InstructorApplyDetailedResponse applyForInstructor(
            String authorization,
            MultipartFile resume,
            MultipartFile educationalCertificates,
            MultipartFile governmentIdProof,
            MultipartFile experienceLetter,
            MultipartFile internshipCertificate,
            MultipartFile skillCertificates,
            MultipartFile portfolio,
            MultipartFile demoLecturePpt,
            MultipartFile demoLectureRecording,
            MultipartFile projects,
            MultipartFile passportPhoto,
            MultipartFile bankDetails,
            MultipartFile panDocument,
            MultipartFile applicationForm,
            String bankAccountNumber,
            String bankIfsc,
            String bankName,
            String panNumber,
            String additionalNotes
    ) {
        String tokenRole = BearerTokenResolver.resolveTokenRole(authorization, jwtUtil);
        UUID userId = BearerTokenResolver.resolveUserAccessToken(authorization, jwtUtil, userRepository);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() == User.Role.MAIN_ADMIN || user.getRole() == User.Role.SUB_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin accounts cannot apply for instructor role");
        }

        // Check if user has a pending application
        if (applicationRepository.existsByUserIdAndStatus(userId, InstructorApplication.ApplicationStatus.PENDING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An instructor application is already pending review");
        }

        // Check if user has an approved application (prevent re-application by approved instructors)
        if (applicationRepository.existsByUserIdAndStatus(userId, InstructorApplication.ApplicationStatus.APPROVED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have an approved instructor application");
        }

        // Allow re-application after rejection, but reset user state
        if (Boolean.TRUE.equals(user.getIsInstructorApproved()) && user.getRole() == User.Role.INSTRUCTOR) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already an approved instructor and cannot re-apply");
        }

        validateRequiredFile(resume, "resume");
        validateRequiredFile(educationalCertificates, "educationalCertificates");
        validateRequiredFile(governmentIdProof, "governmentIdProof");
        validateRequiredFile(passportPhoto, "passportPhoto");
        validateRequiredFile(bankDetails, "bankDetails");
        validateRequiredFile(panDocument, "panDocument");

        try {
            InstructorApplication application = InstructorApplication.builder()
                    .userId(userId)
                    .status(InstructorApplication.ApplicationStatus.PENDING)
                    .resumePath(documentStorageService.store(userId, "resume", resume))
                    .educationalCertificatesPath(documentStorageService.store(userId, "education", educationalCertificates))
                    .governmentIdProofPath(documentStorageService.store(userId, "gov_id", governmentIdProof))
                    .experienceLetterPath(documentStorageService.store(userId, "experience", experienceLetter))
                    .internshipCertificatePath(documentStorageService.store(userId, "internship", internshipCertificate))
                    .skillCertificatesPath(documentStorageService.store(userId, "skills", skillCertificates))
                    .portfolioPath(documentStorageService.store(userId, "portfolio", portfolio))
                    .demoLecturePptPath(documentStorageService.store(userId, "demo_ppt", demoLecturePpt))
                    .demoLectureRecordingPath(documentStorageService.store(userId, "demo_recording", demoLectureRecording))
                    .projectsPath(documentStorageService.store(userId, "projects", projects))
                    .passportPhotoPath(documentStorageService.store(userId, "passport_photo", passportPhoto))
                    .bankDetailsPath(documentStorageService.store(userId, "bank_details", bankDetails))
                    .panDocumentPath(documentStorageService.store(userId, "pan", panDocument))
                    .applicationFormPath(documentStorageService.store(userId, "application_form", applicationForm))
                    .bankAccountNumber(bankAccountNumber)
                    .bankIfsc(bankIfsc)
                    .bankName(bankName)
                    .panNumber(panNumber)
                    .additionalNotes(additionalNotes)
                    .submittedAt(LocalDateTime.now())
                    .build();

            applicationRepository.save(application);

            // Reset user state for new application
            user.setAppliedRole(User.Role.INSTRUCTOR);
            user.setApplicationStatus(User.ApplicationStatus.PENDING_VERIFICATION);
            // Don't reset isInstructorApproved here - it should only be set during approval/rejection
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            return buildResponse(user, application);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to store application documents: " + e.getMessage());
        }
    }

    public InstructorApplyDetailedResponse getApplicationStatus(String authorization) {
        String tokenRole = BearerTokenResolver.resolveTokenRole(authorization, jwtUtil);
        UUID userId = BearerTokenResolver.resolveUserAccessToken(authorization, jwtUtil, userRepository);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        InstructorApplication application = applicationRepository
                .findTopByUserIdOrderBySubmittedAtDesc(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No instructor application found"));

        return buildResponse(user, application);
    }

    public List<InstructorApplyDetailedResponse> getAllApplications() {
        List<InstructorApplication> applications = applicationRepository.findAll();

        return applications.stream().map(application -> {
            User user = userRepository.findById(application.getUserId()).orElse(null);
            if (user == null) {
                user = new User();
                user.setId(application.getUserId());
            }
            return buildResponse(user, application);
        }).toList();
    }

    public Map<String, Object> getAllApplicationsPaginated(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("submittedAt").descending());
        org.springframework.data.domain.Page<InstructorApplication> applicationPage = applicationRepository.findAll(pageable);

        List<InstructorApplyDetailedResponse> applications = applicationPage.stream().map(application -> {
            User user = userRepository.findById(application.getUserId()).orElse(null);
            if (user == null) {
                user = new User();
                user.setId(application.getUserId());
            }
            return buildResponse(user, application);
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("applications", applications);
        response.put("currentPage", applicationPage.getNumber());
        response.put("totalPages", applicationPage.getTotalPages());
        response.put("totalApplications", applicationPage.getTotalElements());
        response.put("pageSize", applicationPage.getSize());

        return response;
    }

    public Map<String, Object> getApplicationsByStatusPaginated(InstructorApplication.ApplicationStatus status, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("submittedAt").descending());
        org.springframework.data.domain.Page<InstructorApplication> applicationPage = applicationRepository.findByStatus(status, pageable);

        List<InstructorApplyDetailedResponse> applications = applicationPage.stream().map(application -> {
            User user = userRepository.findById(application.getUserId()).orElse(null);
            if (user == null) {
                user = new User();
                user.setId(application.getUserId());
            }
            return buildResponse(user, application);
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("applications", applications);
        response.put("currentPage", applicationPage.getNumber());
        response.put("totalPages", applicationPage.getTotalPages());
        response.put("totalApplications", applicationPage.getTotalElements());
        response.put("pageSize", applicationPage.getSize());
        response.put("status", status.name());

        return response;
    }

    public InstructorApplyDetailedResponse approveApplicationById(UUID applicationId, UUID adminId) {
        InstructorApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Instructor application not found with ID: " + applicationId));

        User user = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + application.getUserId()));

        // Only check the current application's status, not user history
        if (application.getStatus() == InstructorApplication.ApplicationStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Application is already approved");
        }

        if (application.getStatus() == InstructorApplication.ApplicationStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This application has already been rejected. User must submit a new application.");
        }

        // Check if user is already an approved instructor (from a different application)
        // Find all approved applications for this user, excluding the current one
        List<InstructorApplication> userApprovedApps = applicationRepository.findByStatus(
            InstructorApplication.ApplicationStatus.APPROVED,
            org.springframework.data.domain.PageRequest.of(0, 100)
        ).getContent().stream()
            .filter(app -> app.getUserId().equals(user.getId()) && !app.getId().equals(applicationId))
            .toList();

        if (!userApprovedApps.isEmpty() || (Boolean.TRUE.equals(user.getIsInstructorApproved()) && user.getRole() == User.Role.INSTRUCTOR)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "User is already an approved instructor. Cannot approve another application.");
        }

        // Approve the application
        application.setStatus(InstructorApplication.ApplicationStatus.APPROVED);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewedBy(adminId.toString());
        applicationRepository.save(application);

        // Update user: set role to INSTRUCTOR, mark as approved
        user.setRole(User.Role.INSTRUCTOR);
        user.setIsInstructorApproved(true);
        user.setApplicationStatus(User.ApplicationStatus.APPROVED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return buildResponse(user, application);
    }
    public InstructorApplyDetailedResponse rejectApplicationById(UUID applicationId, UUID adminId) {
        InstructorApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Instructor application not found with ID: " + applicationId));

        User user = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + application.getUserId()));

        if (application.getStatus() == InstructorApplication.ApplicationStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Application is already rejected");
        }

        // Reject the application
        application.setStatus(InstructorApplication.ApplicationStatus.REJECTED);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewedBy(adminId.toString());
        applicationRepository.save(application);

        // Update user state
        user.setIsInstructorApproved(false);
        user.setApplicationStatus(User.ApplicationStatus.REJECTED);

        // Only demote user if this was their active approved application
        // Check if user has any other approved applications
        boolean hasOtherApprovedApps = applicationRepository.existsByUserIdAndStatus(
            user.getId(), InstructorApplication.ApplicationStatus.APPROVED);

        // Only demote if no other approved applications exist
        if (user.getRole() == User.Role.INSTRUCTOR && !hasOtherApprovedApps) {
            user.setRole(User.Role.STUDENT);
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return buildResponse(user, application);
    }


    private InstructorApplyDetailedResponse buildResponse(User user, InstructorApplication application) {

        // Application info
        String reviewMessage = switch (application.getStatus()) {
            case PENDING -> "Your application is under admin review.";
            case APPROVED -> "Approved. You can switch to INSTRUCTOR role using POST /auth/switch-role.";
            case REJECTED -> "Application rejected. Contact support or re-apply after updating documents.";
        };

        InstructorApplyDetailedResponse.ApplicationInfo applicationInfo =
                InstructorApplyDetailedResponse.ApplicationInfo.builder()
                        .applicationId(application.getId())
                        .status(application.getStatus().name())
                        .reviewMessage(reviewMessage)
                        .submittedAt(application.getSubmittedAt())
                        .build();

        // User info
        InstructorApplyDetailedResponse.UserInfo userInfo = InstructorApplyDetailedResponse.UserInfo.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .currentRole(user.getRole() != null ? user.getRole().name() : null)
                .appliedRole(user.getAppliedRole() != null ? user.getAppliedRole().name() : "INSTRUCTOR")
                .accountStatus(user.getStatus() != null ? user.getStatus().name() : null)
                .isInstructorApproved(user.getIsInstructorApproved())
                .build();

        // Documents - required
        Map<String, Boolean> requiredDocs = new LinkedHashMap<>();
        requiredDocs.put("resume", application.getResumePath() != null);
        requiredDocs.put("educationalCertificates", application.getEducationalCertificatesPath() != null);
        requiredDocs.put("governmentIdProof", application.getGovernmentIdProofPath() != null);
        requiredDocs.put("passportPhoto", application.getPassportPhotoPath() != null);
        requiredDocs.put("bankDetails", application.getBankDetailsPath() != null);
        requiredDocs.put("panDocument", application.getPanDocumentPath() != null);

        // Documents - optional
        Map<String, Boolean> optionalDocs = new LinkedHashMap<>();
        optionalDocs.put("portfolio", application.getPortfolioPath() != null);
        optionalDocs.put("experienceLetter", application.getExperienceLetterPath() != null);
        optionalDocs.put("internshipCertificate", application.getInternshipCertificatePath() != null);
        optionalDocs.put("skillCertificates", application.getSkillCertificatesPath() != null);
        optionalDocs.put("demoLecturePpt", application.getDemoLecturePptPath() != null);
        optionalDocs.put("demoLectureRecording", application.getDemoLectureRecordingPath() != null);
        optionalDocs.put("projects", application.getProjectsPath() != null);
        optionalDocs.put("applicationForm", application.getApplicationFormPath() != null);

        InstructorApplyDetailedResponse.DocumentsInfo documentsInfo =
                InstructorApplyDetailedResponse.DocumentsInfo.builder()
                        .required(requiredDocs)
                        .optional(optionalDocs)
                        .build();

        // Next steps
        List<String> nextSteps = List.of(
                "Admin will review your application and documents",
                "You will be notified once approved",
                "After approval, switch role using POST /auth/switch-role with {\"switchRole\":\"INSTRUCTOR\"}",
                "Track status via GET /instructors/application/status"
        );

        return InstructorApplyDetailedResponse.builder()
                .application(applicationInfo)
                .user(userInfo)
                .documents(documentsInfo)
                .nextSteps(nextSteps)
                .build();
    }

    private void validateRequiredFile(MultipartFile file, String name) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required document missing: " + name);
        }
    }

    private String decrypt(String value) {
        if (value == null) return null;
        try {
            return SecurityUtils.decrypt(value, encryptionKey);
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * Validates and fixes user-application state consistency
     * This ensures that user state matches their latest application status
     * Can be called manually to fix inconsistent states
     */
    public void validateAndFixUserState(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Get the latest application for this user
        InstructorApplication latestApplication = applicationRepository
            .findTopByUserIdOrderBySubmittedAtDesc(userId)
            .orElse(null);

        if (latestApplication == null) {
            // No applications found, reset user to default state
            if (user.getRole() == User.Role.INSTRUCTOR) {
                user.setRole(User.Role.STUDENT);
            }
            user.setIsInstructorApproved(false);
            user.setApplicationStatus(null);
            userRepository.save(user);
            return;
        }

        // Ensure user's isInstructorApproved matches latest application status
        boolean shouldBeApproved = latestApplication.getStatus() == InstructorApplication.ApplicationStatus.APPROVED;

        if (Boolean.TRUE.equals(user.getIsInstructorApproved()) != shouldBeApproved) {
            user.setIsInstructorApproved(shouldBeApproved);
        }

        // Ensure user's applicationStatus matches latest application status
        User.ApplicationStatus expectedStatus = switch (latestApplication.getStatus()) {
            case PENDING -> User.ApplicationStatus.PENDING_VERIFICATION;
            case APPROVED -> User.ApplicationStatus.APPROVED;
            case REJECTED -> User.ApplicationStatus.REJECTED;
        };

        if (user.getApplicationStatus() != expectedStatus) {
            user.setApplicationStatus(expectedStatus);
        }

        // Ensure user's role matches approval status
        if (shouldBeApproved && user.getRole() != User.Role.INSTRUCTOR) {
            user.setRole(User.Role.INSTRUCTOR);
        } else if (!shouldBeApproved && user.getRole() == User.Role.INSTRUCTOR) {
            // Check if user has any other approved applications before demoting
            boolean hasOtherApprovedApps = applicationRepository.existsByUserIdAndStatus(
                user.getId(), InstructorApplication.ApplicationStatus.APPROVED);
            if (!hasOtherApprovedApps) {
                user.setRole(User.Role.STUDENT);
            }
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
