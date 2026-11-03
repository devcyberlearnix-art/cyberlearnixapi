package com.user.register.controller;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.InstructorApplyDetailedResponse;
import com.user.register.dto.InstructorApplyRequest;
import com.user.register.security.JwtUtil;
import com.user.register.service.InstructorService;
import com.user.register.util.BearerTokenResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/instructors")
public class InstructorController {

    private final InstructorService instructorService;
    private final JwtUtil jwtUtil;

    public InstructorController(InstructorService instructorService, JwtUtil jwtUtil) {
        this.instructorService = instructorService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Public Taxonomy of teaching subjects.
     */
    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTeachingSubjects() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Teaching subjects taxonomy retrieved successfully",
                instructorService.getTeachingSubjects(),
                LocalDateTime.now()
        ));
    }

    /**
     * Get current user's application status or draft.
     */
    @GetMapping({"/applications/me", "/applications/my-status", "/applications"})
    public ResponseEntity<ApiResponse<InstructorApplyDetailedResponse>> getApplicationStatus(
            HttpServletRequest httpRequest) {
        try {
            String authorization = resolveBearerHeader(httpRequest);
            InstructorApplyDetailedResponse responseData = instructorService.getApplicationStatus(authorization);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Application status fetched successfully",
                    responseData,
                    LocalDateTime.now()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(false, e.getReason(), null, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }

    /**
     * Save draft application without submitting.
     */
    @PostMapping("/applications/draft")
    public ResponseEntity<ApiResponse<InstructorApplyDetailedResponse>> saveDraft(
            HttpServletRequest httpRequest,
            @RequestBody InstructorApplyRequest draftRequest) {
        try {
            String authorization = resolveBearerHeader(httpRequest);
            InstructorApplyDetailedResponse responseData = instructorService.saveDraft(authorization, draftRequest);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Application draft saved successfully",
                    responseData,
                    LocalDateTime.now()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(false, e.getReason(), null, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }

    /**
     * Upload single document to draft / application.
     */
    @PostMapping(value = "/applications/documents/{docType}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<InstructorApplyDetailedResponse>> uploadDocument(
            HttpServletRequest httpRequest,
            @PathVariable String docType,
            @RequestParam("file") MultipartFile file) {
        try {
            String authorization = resolveBearerHeader(httpRequest);
            InstructorApplyDetailedResponse responseData = instructorService.uploadSingleDocument(authorization, docType, file);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Document " + docType + " uploaded successfully",
                    responseData,
                    LocalDateTime.now()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(false, e.getReason(), null, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }

    /**
     * Remove an uploaded document from draft.
     */
    @DeleteMapping("/applications/documents/{docType}")
    public ResponseEntity<ApiResponse<InstructorApplyDetailedResponse>> removeDocument(
            HttpServletRequest httpRequest,
            @PathVariable String docType) {
        try {
            String authorization = resolveBearerHeader(httpRequest);
            InstructorApplyDetailedResponse responseData = instructorService.removeDocument(authorization, docType);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Document " + docType + " removed successfully",
                    responseData,
                    LocalDateTime.now()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(false, e.getReason(), null, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }

    /**
     * Complete Application Submission (Multipart).
     */
    @PostMapping(value = {"/applications", "/applications/apply"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<InstructorApplyDetailedResponse>> applyForInstructor(
            HttpServletRequest httpRequest,
            @RequestPart(value = "applicationBody", required = false) InstructorApplyRequest applicationBody,
            @RequestParam(value = "subject", required = false) String subjectParam,
            @RequestParam(value = "contentType", required = false) String contentTypeParam,
            @RequestParam(value = "specialization", required = false) String specializationParam,
            @RequestParam(value = "experience", required = false) String experienceParam,
            @RequestParam(value = "bio", required = false) String bioParam,
            @RequestParam(value = "phone", required = false) String phoneParam,
            @RequestParam(value = "linkedIn", required = false) String linkedInParam,
            @RequestParam(value = "linkedinUrl", required = false) String linkedinUrlParam,
            @RequestParam(value = "website", required = false) String websiteParam,
            @RequestParam(value = "websiteUrl", required = false) String websiteUrlParam,
            @RequestPart(value = "resume", required = false) MultipartFile resume,
            @RequestPart(value = "educationalCertificates", required = false) MultipartFile educationalCertificates,
            @RequestPart(value = "governmentIdProof", required = false) MultipartFile governmentIdProof,
            @RequestPart(value = "experienceLetter", required = false) MultipartFile experienceLetter,
            @RequestPart(value = "internshipCertificate", required = false) MultipartFile internshipCertificate,
            @RequestPart(value = "skillCertificates", required = false) MultipartFile skillCertificates,
            @RequestPart(value = "portfolio", required = false) MultipartFile portfolio,
            @RequestPart(value = "demoLecturePpt", required = false) MultipartFile demoLecturePpt,
            @RequestPart(value = "demoLectureRecording", required = false) MultipartFile demoLectureRecording,
            @RequestPart(value = "projects", required = false) MultipartFile projects,
            @RequestPart(value = "passportPhoto", required = false) MultipartFile passportPhoto,
            @RequestPart(value = "bankDetails", required = false) MultipartFile bankDetails,
            @RequestPart(value = "panDocument", required = false) MultipartFile panDocument,
            @RequestPart(value = "applicationForm", required = false) MultipartFile applicationForm
    ) {
        try {
            String authorization = resolveBearerHeader(httpRequest);

            String subject = subjectParam != null ? subjectParam : (contentTypeParam != null ? contentTypeParam : (applicationBody != null ? applicationBody.getSubject() : null));
            String specialization = specializationParam != null ? specializationParam : (applicationBody != null ? applicationBody.getSpecialization() : null);
            String experience = experienceParam != null ? experienceParam : (applicationBody != null ? applicationBody.getExperience() : null);
            String bio = bioParam != null ? bioParam : (applicationBody != null ? applicationBody.getBio() : null);
            String phone = phoneParam != null ? phoneParam : (applicationBody != null ? applicationBody.getPhone() : null);
            String linkedinUrl = linkedinUrlParam != null ? linkedinUrlParam : (linkedInParam != null ? linkedInParam : (applicationBody != null ? applicationBody.getLinkedinUrl() : null));
            String websiteUrl = websiteUrlParam != null ? websiteUrlParam : (websiteParam != null ? websiteParam : (applicationBody != null ? applicationBody.getWebsiteUrl() : null));

            String bankAccountNumber = applicationBody != null ? applicationBody.getBankAccountNumber() : null;
            String bankIfsc = applicationBody != null ? applicationBody.getBankIfsc() : null;
            String bankName = applicationBody != null ? applicationBody.getBankName() : null;
            String panNumber = applicationBody != null ? applicationBody.getPanNumber() : null;
            String additionalNotes = applicationBody != null ? applicationBody.getAdditionalNotes() : null;

            InstructorApplyDetailedResponse responseData = instructorService.applyForInstructor(
                    authorization, resume, educationalCertificates, governmentIdProof,
                    experienceLetter, internshipCertificate, skillCertificates, portfolio,
                    demoLecturePpt, demoLectureRecording, projects, passportPhoto,
                    bankDetails, panDocument, applicationForm,
                    subject, specialization, experience, bio, phone, linkedinUrl, websiteUrl,
                    bankAccountNumber, bankIfsc, bankName, panNumber, additionalNotes
            );

            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Instructor application submitted successfully. Status: PENDING review.",
                    responseData,
                    LocalDateTime.now()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(false, e.getReason(), null, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }

    /**
     * Complete Application Submission (JSON - when documents have already been uploaded).
     */
    @PostMapping(value = {"/applications", "/applications/apply"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<InstructorApplyDetailedResponse>> applyForInstructorJson(
            HttpServletRequest httpRequest,
            @RequestBody InstructorApplyRequest request) {
        try {
            String authorization = resolveBearerHeader(httpRequest);
            InstructorApplyDetailedResponse responseData = instructorService.applyForInstructor(
                    authorization, null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null,
                    request.getSubject(), request.getSpecialization(), request.getExperience(),
                    request.getBio(), request.getPhone(), request.getLinkedinUrl(), request.getWebsiteUrl(),
                    request.getBankAccountNumber(), request.getBankIfsc(), request.getBankName(),
                    request.getPanNumber(), request.getAdditionalNotes()
            );

            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Instructor application submitted successfully. Status: PENDING review.",
                    responseData,
                    LocalDateTime.now()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(false, e.getReason(), null, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }

    /**
     * Authenticated secure document view / download.
     */
    @GetMapping("/applications/{applicationId}/documents/{docType}")
    public ResponseEntity<Resource> getDocument(
            HttpServletRequest httpRequest,
            @PathVariable UUID applicationId,
            @PathVariable String docType) {
        String authorization = resolveBearerHeader(httpRequest);
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
        String userIdStr = jwtUtil.extractUserId(token);
        UUID requestingUserId = (userIdStr != null && !userIdStr.isBlank()) ? UUID.fromString(userIdStr) : null;
        String requestingRole = jwtUtil.extractRole(token);

        Resource resource = instructorService.getDocumentResource(applicationId, docType, requestingUserId, requestingRole);

        String contentType = "application/octet-stream";
        if (resource.getFilename() != null) {
            String fn = resource.getFilename().toLowerCase();
            if (fn.endsWith(".pdf")) contentType = "application/pdf";
            else if (fn.endsWith(".png")) contentType = "image/png";
            else if (fn.endsWith(".jpg") || fn.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (fn.endsWith(".webp")) contentType = "image/webp";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    private String resolveBearerHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || header.isBlank()) {
            header = request.getHeader("authorization");
        }
        BearerTokenResolver.extractBearerToken(header);
        return header;
    }
}
