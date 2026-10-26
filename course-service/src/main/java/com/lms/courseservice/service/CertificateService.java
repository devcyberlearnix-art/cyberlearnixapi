package com.lms.courseservice.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.lms.courseservice.dto.CertificateResponse;
import com.lms.courseservice.dto.CertificateVerificationResponse;
import com.lms.courseservice.entity.Certificate;
import com.lms.courseservice.entity.CertificateRevocation;
import com.lms.courseservice.entity.CertificateSequence;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Enrollment;
import com.lms.courseservice.entity.LectureProgress;
import com.lms.courseservice.exception.CertificateException;
import com.lms.courseservice.repository.CertificateRepository;
import com.lms.courseservice.repository.CertificateRevocationRepository;
import com.lms.courseservice.repository.CertificateSequenceRepository;
import com.lms.courseservice.repository.EnrollmentRepository;
import com.lms.courseservice.repository.LectureProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CertificateRevocationRepository certificateRevocationRepository;
    private final CertificateSequenceRepository certificateSequenceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LectureProgressRepository lectureProgressRepository;
    private final CourseService courseService;
    private final GoogleDriveService googleDriveService;

    /**
     * Generate or retrieve certificate for a student's completed course
     */
    @Transactional
    public CertificateResponse generateCertificate(UUID studentId, Long courseId) {
        // Validate enrollment
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new CertificateException("Student is not enrolled in this course"));

        // Validate course completion
        if (!isCourseCompleted(studentId, courseId)) {
            throw new CertificateException("Course is not completed. Certificate cannot be issued.");
        }

        // Check if ACTIVE certificate already exists (idempotency)
        Certificate existingCertificate = certificateRepository.findByStudentIdAndCourseIdAndStatus(
                studentId, courseId, Certificate.CertificateStatus.ACTIVE)
                .orElse(null);
        if (existingCertificate != null) {
            log.info("Active certificate already exists for student {} and course {}", studentId, courseId);
            return mapToCertificateResponse(existingCertificate);
        }

        // Get course details
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            throw new CertificateException("Course not found");
        }

        // Generate certificate number
        String certificateNumber = generateCertificateNumber();

        // Generate verification code
        String verificationCode = generateVerificationCode();

        // Generate PDF certificate
        byte[] pdfBytes = generatePdfCertificate(
                enrollment.getStudentName(),
                course.getTitle(),
                certificateNumber,
                verificationCode,
                LocalDateTime.now()
        );

        // Upload to Cloudinary
        String certificateUrl = uploadCertificateToCloudinary(pdfBytes, certificateNumber);

        // Create certificate record
        Certificate certificate = Certificate.builder()
                .certificateNumber(certificateNumber)
                .verificationCode(verificationCode)
                .studentId(studentId)
                .studentName(enrollment.getStudentName())
                .courseId(courseId)
                .courseTitle(course.getTitle())
                .enrollmentId(enrollment.getId())
                .certificateUrl(certificateUrl)
                .issuedAt(LocalDateTime.now())
                .build();

        certificate = certificateRepository.save(certificate);
        log.info("Certificate generated: {} for student: {} in course: {}", certificateNumber, studentId, courseId);

        return mapToCertificateResponse(certificate);
    }

    /**
     * Get certificate by ID (student can only access their own)
     */
    @Transactional(readOnly = true)
    public CertificateResponse getCertificateById(Long certificateId, UUID studentId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new CertificateException("Certificate not found"));

        if (!certificate.getStudentId().equals(studentId)) {
            throw new CertificateException("You do not have permission to access this certificate");
        }

        return mapToCertificateResponse(certificate);
    }

    /**
     * Get all certificates for a student
     */
    @Transactional(readOnly = true)
    public java.util.List<CertificateResponse> getMyCertificates(UUID studentId) {
        return certificateRepository.findByStudentId(studentId).stream()
                .map(this::mapToCertificateResponse)
                .toList();
    }

    /**
     * Verify certificate by verification code (public endpoint)
     */
    @Transactional(readOnly = true)
    public CertificateVerificationResponse verifyCertificate(String verificationCode) {
        // Check certificates (both active and revoked)
        Certificate certificate = certificateRepository.findByVerificationCode(verificationCode)
                .orElse(null);

        if (certificate != null) {
            if (certificate.getStatus() == Certificate.CertificateStatus.ACTIVE) {
                return CertificateVerificationResponse.builder()
                        .valid(true)
                        .certificateNumber(certificate.getCertificateNumber())
                        .studentName(certificate.getStudentName())
                        .courseTitle(certificate.getCourseTitle())
                        .status(certificate.getStatus().name())
                        .issuedAt(certificate.getIssuedAt())
                        .message("Certificate is valid")
                        .build();
            } else {
                return CertificateVerificationResponse.builder()
                        .valid(false)
                        .certificateNumber(certificate.getCertificateNumber())
                        .studentName(certificate.getStudentName())
                        .courseTitle(certificate.getCourseTitle())
                        .status(certificate.getStatus().name())
                        .issuedAt(certificate.getIssuedAt())
                        .message("Certificate has been revoked")
                        .build();
            }
        }

        // Check revocation records for historical revoked certificates
        CertificateRevocation revocation = certificateRevocationRepository.findByVerificationCode(verificationCode)
                .orElse(null);

        if (revocation != null) {
            return CertificateVerificationResponse.builder()
                    .valid(false)
                    .certificateNumber(revocation.getCertificateNumber())
                    .studentName(revocation.getStudentName())
                    .courseTitle(revocation.getCourseTitle())
                    .status("REVOKED")
                    .issuedAt(revocation.getOriginalIssuedAt())
                    .message("Certificate has been revoked")
                    .build();
        }

        return CertificateVerificationResponse.builder()
                .valid(false)
                .message("Certificate not found")
                .build();
    }

    /**
     * Revoke certificate (admin only)
     */
    @Transactional
    public void revokeCertificate(Long certificateId, UUID adminId, String reason) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new CertificateException("Certificate not found"));

        if (certificate.getStatus() == Certificate.CertificateStatus.REVOKED) {
            throw new CertificateException("Certificate is already revoked");
        }

        // Save revocation record before marking as revoked
        CertificateRevocation revocation = CertificateRevocation.builder()
                .certificateNumber(certificate.getCertificateNumber())
                .verificationCode(certificate.getVerificationCode())
                .studentId(certificate.getStudentId())
                .studentName(certificate.getStudentName())
                .courseId(certificate.getCourseId())
                .courseTitle(certificate.getCourseTitle())
                .revokedBy(adminId)
                .revocationReason(reason)
                .revokedAt(LocalDateTime.now())
                .originalIssuedAt(certificate.getIssuedAt())
                .originalCertificateUrl(certificate.getCertificateUrl())
                .build();

        certificateRevocationRepository.save(revocation);

        // Mark certificate as revoked instead of deleting
        certificate.setStatus(Certificate.CertificateStatus.REVOKED);
        certificateRepository.save(certificate);

        log.info("Certificate {} revoked by admin {} for reason: {}", certificateId, adminId, reason);
    }

    /**
     * Get certificates for a course (admin)
     */
    @Transactional(readOnly = true)
    public java.util.List<CertificateResponse> getCertificatesByCourse(Long courseId) {
        return certificateRepository.findByCourseId(courseId).stream()
                .map(this::mapToCertificateResponse)
                .toList();
    }

    // Helper methods

    private boolean isCourseCompleted(UUID studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElse(null);
        if (enrollment == null) {
            return false;
        }

        if (!"COMPLETED".equals(enrollment.getStatus()) || enrollment.getCompletedAt() == null) {
            return false;
        }

        // Double-check lecture completion
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            return false;
        }

        int totalLectures = course.getSections().stream()
                .mapToInt(section -> section.getLectures() != null ? section.getLectures().size() : 0)
                .sum();

        if (totalLectures == 0) {
            return false;
        }

        long completedLectures = lectureProgressRepository.countByStudentIdAndCourseIdAndStatus(
                studentId, courseId, LectureProgress.LectureStatus.COMPLETED);

        return completedLectures == totalLectures;
    }

    private String generateCertificateNumber() {
        int currentYear = Year.now().getValue();

        CertificateSequence sequence = certificateSequenceRepository.findByYear(currentYear)
                .orElse(null);

        if (sequence == null) {
            try {
                sequence = CertificateSequence.builder()
                        .year(currentYear)
                        .lastSequence(0L)
                        .build();
                sequence = certificateSequenceRepository.save(sequence);
            } catch (Exception e) {
                // Handle race condition: another thread may have created the sequence
                sequence = certificateSequenceRepository.findByYear(currentYear)
                        .orElseThrow(() -> new CertificateException("Failed to create certificate sequence for year: " + currentYear));
            }
        }

        long nextSequence = sequence.getLastSequence() + 1;
        sequence.setLastSequence(nextSequence);

        certificateSequenceRepository.save(sequence);

        String year = String.valueOf(currentYear);
        String sequenceStr = String.format("%06d", nextSequence);
        return "CLX-" + year + "-" + sequenceStr;
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private byte[] generatePdfCertificate(String studentName, String courseTitle,
                                           String certificateNumber, String verificationCode,
                                           LocalDateTime issuedAt) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // Title
            Paragraph title = new Paragraph("Certificate of Completion")
                    .setFont(boldFont)
                    .setFontSize(24)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(100);
            document.add(title);

            // Line
            document.add(new Paragraph("------------------------------------------------")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20));

            // Student name
            document.add(new Paragraph("This is to certify that")
                    .setFont(font)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(40));

            document.add(new Paragraph(studentName)
                    .setFont(boldFont)
                    .setFontSize(20)
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10));

            // Course completion
            document.add(new Paragraph("has successfully completed the course")
                    .setFont(font)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30));

            document.add(new Paragraph(courseTitle)
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10));

            // Certificate details
            document.add(new Paragraph("Certificate Number: " + certificateNumber)
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(50));

            document.add(new Paragraph("Verification Code: " + verificationCode)
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Issued on: " + issuedAt.toLocalDate())
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            // Footer
            document.add(new Paragraph("CyberLearnix Learning Management System")
                    .setFont(font)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(60));

            document.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new CertificateException("Failed to generate PDF certificate: " + e.getMessage(), e);
        }
    }

    private String uploadCertificateToCloudinary(byte[] pdfBytes, String certificateNumber) {
        try {
            String fileName = "certificates/" + certificateNumber + ".pdf";
            return googleDriveService.uploadBytes(pdfBytes, fileName, "application/pdf");
        } catch (Exception e) {
            throw new CertificateException("Failed to upload certificate to Google Drive: " + e.getMessage(), e);
        }
    }

    private CertificateResponse mapToCertificateResponse(Certificate certificate) {
        return CertificateResponse.builder()
                .id(certificate.getId())
                .certificateNumber(certificate.getCertificateNumber())
                .verificationCode(certificate.getVerificationCode())
                .studentName(certificate.getStudentName())
                .courseTitle(certificate.getCourseTitle())
                .certificateUrl(certificate.getCertificateUrl())
                .status(certificate.getStatus().name())
                .issuedAt(certificate.getIssuedAt())
                .createdAt(certificate.getCreatedAt())
                .build();
    }
}
