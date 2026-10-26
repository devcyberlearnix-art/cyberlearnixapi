package com.lms.courseservice.service;

import com.lms.courseservice.dto.CertificateResponse;
import com.lms.courseservice.dto.CertificateVerificationResponse;
import com.lms.courseservice.entity.Certificate;
import com.lms.courseservice.entity.CertificateRevocation;
import com.lms.courseservice.entity.CertificateSequence;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Enrollment;
import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.entity.LectureProgress;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.exception.CertificateException;
import com.lms.courseservice.repository.CertificateRepository;
import com.lms.courseservice.repository.CertificateRevocationRepository;
import com.lms.courseservice.repository.CertificateSequenceRepository;
import com.lms.courseservice.repository.EnrollmentRepository;
import com.lms.courseservice.repository.LectureProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private CertificateRevocationRepository certificateRevocationRepository;

    @Mock
    private CertificateSequenceRepository certificateSequenceRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private LectureProgressRepository lectureProgressRepository;

    @Mock
    private CourseService courseService;

    @Mock
    private GoogleDriveService googleDriveService;

    @InjectMocks
    private CertificateService certificateService;

    private UUID studentId;
    private Long courseId;
    private Long enrollmentId;
    private Enrollment enrollment;
    private Course course;
    private Certificate certificate;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        courseId = 1L;
        enrollmentId = 100L;

        enrollment = Enrollment.builder()
                .id(enrollmentId)
                .studentId(studentId)
                .courseId(courseId)
                .studentName("John Doe")
                .status("COMPLETED")
                .completedAt(LocalDateTime.now())
                .build();

        Section section = Section.builder()
                .id(1L)
                .course(course)
                .lectures(new ArrayList<>())
                .build();

        Lecture lecture1 = Lecture.builder()
                .id(1L)
                .section(section)
                .title("Lecture 1")
                .build();

        Lecture lecture2 = Lecture.builder()
                .id(2L)
                .section(section)
                .title("Lecture 2")
                .build();

        section.setLectures(List.of(lecture1, lecture2));

        course = Course.builder()
                .id(courseId)
                .title("Java Programming")
                .sections(List.of(section))
                .build();

        certificate = Certificate.builder()
                .id(1L)
                .certificateNumber("CLX-2026-000006")
                .verificationCode("ABC123DEF456")
                .studentId(studentId)
                .studentName("John Doe")
                .courseId(courseId)
                .courseTitle("Java Programming")
                .enrollmentId(enrollmentId)
                .certificateUrl("https://cloudinary.com/certificates/CLX-2026-000006.pdf")
                .status(Certificate.CertificateStatus.ACTIVE)
                .issuedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void generateCertificate_CompletedCourse_Success() {
        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.of(enrollment));
        when(courseService.getCourseById(courseId)).thenReturn(course);
        when(lectureProgressRepository.countByStudentIdAndCourseIdAndStatus(
                studentId, courseId, LectureProgress.LectureStatus.COMPLETED))
                .thenReturn(2L);
        when(certificateRepository.findByStudentIdAndCourseIdAndStatus(
                studentId, courseId, Certificate.CertificateStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(certificateSequenceRepository.findByYear(anyInt())).thenReturn(Optional.empty());
        when(certificateSequenceRepository.save(any(CertificateSequence.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(googleDriveService.uploadBytes(any(byte[].class), anyString(), anyString()))
                .thenReturn("https://cloudinary.com/certificates/CLX-2026-000006.pdf");
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);

        CertificateResponse response = certificateService.generateCertificate(studentId, courseId);

        assertNotNull(response);
        assertEquals("CLX-2026-000006", response.getCertificateNumber());
        assertEquals("John Doe", response.getStudentName());
        assertEquals("Java Programming", response.getCourseTitle());
        verify(certificateRepository).save(any(Certificate.class));
    }

    @Test
    void generateCertificate_NotEnrolled_ThrowsException() {
        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.empty());

        CertificateException exception = assertThrows(CertificateException.class,
                () -> certificateService.generateCertificate(studentId, courseId));

        assertEquals("Student is not enrolled in this course", exception.getMessage());
    }

    @Test
    void generateCertificate_IncompleteCourse_ThrowsException() {
        enrollment.setStatus("IN_PROGRESS");
        enrollment.setCompletedAt(null);

        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.of(enrollment));

        CertificateException exception = assertThrows(CertificateException.class,
                () -> certificateService.generateCertificate(studentId, courseId));

        assertEquals("Course is not completed. Certificate cannot be issued.", exception.getMessage());
    }

    @Test
    void generateCertificate_CertificateAlreadyExists_ReturnsExisting() {
        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.of(enrollment));
        when(courseService.getCourseById(courseId)).thenReturn(course);
        when(lectureProgressRepository.countByStudentIdAndCourseIdAndStatus(
                studentId, courseId, LectureProgress.LectureStatus.COMPLETED))
                .thenReturn(2L);
        when(certificateRepository.findByStudentIdAndCourseIdAndStatus(
                studentId, courseId, Certificate.CertificateStatus.ACTIVE))
                .thenReturn(Optional.of(certificate));

        CertificateResponse response = certificateService.generateCertificate(studentId, courseId);

        assertNotNull(response);
        assertEquals("CLX-2026-000006", response.getCertificateNumber());
        verify(certificateRepository, never()).save(any(Certificate.class));
        verify(certificateSequenceRepository, never()).save(any(CertificateSequence.class));
    }

    @Test
    void generateCertificate_RevokedCertificate_AllowsReissuance() {
        // When certificate is revoked, it's marked as REVOKED, so ACTIVE check returns empty
        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.of(enrollment));
        when(courseService.getCourseById(courseId)).thenReturn(course);
        when(lectureProgressRepository.countByStudentIdAndCourseIdAndStatus(
                studentId, courseId, LectureProgress.LectureStatus.COMPLETED))
                .thenReturn(2L);
        when(certificateRepository.findByStudentIdAndCourseIdAndStatus(
                studentId, courseId, Certificate.CertificateStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(certificateSequenceRepository.findByYear(anyInt())).thenReturn(Optional.empty());
        when(certificateSequenceRepository.save(any(CertificateSequence.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(googleDriveService.uploadBytes(any(byte[].class), anyString(), anyString()))
                .thenReturn("https://cloudinary.com/certificates/CLX-2026-000002.pdf");
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);

        CertificateResponse response = certificateService.generateCertificate(studentId, courseId);

        assertNotNull(response);
        // Should allow reissuance and create a new certificate
        verify(certificateRepository).save(any(Certificate.class));
    }

    @Test
    void getCertificateById_Owner_Success() {
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));

        CertificateResponse response = certificateService.getCertificateById(1L, studentId);

        assertNotNull(response);
        assertEquals("CLX-2026-000006", response.getCertificateNumber());
    }

    @Test
    void getCertificateById_NotOwner_ThrowsException() {
        UUID otherStudentId = UUID.randomUUID();
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));

        CertificateException exception = assertThrows(CertificateException.class,
                () -> certificateService.getCertificateById(1L, otherStudentId));

        assertEquals("You do not have permission to access this certificate", exception.getMessage());
    }

    @Test
    void getCertificateById_NotFound_ThrowsException() {
        when(certificateRepository.findById(1L)).thenReturn(Optional.empty());

        CertificateException exception = assertThrows(CertificateException.class,
                () -> certificateService.getCertificateById(1L, studentId));

        assertEquals("Certificate not found", exception.getMessage());
    }

    @Test
    void getMyCertificates_Success() {
        List<Certificate> certificates = List.of(certificate);
        when(certificateRepository.findByStudentId(studentId)).thenReturn(certificates);

        List<CertificateResponse> response = certificateService.getMyCertificates(studentId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("CLX-2026-000006", response.get(0).getCertificateNumber());
    }

    @Test
    void verifyCertificate_Valid_Success() {
        when(certificateRepository.findByVerificationCode("ABC123DEF456"))
                .thenReturn(Optional.of(certificate));

        CertificateVerificationResponse response = certificateService.verifyCertificate("ABC123DEF456");

        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals("CLX-2026-000006", response.getCertificateNumber());
        assertEquals("John Doe", response.getStudentName());
        assertEquals("Certificate is valid", response.getMessage());
    }

    @Test
    void verifyCertificate_InvalidCode_ReturnsInvalid() {
        when(certificateRepository.findByVerificationCode("INVALID"))
                .thenReturn(Optional.empty());

        CertificateVerificationResponse response = certificateService.verifyCertificate("INVALID");

        assertNotNull(response);
        assertFalse(response.isValid());
        assertEquals("Certificate not found", response.getMessage());
    }

    @Test
    void verifyCertificate_Revoked_ReturnsRevoked() {
        CertificateRevocation revocation = CertificateRevocation.builder()
                .certificateNumber("CLX-2026-000006")
                .verificationCode("ABC123DEF456")
                .studentId(studentId)
                .studentName("John Doe")
                .courseId(courseId)
                .courseTitle("Java Programming")
                .revokedBy(UUID.randomUUID())
                .revocationReason("Test revocation")
                .revokedAt(LocalDateTime.now())
                .originalIssuedAt(LocalDateTime.now())
                .build();

        when(certificateRepository.findByVerificationCode("ABC123DEF456"))
                .thenReturn(Optional.empty());
        when(certificateRevocationRepository.findByVerificationCode("ABC123DEF456"))
                .thenReturn(Optional.of(revocation));

        CertificateVerificationResponse response = certificateService.verifyCertificate("ABC123DEF456");

        assertNotNull(response);
        assertFalse(response.isValid());
        assertEquals("Certificate has been revoked", response.getMessage());
        assertEquals("REVOKED", response.getStatus());
    }

    @Test
    void revokeCertificate_Success() {
        UUID adminId = UUID.randomUUID();
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));
        when(certificateRevocationRepository.save(any(CertificateRevocation.class))).thenReturn(null);
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);

        certificateService.revokeCertificate(1L, adminId, "Academic dishonesty");

        verify(certificateRevocationRepository).save(any(CertificateRevocation.class));
        verify(certificateRepository).save(certificate);
        assertEquals(Certificate.CertificateStatus.REVOKED, certificate.getStatus());
    }

    @Test
    void revokeCertificate_NotFound_ThrowsException() {
        UUID adminId = UUID.randomUUID();
        when(certificateRepository.findById(1L)).thenReturn(Optional.empty());

        CertificateException exception = assertThrows(CertificateException.class,
                () -> certificateService.revokeCertificate(1L, adminId, "Reason"));

        assertEquals("Certificate not found", exception.getMessage());
    }

    @Test
    void revokeCertificate_ThenReissue_AllowsNewCertificate() {
        UUID adminId = UUID.randomUUID();
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));
        when(certificateRevocationRepository.save(any(CertificateRevocation.class))).thenReturn(null);
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);

        // Revoke
        certificateService.revokeCertificate(1L, adminId, "Test reason");
        verify(certificateRepository).save(certificate);
        assertEquals(Certificate.CertificateStatus.REVOKED, certificate.getStatus());

        // Now try to reissue - should allow since certificate is now REVOKED
        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.of(enrollment));
        when(courseService.getCourseById(courseId)).thenReturn(course);
        when(lectureProgressRepository.countByStudentIdAndCourseIdAndStatus(
                studentId, courseId, LectureProgress.LectureStatus.COMPLETED))
                .thenReturn(2L);
        when(certificateRepository.findByStudentIdAndCourseIdAndStatus(
                studentId, courseId, Certificate.CertificateStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(certificateSequenceRepository.findByYear(anyInt())).thenReturn(Optional.empty());
        when(certificateSequenceRepository.save(any(CertificateSequence.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(googleDriveService.uploadBytes(any(byte[].class), anyString(), anyString()))
                .thenReturn("https://cloudinary.com/certificates/CLX-2026-000007.pdf");
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);

        CertificateResponse response = certificateService.generateCertificate(studentId, courseId);

        assertNotNull(response);
        verify(certificateRepository, times(2)).save(any(Certificate.class));
    }

    @Test
    void verifyCertificate_Active_ReturnsValid() {
        certificate.setStatus(Certificate.CertificateStatus.ACTIVE);
        when(certificateRepository.findByVerificationCode("ABC123DEF456"))
                .thenReturn(Optional.of(certificate));

        CertificateVerificationResponse response = certificateService.verifyCertificate("ABC123DEF456");

        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals("CLX-2026-000006", response.getCertificateNumber());
        assertEquals("Certificate is valid", response.getMessage());
    }

    @Test
    void getCertificatesByCourse_Success() {
        List<Certificate> certificates = List.of(certificate);
        when(certificateRepository.findByCourseId(courseId)).thenReturn(certificates);

        List<CertificateResponse> response = certificateService.getCertificatesByCourse(courseId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("CLX-2026-000006", response.get(0).getCertificateNumber());
    }

    @Test
    void generateCertificate_Idempotency_ReturnsSameCertificate() {
        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.of(enrollment));
        when(courseService.getCourseById(courseId)).thenReturn(course);
        when(lectureProgressRepository.countByStudentIdAndCourseIdAndStatus(
                studentId, courseId, LectureProgress.LectureStatus.COMPLETED))
                .thenReturn(2L);
        when(certificateRepository.findByStudentIdAndCourseIdAndStatus(
                studentId, courseId, Certificate.CertificateStatus.ACTIVE))
                .thenReturn(Optional.of(certificate));

        CertificateResponse response1 = certificateService.generateCertificate(studentId, courseId);
        CertificateResponse response2 = certificateService.generateCertificate(studentId, courseId);

        assertEquals(response1.getCertificateNumber(), response2.getCertificateNumber());
        verify(certificateRepository, never()).save(any(Certificate.class));
        verify(certificateSequenceRepository, never()).save(any(CertificateSequence.class));
    }

    @Test
    void generateCertificate_SequenceGeneration_IncrementsCorrectly() {
        CertificateSequence sequence = CertificateSequence.builder()
                .year(2026)
                .lastSequence(5L)
                .build();

        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.of(enrollment));
        when(courseService.getCourseById(courseId)).thenReturn(course);
        when(lectureProgressRepository.countByStudentIdAndCourseIdAndStatus(
                studentId, courseId, LectureProgress.LectureStatus.COMPLETED))
                .thenReturn(2L);
        when(certificateRepository.findByStudentIdAndCourseIdAndStatus(
                studentId, courseId, Certificate.CertificateStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(certificateSequenceRepository.findByYear(2026)).thenReturn(Optional.of(sequence));
        when(certificateSequenceRepository.save(any(CertificateSequence.class))).thenReturn(sequence);
        when(googleDriveService.uploadBytes(any(byte[].class), anyString(), anyString()))
                .thenReturn("https://cloudinary.com/certificates/CLX-2026-000006.pdf");
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);

        CertificateResponse response = certificateService.generateCertificate(studentId, courseId);

        assertNotNull(response);
        assertEquals("CLX-2026-000006", response.getCertificateNumber());
        verify(certificateSequenceRepository).save(sequence);
        assertEquals(6L, sequence.getLastSequence());
    }
}
