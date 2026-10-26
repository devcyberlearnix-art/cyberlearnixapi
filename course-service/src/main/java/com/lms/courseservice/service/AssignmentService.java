package com.lms.courseservice.service;

import com.lms.courseservice.dto.AssignmentRequest;
import com.lms.courseservice.dto.AssignmentResponse;
import com.lms.courseservice.entity.Assignment;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.exception.AssignmentException;
import com.lms.courseservice.repository.AssignmentRepository;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;

    /**
     * Create a new assignment
     */
    @Transactional
    public AssignmentResponse createAssignment(AssignmentRequest request, UUID instructorId) {
        validateAssignmentRequest(request);
        
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new AssignmentException("Course not found with id: " + request.getCourseId()));
        
        validateInstructorOwnership(course, instructorId);
        
        if (request.getSectionId() != null) {
            Section section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new AssignmentException("Section not found with id: " + request.getSectionId()));
            
            if (!section.getCourse().getId().equals(request.getCourseId())) {
                throw new AssignmentException("Section does not belong to the specified course");
            }
        }
        
        Assignment assignment = Assignment.builder()
                .courseId(request.getCourseId())
                .sectionId(request.getSectionId())
                .title(request.getTitle())
                .description(request.getDescription())
                .instructions(request.getInstructions())
                .maxScore(request.getMaxScore())
                .dueDate(request.getDueDate())
                .status(Assignment.AssignmentStatus.DRAFT)
                .allowedFileTypes(request.getAllowedFileTypes())
                .maxFileSizeBytes(request.getMaxFileSizeBytes())
                .createdBy(instructorId)
                .build();
        
        assignment = assignmentRepository.save(assignment);
        log.info("Created assignment with id: {} for course: {} by instructor: {}", 
                assignment.getId(), assignment.getCourseId(), instructorId);
        
        return mapToAssignmentResponse(assignment);
    }

    /**
     * Update an existing assignment
     */
    @Transactional
    public AssignmentResponse updateAssignment(Long assignmentId, AssignmentRequest request, UUID instructorId) {
        validateAssignmentRequest(request);
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentException("Assignment not found with id: " + assignmentId));
        
        Long courseId = assignment.getCourseId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AssignmentException("Course not found with id: " + courseId));
        
        validateInstructorOwnership(course, instructorId);
        
        if (request.getSectionId() != null) {
            Section section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new AssignmentException("Section not found with id: " + request.getSectionId()));
            
            if (!section.getCourse().getId().equals(request.getCourseId())) {
                throw new AssignmentException("Section does not belong to the specified course");
            }
            assignment.setSectionId(request.getSectionId());
        }
        
        if (request.getTitle() != null) {
            assignment.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            assignment.setDescription(request.getDescription());
        }
        if (request.getInstructions() != null) {
            assignment.setInstructions(request.getInstructions());
        }
        if (request.getMaxScore() != null) {
            assignment.setMaxScore(request.getMaxScore());
        }
        if (request.getDueDate() != null) {
            assignment.setDueDate(request.getDueDate());
        }
        if (request.getAllowedFileTypes() != null) {
            assignment.setAllowedFileTypes(request.getAllowedFileTypes());
        }
        if (request.getMaxFileSizeBytes() != null) {
            assignment.setMaxFileSizeBytes(request.getMaxFileSizeBytes());
        }
        
        assignment = assignmentRepository.save(assignment);
        log.info("Updated assignment with id: {} by instructor: {}", assignmentId, instructorId);
        
        return mapToAssignmentResponse(assignment);
    }

    /**
     * Get assignment by ID
     */
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentException("Assignment not found with id: " + assignmentId));
        
        return mapToAssignmentResponse(assignment);
    }

    /**
     * Delete an assignment
     */
    @Transactional
    public void deleteAssignment(Long assignmentId, UUID instructorId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentException("Assignment not found with id: " + assignmentId));
        
        Long courseId = assignment.getCourseId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AssignmentException("Course not found with id: " + courseId));
        
        validateInstructorOwnership(course, instructorId);
        
        assignmentRepository.delete(assignment);
        log.info("Deleted assignment with id: {} by instructor: {}", assignmentId, instructorId);
    }

    /**
     * Publish an assignment
     */
    @Transactional
    public AssignmentResponse publishAssignment(Long assignmentId, UUID instructorId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentException("Assignment not found with id: " + assignmentId));
        
        Long courseId = assignment.getCourseId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AssignmentException("Course not found with id: " + courseId));
        
        validateInstructorOwnership(course, instructorId);
        
        assignment.setStatus(Assignment.AssignmentStatus.PUBLISHED);
        assignment = assignmentRepository.save(assignment);
        log.info("Published assignment with id: {} by instructor: {}", assignmentId, instructorId);
        
        return mapToAssignmentResponse(assignment);
    }

    /**
     * Unpublish an assignment
     */
    @Transactional
    public AssignmentResponse unpublishAssignment(Long assignmentId, UUID instructorId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentException("Assignment not found with id: " + assignmentId));
        
        Long courseId = assignment.getCourseId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AssignmentException("Course not found with id: " + courseId));
        
        validateInstructorOwnership(course, instructorId);
        
        assignment.setStatus(Assignment.AssignmentStatus.DRAFT);
        assignment = assignmentRepository.save(assignment);
        log.info("Unpublished assignment with id: {} by instructor: {}", assignmentId, instructorId);
        
        return mapToAssignmentResponse(assignment);
    }

    /**
     * List assignments for a course
     */
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AssignmentException("Course not found with id: " + courseId));
        
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        return assignments.stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * List assignments for a section
     */
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsBySection(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new AssignmentException("Section not found with id: " + sectionId));
        
        List<Assignment> assignments = assignmentRepository.findBySectionId(sectionId);
        return assignments.stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get published assignments for a course (student view)
     */
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getPublishedAssignmentsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AssignmentException("Course not found with id: " + courseId));
        
        List<Assignment> assignments = assignmentRepository.findByCourseIdAndStatus(
                courseId, Assignment.AssignmentStatus.PUBLISHED);
        return assignments.stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    // Helper methods

    private void validateAssignmentRequest(AssignmentRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new AssignmentException("Assignment title is required");
        }
        if (request.getCourseId() == null) {
            throw new AssignmentException("Course ID is required");
        }
        if (request.getMaxScore() == null || request.getMaxScore() <= 0) {
            throw new AssignmentException("Max score must be greater than 0");
        }
    }

    private void validateInstructorOwnership(Course course, UUID instructorId) {
        if (!course.getInstructorId().equals(instructorId)) {
            throw new AssignmentException("Instructor does not own this course");
        }
    }

    private AssignmentResponse mapToAssignmentResponse(Assignment assignment) {
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .courseId(assignment.getCourseId())
                .sectionId(assignment.getSectionId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .instructions(assignment.getInstructions())
                .maxScore(assignment.getMaxScore())
                .dueDate(assignment.getDueDate())
                .status(assignment.getStatus().name())
                .allowedFileTypes(assignment.getAllowedFileTypes())
                .maxFileSizeBytes(assignment.getMaxFileSizeBytes())
                .createdBy(assignment.getCreatedBy())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}