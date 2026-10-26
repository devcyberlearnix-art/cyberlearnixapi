package com.lms.courseservice.service;

import com.lms.courseservice.dto.GradingRequest;
import com.lms.courseservice.dto.SubmissionRequest;
import com.lms.courseservice.dto.SubmissionResponse;
import com.lms.courseservice.dto.SubmissionResultResponse;
import com.lms.courseservice.entity.Assignment;
import com.lms.courseservice.entity.AssignmentSubmission;
import com.lms.courseservice.exception.AssignmentException;
import com.lms.courseservice.repository.AssignmentRepository;
import com.lms.courseservice.repository.AssignmentSubmissionRepository;
import com.lms.courseservice.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private AssignmentSubmissionRepository submissionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private SubmissionService submissionService;

    private UUID testStudentId;
    private UUID testInstructorId;
    private Long testCourseId;
    private Long testAssignmentId;
    private Long testSubmissionId;
    private Assignment testAssignment;
    private AssignmentSubmission testSubmission;

    @BeforeEach
    void setUp() {
        testStudentId = UUID.randomUUID();
        testInstructorId = UUID.randomUUID();
        testCourseId = 1L;
        testAssignmentId = 10L;
        testSubmissionId = 100L;

        testAssignment = Assignment.builder()
                .id(testAssignmentId)
                .courseId(testCourseId)
                .title("Test Assignment")
                .maxScore(100)
                .status(Assignment.AssignmentStatus.PUBLISHED)
                .createdBy(testInstructorId)
                .build();

        testSubmission = AssignmentSubmission.builder()
                .id(testSubmissionId)
                .assignmentId(testAssignmentId)
                .studentId(testStudentId)
                .submissionText("Test submission text")
                .status(AssignmentSubmission.SubmissionStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createOrUpdateSubmission_NewSubmission_CreatesSubmission() {
        SubmissionRequest request = SubmissionRequest.builder()
                .submissionText("New submission text")
                .build();

        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(submissionRepository.findByAssignmentIdAndStudentId(testAssignmentId, testStudentId))
                .thenReturn(Optional.empty());
        when(submissionRepository.save(any(AssignmentSubmission.class))).thenAnswer(invocation -> {
            AssignmentSubmission saved = invocation.getArgument(0);
            saved.setId(testSubmissionId);
            return saved;
        });

        SubmissionResponse response = submissionService.createOrUpdateSubmission(testAssignmentId, testStudentId, request);

        assertNotNull(response);
        assertEquals(testSubmissionId, response.getId());
        assertEquals("New submission text", response.getSubmissionText());
        assertEquals("DRAFT", response.getStatus());
        verify(submissionRepository, times(1)).save(any(AssignmentSubmission.class));
    }

    @Test
    void createOrUpdateSubmission_ExistingSubmission_UpdatesSubmission() {
        SubmissionRequest request = SubmissionRequest.builder()
                .submissionText("Updated submission text")
                .build();

        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(submissionRepository.findByAssignmentIdAndStudentId(testAssignmentId, testStudentId))
                .thenReturn(Optional.of(testSubmission));
        when(submissionRepository.save(any(AssignmentSubmission.class))).thenReturn(testSubmission);

        SubmissionResponse response = submissionService.createOrUpdateSubmission(testAssignmentId, testStudentId, request);

        assertNotNull(response);
        verify(submissionRepository, times(1)).save(any(AssignmentSubmission.class));
    }

    @Test
    void createOrUpdateSubmission_NotEnrolled_ThrowsException() {
        SubmissionRequest request = SubmissionRequest.builder()
                .submissionText("New submission text")
                .build();

        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(false);

        assertThrows(AssignmentException.class, () -> 
                submissionService.createOrUpdateSubmission(testAssignmentId, testStudentId, request));
        verify(submissionRepository, never()).save(any(AssignmentSubmission.class));
    }

    @Test
    void createOrUpdateSubmission_AlreadySubmitted_ThrowsException() {
        testSubmission.setStatus(AssignmentSubmission.SubmissionStatus.SUBMITTED);
        SubmissionRequest request = SubmissionRequest.builder()
                .submissionText("Updated submission text")
                .build();

        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(submissionRepository.findByAssignmentIdAndStudentId(testAssignmentId, testStudentId))
                .thenReturn(Optional.of(testSubmission));

        assertThrows(AssignmentException.class, () -> 
                submissionService.createOrUpdateSubmission(testAssignmentId, testStudentId, request));
        verify(submissionRepository, never()).save(any(AssignmentSubmission.class));
    }

    @Test
    void submitAssignment_ValidSubmission_SubmitsAssignment() {
        testSubmission.setSubmissionText("Test submission text");
        testSubmission.setStatus(AssignmentSubmission.SubmissionStatus.DRAFT);

        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(submissionRepository.findByAssignmentIdAndStudentId(testAssignmentId, testStudentId))
                .thenReturn(Optional.of(testSubmission));
        when(submissionRepository.save(any(AssignmentSubmission.class))).thenReturn(testSubmission);

        SubmissionResponse response = submissionService.submitAssignment(testAssignmentId, testStudentId);

        assertNotNull(response);
        assertEquals("SUBMITTED", response.getStatus());
        assertNotNull(response.getSubmittedAt());
        verify(submissionRepository, times(1)).save(any(AssignmentSubmission.class));
    }

    @Test
    void submitAssignment_NotEnrolled_ThrowsException() {
        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(false);

        assertThrows(AssignmentException.class, () -> 
                submissionService.submitAssignment(testAssignmentId, testStudentId));
        verify(submissionRepository, never()).save(any(AssignmentSubmission.class));
    }

    @Test
    void submitAssignment_NoSubmission_ThrowsException() {
        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(submissionRepository.findByAssignmentIdAndStudentId(testAssignmentId, testStudentId))
                .thenReturn(Optional.empty());

        assertThrows(AssignmentException.class, () -> 
                submissionService.submitAssignment(testAssignmentId, testStudentId));
    }

    @Test
    void submitAssignment_AlreadySubmitted_ThrowsException() {
        testSubmission.setStatus(AssignmentSubmission.SubmissionStatus.SUBMITTED);

        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(submissionRepository.findByAssignmentIdAndStudentId(testAssignmentId, testStudentId))
                .thenReturn(Optional.of(testSubmission));

        assertThrows(AssignmentException.class, () -> 
                submissionService.submitAssignment(testAssignmentId, testStudentId));
    }

    @Test
    void submitAssignment_EmptySubmission_ThrowsException() {
        testSubmission.setSubmissionText(null);
        testSubmission.setFileUrl(null);

        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(submissionRepository.findByAssignmentIdAndStudentId(testAssignmentId, testStudentId))
                .thenReturn(Optional.of(testSubmission));

        assertThrows(AssignmentException.class, () -> 
                submissionService.submitAssignment(testAssignmentId, testStudentId));
    }

    @Test
    void getSubmission_ValidAssignment_ReturnsSubmission() {
        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(submissionRepository.findByAssignmentIdAndStudentId(testAssignmentId, testStudentId))
                .thenReturn(Optional.of(testSubmission));

        SubmissionResponse response = submissionService.getSubmission(testAssignmentId, testStudentId);

        assertNotNull(response);
        assertEquals(testSubmissionId, response.getId());
        assertEquals("Test submission text", response.getSubmissionText());
    }

    @Test
    void getSubmission_NotEnrolled_ThrowsException() {
        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(false);

        assertThrows(AssignmentException.class, () -> 
                submissionService.getSubmission(testAssignmentId, testStudentId));
    }

    @Test
    void getSubmission_NoSubmission_ThrowsException() {
        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(submissionRepository.findByAssignmentIdAndStudentId(testAssignmentId, testStudentId))
                .thenReturn(Optional.empty());

        assertThrows(AssignmentException.class, () -> 
                submissionService.getSubmission(testAssignmentId, testStudentId));
    }

    @Test
    void getSubmissionById_ValidId_ReturnsSubmission() {
        when(submissionRepository.findById(testSubmissionId)).thenReturn(Optional.of(testSubmission));

        SubmissionResponse response = submissionService.getSubmissionById(testSubmissionId);

        assertNotNull(response);
        assertEquals(testSubmissionId, response.getId());
    }

    @Test
    void getSubmissionById_NotFound_ThrowsException() {
        when(submissionRepository.findById(testSubmissionId)).thenReturn(Optional.empty());

        assertThrows(AssignmentException.class, () -> submissionService.getSubmissionById(testSubmissionId));
    }

    @Test
    void getSubmissionsByAssignment_ValidAssignment_ReturnsSubmissions() {
        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.findByAssignmentId(testAssignmentId)).thenReturn(List.of(testSubmission));

        List<SubmissionResponse> response = submissionService.getSubmissionsByAssignment(testAssignmentId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(testSubmissionId, response.get(0).getId());
    }

    @Test
    void gradeSubmission_ValidRequest_GradesSubmission() {
        testSubmission.setStatus(AssignmentSubmission.SubmissionStatus.SUBMITTED);
        GradingRequest request = GradingRequest.builder()
                .score(85)
                .feedback("Good work")
                .build();

        when(submissionRepository.findById(testSubmissionId)).thenReturn(Optional.of(testSubmission));
        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.save(any(AssignmentSubmission.class))).thenReturn(testSubmission);

        SubmissionResponse response = submissionService.gradeSubmission(testSubmissionId, request, testInstructorId);

        assertNotNull(response);
        assertEquals(85, response.getScore());
        assertEquals("Good work", response.getFeedback());
        assertEquals("GRADED", response.getStatus());
        assertNotNull(response.getGradedAt());
        verify(submissionRepository, times(1)).save(any(AssignmentSubmission.class));
    }

    @Test
    void gradeSubmission_NotSubmitted_ThrowsException() {
        testSubmission.setStatus(AssignmentSubmission.SubmissionStatus.DRAFT);
        GradingRequest request = GradingRequest.builder()
                .score(85)
                .build();

        when(submissionRepository.findById(testSubmissionId)).thenReturn(Optional.of(testSubmission));
        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));

        assertThrows(AssignmentException.class, () -> 
                submissionService.gradeSubmission(testSubmissionId, request, testInstructorId));
    }

    @Test
    void gradeSubmission_ScoreExceedsMax_ThrowsException() {
        testSubmission.setStatus(AssignmentSubmission.SubmissionStatus.SUBMITTED);
        GradingRequest request = GradingRequest.builder()
                .score(150)
                .build();

        when(submissionRepository.findById(testSubmissionId)).thenReturn(Optional.of(testSubmission));
        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));

        assertThrows(AssignmentException.class, () -> 
                submissionService.gradeSubmission(testSubmissionId, request, testInstructorId));
    }

    @Test
    void gradeSubmission_NegativeScore_ThrowsException() {
        testSubmission.setStatus(AssignmentSubmission.SubmissionStatus.SUBMITTED);
        GradingRequest request = GradingRequest.builder()
                .score(-10)
                .build();

        when(submissionRepository.findById(testSubmissionId)).thenReturn(Optional.of(testSubmission));
        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));

        assertThrows(AssignmentException.class, () -> 
                submissionService.gradeSubmission(testSubmissionId, request, testInstructorId));
    }

    @Test
    void getSubmissionResult_ValidAssignment_ReturnsResult() {
        testSubmission.setScore(85);
        testSubmission.setStatus(AssignmentSubmission.SubmissionStatus.GRADED);
        testSubmission.setSubmittedAt(LocalDateTime.now());
        testSubmission.setGradedAt(LocalDateTime.now());

        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(submissionRepository.findByAssignmentIdAndStudentId(testAssignmentId, testStudentId))
                .thenReturn(Optional.of(testSubmission));

        SubmissionResultResponse response = submissionService.getSubmissionResult(testAssignmentId, testStudentId);

        assertNotNull(response);
        assertEquals(85, response.getScore());
        assertEquals(100, response.getMaxScore());
        assertEquals(85, response.getPercentage());
        assertEquals("GRADED", response.getStatus());
    }

    @Test
    void getSubmissionsByStudent_ValidStudent_ReturnsSubmissions() {
        when(submissionRepository.findByStudentId(testStudentId)).thenReturn(List.of(testSubmission));

        List<SubmissionResponse> response = submissionService.getSubmissionsByStudent(testStudentId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(testSubmissionId, response.get(0).getId());
    }
}