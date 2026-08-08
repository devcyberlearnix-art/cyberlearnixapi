package com.user.register.service;

import java.time.LocalDateTime;
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

        if (applicationRepository.existsByUserIdAndStatus(userId, InstructorApplication.ApplicationStatus.PENDING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An instructor application is already pending review");
        }

        if (Boolean.TRUE.equals(user.getIsInstructorApproved()) && user.getRole() == User.Role.INSTRUCTOR) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already an approved instructor");
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

            user.setAppliedRole(User.Role.INSTRUCTOR);
            user.setApplicationStatus(User.ApplicationStatus.PENDING_VERIFICATION);
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

    public InstructorApplyDetailedResponse approveApplicationByUserId(UUID userId, UUID adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));

        InstructorApplication application = applicationRepository
                .findTopByUserIdOrderBySubmittedAtDesc(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No instructor application found for user ID: " + userId));

        if (application.getStatus() == InstructorApplication.ApplicationStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Application is already approved");
        }

        if (application.getStatus() == InstructorApplication.ApplicationStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Application was previously rejected. User must re-apply.");
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
    public InstructorApplyDetailedResponse rejectApplicationByUserId(UUID userId, UUID adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));

        InstructorApplication application = applicationRepository
                .findTopByUserIdOrderBySubmittedAtDesc(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No instructor application found for user ID: " + userId));

        if (application.getStatus() == InstructorApplication.ApplicationStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Application is already rejected");
        }

        // Reject the application
        application.setStatus(InstructorApplication.ApplicationStatus.REJECTED);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewedBy(adminId.toString());
        applicationRepository.save(application);

        // Update user
        user.setIsInstructorApproved(false);
        user.setApplicationStatus(User.ApplicationStatus.REJECTED);
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
}
