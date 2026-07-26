package com.user.register.controller;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.InstructorApplyDetailedResponse;
import com.user.register.dto.InstructorApplyRequest;
import com.user.register.service.InstructorService;
import com.user.register.util.BearerTokenResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/instructors")
public class InstructorController {

    private final InstructorService instructorService;

    public InstructorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    /**
     * Auth header only (not body): Authorization: Bearer &lt;user_access_token&gt;
     */
    @PostMapping(value = "/applications", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<InstructorApplyDetailedResponse>> applyForInstructor(
            HttpServletRequest httpRequest,
            @RequestPart(value = "applicationBody", required = false) InstructorApplyRequest applicationBody,
            @RequestPart("resume") MultipartFile resume,
            @RequestPart("educationalCertificates") MultipartFile educationalCertificates,
            @RequestPart("governmentIdProof") MultipartFile governmentIdProof,
            @RequestPart(value = "experienceLetter", required = false) MultipartFile experienceLetter,
            @RequestPart(value = "internshipCertificate", required = false) MultipartFile internshipCertificate,
            @RequestPart(value = "skillCertificates", required = false) MultipartFile skillCertificates,
            @RequestPart(value = "portfolio", required = false) MultipartFile portfolio,
            @RequestPart(value = "demoLecturePpt", required = false) MultipartFile demoLecturePpt,
            @RequestPart(value = "demoLectureRecording", required = false) MultipartFile demoLectureRecording,
            @RequestPart(value = "projects", required = false) MultipartFile projects,
            @RequestPart("passportPhoto") MultipartFile passportPhoto,
            @RequestPart("bankDetails") MultipartFile bankDetails,
            @RequestPart("panDocument") MultipartFile panDocument,
            @RequestPart(value = "applicationForm", required = false) MultipartFile applicationForm
    ) {
        try {
            String authorization = resolveBearerHeader(httpRequest);

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
                    bankAccountNumber, bankIfsc, bankName, panNumber, additionalNotes
            );

            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Instructor application submitted successfully. Status: PENDING approval.",
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

    @GetMapping("/applications/me")
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


    /** Reads Authorization header — token must be Bearer only, never from body or cookies. */
    private String resolveBearerHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || header.isBlank()) {
            header = request.getHeader("authorization");
        }
        BearerTokenResolver.extractBearerToken(header);
        return header;
    }
}
