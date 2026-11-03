package com.user.register.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.user.register.dto.InstructorApplyDetailedResponse;
import com.user.register.dto.InstructorApplyRequest;
import com.user.register.entity.InstructorApplication;
import com.user.register.entity.User;
import com.user.register.repository.InstructorApplicationRepository;
import com.user.register.repository.UserRepository;
import com.user.register.security.JwtUtil;
import com.user.register.util.BearerTokenResolver;
import com.user.register.util.SecurityUtils;

@Slf4j
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

    public List<Map<String, Object>> getTeachingSubjects() {
        return List.of(
                Map.of("id", "web-development", "name", "Web Development", "category", "Development", "description", "Full-Stack, React, Node.js, Spring Boot & APIs"),
                Map.of("id", "data-science-ai", "name", "Data Science & AI", "category", "AI & Data", "description", "Machine Learning, Deep Learning, Big Data & Python"),
                Map.of("id", "cybersecurity", "name", "Cybersecurity & InfoSec", "category", "Security", "description", "Ethical Hacking, Network Security, SOC & Pentesting"),
                Map.of("id", "cloud-devops", "name", "Cloud Computing & DevOps", "category", "Cloud Infrastructure", "description", "AWS, Azure, Docker, Kubernetes & CI/CD Pipelines"),
                Map.of("id", "mobile-app-dev", "name", "Mobile App Development", "category", "Development", "description", "Android (Kotlin), iOS (Swift), React Native & Flutter"),
                Map.of("id", "ui-ux-design", "name", "UI/UX & Product Design", "category", "Design", "description", "Figma, User Research, Wireframing & Design Systems"),
                Map.of("id", "business-finance", "name", "Business & Finance", "category", "Business", "description", "Financial Modeling, Product Management, Growth & Analytics"),
                Map.of("id", "software-architecture", "name", "Software Architecture", "category", "Architecture", "description", "System Design, Microservices, Event-Driven & Scalability")
        );
    }

    @Transactional
    public InstructorApplyDetailedResponse saveDraft(String authorization, InstructorApplyRequest draftRequest) {
        UUID userId = BearerTokenResolver.resolveUserAccessToken(authorization, jwtUtil, userRepository);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() == User.Role.MAIN_ADMIN || user.getRole() == User.Role.SUB_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin accounts cannot apply for instructor role");
        }

        // Find existing draft or pending application
        InstructorApplication application = applicationRepository
                .findTopByUserIdOrderBySubmittedAtDesc(userId)
                .filter(a -> a.getStatus() == InstructorApplication.ApplicationStatus.DRAFT || a.getStatus() == InstructorApplication.ApplicationStatus.NEED_MORE_INFO)
                .orElse(null);

        if (application == null) {
            // Check if already pending or approved
            if (applicationRepository.existsByUserIdAndStatus(userId, InstructorApplication.ApplicationStatus.PENDING)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have an application pending review.");
            }
            if (applicationRepository.existsByUserIdAndStatus(userId, InstructorApplication.ApplicationStatus.APPROVED)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already an approved instructor.");
            }

            application = InstructorApplication.builder()
                    .userId(userId)
                    .status(InstructorApplication.ApplicationStatus.DRAFT)
                    .submittedAt(LocalDateTime.now())
                    .build();
        }

        if (draftRequest != null) {
            if (draftRequest.getSubject() != null) application.setSubject(draftRequest.getSubject());
            if (draftRequest.getSpecialization() != null) application.setSpecialization(draftRequest.getSpecialization());
            if (draftRequest.getExperience() != null) application.setExperience(draftRequest.getExperience());
            if (draftRequest.getBio() != null) application.setBio(draftRequest.getBio());
            if (draftRequest.getPhone() != null) application.setPhone(draftRequest.getPhone());
            if (draftRequest.getLinkedinUrl() != null) application.setLinkedinUrl(draftRequest.getLinkedinUrl());
            if (draftRequest.getWebsiteUrl() != null) application.setWebsiteUrl(draftRequest.getWebsiteUrl());
            if (draftRequest.getBankAccountNumber() != null) application.setBankAccountNumber(draftRequest.getBankAccountNumber());
            if (draftRequest.getBankIfsc() != null) application.setBankIfsc(draftRequest.getBankIfsc());
            if (draftRequest.getBankName() != null) application.setBankName(draftRequest.getBankName());
            if (draftRequest.getPanNumber() != null) application.setPanNumber(draftRequest.getPanNumber());
            if (draftRequest.getAdditionalNotes() != null) application.setAdditionalNotes(draftRequest.getAdditionalNotes());
        }

        applicationRepository.save(application);
        log.info("Saved instructor application draft for user: {}", userId);

        return buildResponse(user, application);
    }

    @Transactional
    public InstructorApplyDetailedResponse uploadSingleDocument(String authorization, String docType, MultipartFile file) {
        UUID userId = BearerTokenResolver.resolveUserAccessToken(authorization, jwtUtil, userRepository);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }

        InstructorApplication application = applicationRepository
                .findTopByUserIdOrderBySubmittedAtDesc(userId)
                .filter(a -> a.getStatus() == InstructorApplication.ApplicationStatus.DRAFT || a.getStatus() == InstructorApplication.ApplicationStatus.NEED_MORE_INFO)
                .orElseGet(() -> {
                    InstructorApplication newDraft = InstructorApplication.builder()
                            .userId(userId)
                            .status(InstructorApplication.ApplicationStatus.DRAFT)
                            .submittedAt(LocalDateTime.now())
                            .build();
                    return applicationRepository.save(newDraft);
                });

        try {
            String storedPath = documentStorageService.store(userId, docType, file);
            setDocumentPath(application, docType, storedPath);
            applicationRepository.save(application);
            return buildResponse(user, application);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload document: " + e.getMessage());
        }
    }

    @Transactional
    public InstructorApplyDetailedResponse removeDocument(String authorization, String docType) {
        UUID userId = BearerTokenResolver.resolveUserAccessToken(authorization, jwtUtil, userRepository);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        InstructorApplication application = applicationRepository
                .findTopByUserIdOrderBySubmittedAtDesc(userId)
                .filter(a -> a.getStatus() == InstructorApplication.ApplicationStatus.DRAFT || a.getStatus() == InstructorApplication.ApplicationStatus.NEED_MORE_INFO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No editable draft found to remove document"));

        setDocumentPath(application, docType, null);
        applicationRepository.save(application);

        return buildResponse(user, application);
    }

    @Transactional
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
            String subject,
            String specialization,
            String experience,
            String bio,
            String phone,
            String linkedinUrl,
            String websiteUrl,
            String bankAccountNumber,
            String bankIfsc,
            String bankName,
            String panNumber,
            String additionalNotes
    ) {
        UUID userId = BearerTokenResolver.resolveUserAccessToken(authorization, jwtUtil, userRepository);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() == User.Role.MAIN_ADMIN || user.getRole() == User.Role.SUB_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin accounts cannot apply for instructor role");
        }

        // Check if user has an active pending application
        if (applicationRepository.existsByUserIdAndStatus(userId, InstructorApplication.ApplicationStatus.PENDING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An instructor application is already pending review");
        }

        // Check if already approved instructor
        if (applicationRepository.existsByUserIdAndStatus(userId, InstructorApplication.ApplicationStatus.APPROVED) ||
                (Boolean.TRUE.equals(user.getIsInstructorApproved()) && user.getRole() == User.Role.INSTRUCTOR)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already an approved instructor and cannot re-apply");
        }

        // Look for existing draft or resubmission
        InstructorApplication application = applicationRepository
                .findTopByUserIdOrderBySubmittedAtDesc(userId)
                .filter(a -> a.getStatus() == InstructorApplication.ApplicationStatus.DRAFT ||
                             a.getStatus() == InstructorApplication.ApplicationStatus.NEED_MORE_INFO ||
                             a.getStatus() == InstructorApplication.ApplicationStatus.REJECTED)
                .orElse(null);

        if (application == null) {
            application = InstructorApplication.builder()
                    .userId(userId)
                    .build();
        } else {
            if (application.getStatus() == InstructorApplication.ApplicationStatus.NEED_MORE_INFO) {
                log.info("Applicant {} is resubmitting application {} following NEED_MORE_INFO request", userId, application.getId());
                application.setRequestedInfo(null);
            } else if (application.getStatus() == InstructorApplication.ApplicationStatus.REJECTED) {
                log.info("Applicant {} is submitting revised application {} following previous rejection", userId, application.getId());
                application.setRejectionReason(null);
            }
        }

        // Fallbacks from existing draft if not provided in multipart body
        if (subject != null && !subject.isBlank()) application.setSubject(subject);
        if (specialization != null && !specialization.isBlank()) application.setSpecialization(specialization);
        if (experience != null && !experience.isBlank()) application.setExperience(experience);
        if (bio != null && !bio.isBlank()) application.setBio(bio);
        if (phone != null && !phone.isBlank()) application.setPhone(phone);
        if (linkedinUrl != null && !linkedinUrl.isBlank()) application.setLinkedinUrl(linkedinUrl);
        if (websiteUrl != null && !websiteUrl.isBlank()) application.setWebsiteUrl(websiteUrl);

        if (bankAccountNumber != null) application.setBankAccountNumber(bankAccountNumber);
        if (bankIfsc != null) application.setBankIfsc(bankIfsc);
        if (bankName != null) application.setBankName(bankName);
        if (panNumber != null) application.setPanNumber(panNumber);
        if (additionalNotes != null) application.setAdditionalNotes(additionalNotes);

        // Strict Step 1 Validations
        if (application.getSubject() == null || application.getSubject().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teaching Subject / Content Type is required");
        }
        if (application.getSpecialization() == null || application.getSpecialization().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Specialization is required");
        }
        if (application.getExperience() == null || application.getExperience().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Years of Experience is required");
        }
        if (application.getBio() == null || application.getBio().trim().length() < 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Professional bio must be at least 30 characters in length");
        }

        try {
            // Upload provided files or retain existing draft files
            if (resume != null && !resume.isEmpty()) {
                application.setResumePath(documentStorageService.store(userId, "resume", resume));
            }
            if (educationalCertificates != null && !educationalCertificates.isEmpty()) {
                application.setEducationalCertificatesPath(documentStorageService.store(userId, "education", educationalCertificates));
            }
            if (governmentIdProof != null && !governmentIdProof.isEmpty()) {
                application.setGovernmentIdProofPath(documentStorageService.store(userId, "gov_id", governmentIdProof));
            }
            if (passportPhoto != null && !passportPhoto.isEmpty()) {
                application.setPassportPhotoPath(documentStorageService.store(userId, "passport_photo", passportPhoto));
            }
            if (bankDetails != null && !bankDetails.isEmpty()) {
                application.setBankDetailsPath(documentStorageService.store(userId, "bank_details", bankDetails));
            }
            if (panDocument != null && !panDocument.isEmpty()) {
                application.setPanDocumentPath(documentStorageService.store(userId, "pan", panDocument));
            }

            // Optionals
            if (portfolio != null && !portfolio.isEmpty()) {
                application.setPortfolioPath(documentStorageService.store(userId, "portfolio", portfolio));
            }
            if (experienceLetter != null && !experienceLetter.isEmpty()) {
                application.setExperienceLetterPath(documentStorageService.store(userId, "experience", experienceLetter));
            }
            if (internshipCertificate != null && !internshipCertificate.isEmpty()) {
                application.setInternshipCertificatePath(documentStorageService.store(userId, "internship", internshipCertificate));
            }
            if (skillCertificates != null && !skillCertificates.isEmpty()) {
                application.setSkillCertificatesPath(documentStorageService.store(userId, "skills", skillCertificates));
            }
            if (demoLecturePpt != null && !demoLecturePpt.isEmpty()) {
                application.setDemoLecturePptPath(documentStorageService.store(userId, "demo_ppt", demoLecturePpt));
            }
            if (demoLectureRecording != null && !demoLectureRecording.isEmpty()) {
                application.setDemoLectureRecordingPath(documentStorageService.store(userId, "demo_recording", demoLectureRecording));
            }
            if (projects != null && !projects.isEmpty()) {
                application.setProjectsPath(documentStorageService.store(userId, "projects", projects));
            }
            if (applicationForm != null && !applicationForm.isEmpty()) {
                application.setApplicationFormPath(documentStorageService.store(userId, "application_form", applicationForm));
            }

            // Strict Step 2 Validations
            validateDocumentRequired(application.getResumePath(), "Resume / CV");
            validateDocumentRequired(application.getEducationalCertificatesPath(), "Educational Certificates");
            validateDocumentRequired(application.getGovernmentIdProofPath(), "Government ID Proof");
            validateDocumentRequired(application.getPassportPhotoPath(), "Passport Photo");
            validateDocumentRequired(application.getBankDetailsPath(), "Bank Details");
            validateDocumentRequired(application.getPanDocumentPath(), "PAN Document");

            // Final state promotion
            application.setStatus(InstructorApplication.ApplicationStatus.PENDING);
            application.setSubmittedAt(LocalDateTime.now());
            applicationRepository.save(application);

            user.setAppliedRole(User.Role.INSTRUCTOR);
            user.setApplicationStatus(User.ApplicationStatus.PENDING_VERIFICATION);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            log.info("Successfully submitted instructor application {} for user {}", application.getId(), userId);
            return buildResponse(user, application);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to submit application: " + e.getMessage());
        }
    }

    public InstructorApplyDetailedResponse getApplicationStatus(String authorization) {
        UUID userId = BearerTokenResolver.resolveUserAccessToken(authorization, jwtUtil, userRepository);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        InstructorApplication application = applicationRepository
                .findTopByUserIdOrderBySubmittedAtDesc(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No instructor application found"));

        return buildResponse(user, application);
    }

    public InstructorApplyDetailedResponse getApplicationById(UUID applicationId) {
        InstructorApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instructor application not found with ID: " + applicationId));

        User user = userRepository.findById(application.getUserId()).orElse(null);
        if (user == null) {
            user = new User();
            user.setId(application.getUserId());
        }

        return buildResponse(user, application);
    }

    public Map<String, Object> getAllApplicationsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<InstructorApplication> applicationPage = applicationRepository.findAll(pageable);

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
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<InstructorApplication> applicationPage = applicationRepository.findByStatus(status, pageable);

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

    @Transactional
    public InstructorApplyDetailedResponse approveApplicationById(UUID applicationId, UUID adminId) {
        InstructorApplication application = applicationRepository.findByIdForUpdate(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instructor application not found with ID: " + applicationId));

        User user = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + application.getUserId()));

        // Prevent self-approval
        if (adminId != null && adminId.equals(application.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Security violation: Administrators are strictly prohibited from approving their own instructor applications.");
        }

        // Idempotent return if already approved
        if (application.getStatus() == InstructorApplication.ApplicationStatus.APPROVED) {
            log.info("Application {} is already APPROVED. Returning idempotent response.", applicationId);
            return buildResponse(user, application);
        }

        // State Machine transition rules
        if (application.getStatus() == InstructorApplication.ApplicationStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot approve an unsubmitted DRAFT application.");
        }
        if (application.getStatus() == InstructorApplication.ApplicationStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot approve an application that was REJECTED. Applicant must resubmit.");
        }

        // Approve application
        application.setStatus(InstructorApplication.ApplicationStatus.APPROVED);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewedBy(adminId != null ? adminId.toString() : "ADMIN");
        applicationRepository.save(application);

        // Promote user role to INSTRUCTOR
        user.setRole(User.Role.INSTRUCTOR);
        user.setIsInstructorApproved(true);
        user.setApplicationStatus(User.ApplicationStatus.APPROVED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Admin {} approved instructor application {} for user {}", adminId, applicationId, user.getId());
        return buildResponse(user, application);
    }

    @Transactional
    public InstructorApplyDetailedResponse rejectApplicationById(UUID applicationId, UUID adminId, String reason) {
        InstructorApplication application = applicationRepository.findByIdForUpdate(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instructor application not found with ID: " + applicationId));

        User user = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + application.getUserId()));

        // Prevent self-rejection
        if (adminId != null && adminId.equals(application.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Security violation: Administrators cannot reject their own applications.");
        }

        // State Machine transition rules
        if (application.getStatus() == InstructorApplication.ApplicationStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot reject an application that is already APPROVED.");
        }
        if (application.getStatus() == InstructorApplication.ApplicationStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot reject an unsubmitted DRAFT application.");
        }

        if (reason == null || reason.trim().length() < 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A meaningful rejection reason of at least 5 characters is strictly mandatory.");
        }

        application.setStatus(InstructorApplication.ApplicationStatus.REJECTED);
        application.setRejectionReason(reason.trim());
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewedBy(adminId != null ? adminId.toString() : "ADMIN");
        applicationRepository.save(application);

        user.setIsInstructorApproved(false);
        user.setApplicationStatus(User.ApplicationStatus.REJECTED);
        if (user.getRole() == User.Role.INSTRUCTOR) {
            user.setRole(User.Role.STUDENT);
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Admin {} rejected instructor application {} with reason: {}", adminId, applicationId, reason);
        return buildResponse(user, application);
    }

    @Transactional
    public InstructorApplyDetailedResponse requestInfoById(UUID applicationId, UUID adminId, String requestedInfo) {
        InstructorApplication application = applicationRepository.findByIdForUpdate(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instructor application not found with ID: " + applicationId));

        User user = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + application.getUserId()));

        // Prevent self-request
        if (adminId != null && adminId.equals(application.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Security violation: Administrators cannot request info from themselves.");
        }

        // State Machine transition rules
        if (application.getStatus() == InstructorApplication.ApplicationStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot request additional info for an application that is already APPROVED.");
        }
        if (application.getStatus() == InstructorApplication.ApplicationStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot request additional info for an application that was REJECTED. Applicant must resubmit.");
        }
        if (application.getStatus() == InstructorApplication.ApplicationStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot request additional info on an unsubmitted DRAFT application.");
        }

        if (requestedInfo == null || requestedInfo.trim().length() < 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Specific details on requested information (minimum 5 characters) are mandatory.");
        }

        application.setStatus(InstructorApplication.ApplicationStatus.NEED_MORE_INFO);
        application.setRequestedInfo(requestedInfo.trim());
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewedBy(adminId != null ? adminId.toString() : "ADMIN");
        applicationRepository.save(application);

        return buildResponse(user, application);
    }

    public Resource getDocumentResource(UUID applicationId, String docType, UUID requestingUserId, String requestingRole) {
        InstructorApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        boolean isAdmin = requestingRole != null && (requestingRole.contains("ADMIN") || requestingRole.contains("MAIN_ADMIN") || requestingRole.contains("SUB_ADMIN"));
        boolean isOwner = requestingUserId != null && application.getUserId().equals(requestingUserId);

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Private document can only be viewed by applicant or authorized administrator.");
        }

        String pathStr = getDocumentPath(application, docType);
        if (pathStr == null || pathStr.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document " + docType + " not found for this application");
        }

        return documentStorageService.loadAsResource(pathStr);
    }

    private void validateDocumentRequired(String path, String docName) {
        if (path == null || path.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required document missing: " + docName);
        }
    }

    private void setDocumentPath(InstructorApplication app, String docType, String path) {
        switch (docType) {
            case "resume" -> app.setResumePath(path);
            case "educationalCertificates", "education" -> app.setEducationalCertificatesPath(path);
            case "governmentIdProof", "gov_id" -> app.setGovernmentIdProofPath(path);
            case "passportPhoto", "passport_photo" -> app.setPassportPhotoPath(path);
            case "bankDetails", "bank_details" -> app.setBankDetailsPath(path);
            case "panDocument", "pan" -> app.setPanDocumentPath(path);
            case "portfolio" -> app.setPortfolioPath(path);
            case "experienceLetter", "experience" -> app.setExperienceLetterPath(path);
            case "internshipCertificate", "internship" -> app.setInternshipCertificatePath(path);
            case "skillCertificates", "skills" -> app.setSkillCertificatesPath(path);
            case "demoLecturePpt", "demo_ppt" -> app.setDemoLecturePptPath(path);
            case "demoLectureRecording", "demo_recording" -> app.setDemoLectureRecordingPath(path);
            case "projects" -> app.setProjectsPath(path);
            case "applicationForm", "application_form" -> app.setApplicationFormPath(path);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown document type: " + docType);
        }
    }

    private String getDocumentPath(InstructorApplication app, String docType) {
        return switch (docType) {
            case "resume" -> app.getResumePath();
            case "educationalCertificates", "education" -> app.getEducationalCertificatesPath();
            case "governmentIdProof", "gov_id" -> app.getGovernmentIdProofPath();
            case "passportPhoto", "passport_photo" -> app.getPassportPhotoPath();
            case "bankDetails", "bank_details" -> app.getBankDetailsPath();
            case "panDocument", "pan" -> app.getPanDocumentPath();
            case "portfolio" -> app.getPortfolioPath();
            case "experienceLetter", "experience" -> app.getExperienceLetterPath();
            case "internshipCertificate", "internship" -> app.getInternshipCertificatePath();
            case "skillCertificates", "skills" -> app.getSkillCertificatesPath();
            case "demoLecturePpt", "demo_ppt" -> app.getDemoLecturePptPath();
            case "demoLectureRecording", "demo_recording" -> app.getDemoLectureRecordingPath();
            case "projects" -> app.getProjectsPath();
            case "applicationForm", "application_form" -> app.getApplicationFormPath();
            default -> null;
        };
    }

    private InstructorApplyDetailedResponse buildResponse(User user, InstructorApplication application) {
        String reviewMessage = switch (application.getStatus()) {
            case DRAFT -> "Application draft saved. Complete all steps and submit for review.";
            case PENDING, UNDER_REVIEW -> "Your application is under admin review.";
            case NEED_MORE_INFO -> "Additional information requested: " + (application.getRequestedInfo() != null ? application.getRequestedInfo() : "Please update your application.");
            case APPROVED -> "Approved. You can switch to INSTRUCTOR role using POST /auth/switch-role.";
            case REJECTED -> "Application rejected. Reason: " + (application.getRejectionReason() != null ? application.getRejectionReason() : "Contact support or re-apply.");
            case WITHDRAWN -> "Application withdrawn.";
        };

        Map<String, String> documentUrls = new LinkedHashMap<>();
        UUID appId = application.getId();
        if (application.getResumePath() != null) documentUrls.put("resume", "/api/v1/instructors/applications/" + appId + "/documents/resume");
        if (application.getEducationalCertificatesPath() != null) documentUrls.put("educationalCertificates", "/api/v1/instructors/applications/" + appId + "/documents/educationalCertificates");
        if (application.getGovernmentIdProofPath() != null) documentUrls.put("governmentIdProof", "/api/v1/instructors/applications/" + appId + "/documents/governmentIdProof");
        if (application.getPassportPhotoPath() != null) documentUrls.put("passportPhoto", "/api/v1/instructors/applications/" + appId + "/documents/passportPhoto");
        if (application.getBankDetailsPath() != null) documentUrls.put("bankDetails", "/api/v1/instructors/applications/" + appId + "/documents/bankDetails");
        if (application.getPanDocumentPath() != null) documentUrls.put("panDocument", "/api/v1/instructors/applications/" + appId + "/documents/panDocument");
        if (application.getPortfolioPath() != null) documentUrls.put("portfolio", "/api/v1/instructors/applications/" + appId + "/documents/portfolio");
        if (application.getExperienceLetterPath() != null) documentUrls.put("experienceLetter", "/api/v1/instructors/applications/" + appId + "/documents/experienceLetter");
        if (application.getInternshipCertificatePath() != null) documentUrls.put("internshipCertificate", "/api/v1/instructors/applications/" + appId + "/documents/internshipCertificate");
        if (application.getSkillCertificatesPath() != null) documentUrls.put("skillCertificates", "/api/v1/instructors/applications/" + appId + "/documents/skillCertificates");
        if (application.getDemoLecturePptPath() != null) documentUrls.put("demoLecturePpt", "/api/v1/instructors/applications/" + appId + "/documents/demoLecturePpt");
        if (application.getDemoLectureRecordingPath() != null) documentUrls.put("demoLectureRecording", "/api/v1/instructors/applications/" + appId + "/documents/demoLectureRecording");
        if (application.getProjectsPath() != null) documentUrls.put("projects", "/api/v1/instructors/applications/" + appId + "/documents/projects");
        if (application.getApplicationFormPath() != null) documentUrls.put("applicationForm", "/api/v1/instructors/applications/" + appId + "/documents/applicationForm");

        InstructorApplyDetailedResponse.ApplicationInfo applicationInfo =
                InstructorApplyDetailedResponse.ApplicationInfo.builder()
                        .applicationId(application.getId())
                        .status(application.getStatus().name())
                        .reviewMessage(reviewMessage)
                        .submittedAt(application.getSubmittedAt())
                        .reviewedAt(application.getReviewedAt())
                        .reviewedBy(application.getReviewedBy())
                        .rejectionReason(application.getRejectionReason())
                        .adminNotes(application.getAdminNotes())
                        .requestedInfo(application.getRequestedInfo())
                        .subject(application.getSubject())
                        .specialization(application.getSpecialization())
                        .experience(application.getExperience())
                        .bio(application.getBio())
                        .phone(application.getPhone())
                        .linkedinUrl(application.getLinkedinUrl())
                        .websiteUrl(application.getWebsiteUrl())
                        .bankAccountNumber(application.getBankAccountNumber())
                        .bankIfsc(application.getBankIfsc())
                        .bankName(application.getBankName())
                        .panNumber(application.getPanNumber())
                        .additionalNotes(application.getAdditionalNotes())
                        .documentUrls(documentUrls)
                        .build();

        String fullName = "";
        if (user.getFirstName() != null) fullName += user.getFirstName();
        if (user.getLastName() != null) fullName += (fullName.isEmpty() ? "" : " ") + user.getLastName();

        InstructorApplyDetailedResponse.UserInfo userInfo = InstructorApplyDetailedResponse.UserInfo.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(fullName.isEmpty() ? "Applicant" : fullName)
                .mobile(user.getMobile())
                .profilePhoto(user.getProfilePhoto())
                .currentRole(user.getRole() != null ? user.getRole().name() : null)
                .appliedRole(user.getAppliedRole() != null ? user.getAppliedRole().name() : "INSTRUCTOR")
                .accountStatus(user.getStatus() != null ? user.getStatus().name() : null)
                .isInstructorApproved(user.getIsInstructorApproved())
                .build();

        Map<String, Boolean> requiredDocs = new LinkedHashMap<>();
        requiredDocs.put("resume", application.getResumePath() != null);
        requiredDocs.put("educationalCertificates", application.getEducationalCertificatesPath() != null);
        requiredDocs.put("governmentIdProof", application.getGovernmentIdProofPath() != null);
        requiredDocs.put("passportPhoto", application.getPassportPhotoPath() != null);
        requiredDocs.put("bankDetails", application.getBankDetailsPath() != null);
        requiredDocs.put("panDocument", application.getPanDocumentPath() != null);

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

        List<String> nextSteps = List.of(
                "Admin will review your application and documents",
                "You will be notified once approved",
                "After approval, switch role using POST /auth/switch-role with {\"switchRole\":\"INSTRUCTOR\"}",
                "Track status via GET /instructors/applications/me"
        );

        return InstructorApplyDetailedResponse.builder()
                .application(applicationInfo)
                .user(userInfo)
                .documents(documentsInfo)
                .nextSteps(nextSteps)
                .build();
    }
}
