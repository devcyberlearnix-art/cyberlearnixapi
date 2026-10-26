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
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final AssignmentSubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * Create or update submission (draft)
     */
    @Transactional
    public SubmissionResponse createOrUpdateSubmission(Long assignmentId, UUID studentId, SubmissionRequest request) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentException("Assignment not found with id: " + assignmentId));
        
        validateEnrollment(studentId, assignment.getCourseId());
        
        AssignmentSubmission submission = submissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElse(null);
        
        if (submission == null) {
            // Create new submission
            submission = AssignmentSubmission.builder()
                    .assignmentId(assignmentId)
                    .studentId(studentId)
                    .submissionText(request.getSubmissionText())
                    .fileUrl(request.getFileUrl())
                    .status(AssignmentSubmission.SubmissionStatus.DRAFT)
                    .build();
        } else {
            // Update existing submission
            if (submission.getStatus() == AssignmentSubmission.SubmissionStatus.SUBMITTED || 
                submission.getStatus() == AssignmentSubmission.SubmissionStatus.GRADED) {
                throw new AssignmentException("Cannot update a submitted or graded assignment");
            }
            submission.setSubmissionText(request.getSubmissionText());
            submission.setFileUrl(request.getFileUrl());
        }
        
        submission = submissionRepository.save(submission);
        log.info("Created/updated submission for assignment: {} by student: {}", assignmentId, studentId);
        
        return mapToSubmissionResponse(submission);
    }

    /**
     * Submit assignment
     */
    @Transactional
    public SubmissionResponse submitAssignment(Long assignmentId, UUID studentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentException("Assignment not found with id: " + assignmentId));
        
        validateEnrollment(studentId, assignment.getCourseId());
        
        AssignmentSubmission submission = submissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElseThrow(() -> new AssignmentException("No submission found. Create a draft first."));
        
        if (submission.getStatus() == AssignmentSubmission.SubmissionStatus.SUBMITTED || 
            submission.getStatus() == AssignmentSubmission.SubmissionStatus.GRADED) {
            throw new AssignmentException("Assignment has already been submitted");
        }
        
        if (submission.getSubmissionText() == null && submission.getFileUrl() == null) {
            throw new AssignmentException("Cannot submit empty assignment");
        }
        
        submission.setStatus(AssignmentSubmission.SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());
        submission = submissionRepository.save(submission);
        log.info("Submitted assignment: {} by student: {}", assignmentId, studentId);
        
        return mapToSubmissionResponse(submission);
    }

    /**
     * Get student's submission for an assignment
     */
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmission(Long assignmentId, UUID studentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentException("Assignment not found with id: " + assignmentId));
        
        validateEnrollment(studentId, assignment.getCourseId());
        
        AssignmentSubmission submission = submissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElse(null);
        
        if (submission == null) {
            throw new AssignmentException("No submission found for this assignment");
        }
        
        return mapToSubmissionResponse(submission);
    }

    /**
     * Get submission by ID (instructor view)
     */
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(Long submissionId) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new AssignmentException("Submission not found with id: " + submissionId));
        
        return mapToSubmissionResponse(submission);
    }

    /**
     * Get all submissions for an assignment (instructor view)
     */
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentException("Assignment not found with id: " + assignmentId));
        
        List<AssignmentSubmission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        return submissions.stream()
                .map(this::mapToSubmissionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Grade a submission
     */
    @Transactional
    public SubmissionResponse gradeSubmission(Long submissionId, GradingRequest request, UUID instructorId) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new AssignmentException("Submission not found with id: " + submissionId));
        
        Long assignmentId = submission.getAssignmentId();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentException("Assignment not found with id: " + assignmentId));
        
        if (submission.getStatus() != AssignmentSubmission.SubmissionStatus.SUBMITTED) {
            throw new AssignmentException("Can only grade submitted assignments");
        }
        
        if (request.getScore() == null || request.getScore() < 0) {
            throw new AssignmentException("Score must be a non-negative number");
        }
        
        if (request.getScore() > assignment.getMaxScore()) {
            throw new AssignmentException("Score cannot exceed max score of " + assignment.getMaxScore());
        }
        
        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setGradedBy(instructorId);
        submission.setGradedAt(LocalDateTime.now());
        submission.setStatus(AssignmentSubmission.SubmissionStatus.GRADED);
        
        submission = submissionRepository.save(submission);
        log.info("Graded submission: {} with score: {} by instructor: {}", submissionId, request.getScore(), instructorId);
        
        return mapToSubmissionResponse(submission);
    }

    /**
     * Get submission result (student view)
     */
    @Transactional(readOnly = true)
    public SubmissionResultResponse getSubmissionResult(Long assignmentId, UUID studentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentException("Assignment not found with id: " + assignmentId));
        
        validateEnrollment(studentId, assignment.getCourseId());
        
        AssignmentSubmission submission = submissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElse(null);
        
        if (submission == null) {
            throw new AssignmentException("No submission found for this assignment");
        }
        
        Integer percentage = null;
        if (submission.getScore() != null && assignment.getMaxScore() != null && assignment.getMaxScore() > 0) {
            percentage = (submission.getScore() * 100) / assignment.getMaxScore();
        }
        
        return SubmissionResultResponse.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignmentId())
                .assignmentTitle(assignment.getTitle())
                .score(submission.getScore())
                .maxScore(assignment.getMaxScore())
                .percentage(percentage)
                .feedback(submission.getFeedback())
                .status(submission.getStatus().name())
                .submittedAt(submission.getSubmittedAt())
                .gradedAt(submission.getGradedAt())
                .build();
    }

    /**
     * Get all submissions for a student
     */
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByStudent(UUID studentId) {
        List<AssignmentSubmission> submissions = submissionRepository.findByStudentId(studentId);
        return submissions.stream()
                .map(this::mapToSubmissionResponse)
                .collect(Collectors.toList());
    }

    // Helper methods

    private void validateEnrollment(UUID studentId, Long courseId) {
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new AccessDeniedException("Student is not enrolled in this course. Course enrollment is required.");
        }
    }

    private SubmissionResponse mapToSubmissionResponse(AssignmentSubmission submission) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignmentId())
                .studentId(submission.getStudentId())
                .submissionText(submission.getSubmissionText())
                .fileUrl(submission.getFileUrl())
                .status(submission.getStatus().name())
                .createdAt(submission.getCreatedAt())
                .submittedAt(submission.getSubmittedAt())
                .updatedAt(submission.getUpdatedAt())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .gradedBy(submission.getGradedBy())
                .gradedAt(submission.getGradedAt())
                .attemptNumber(submission.getAttemptNumber())
                .build();
    }
}