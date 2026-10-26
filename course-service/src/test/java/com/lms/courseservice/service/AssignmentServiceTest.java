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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private SectionRepository sectionRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private UUID testInstructorId;
    private Long testCourseId;
    private Long testSectionId;
    private Long testAssignmentId;
    private Course testCourse;
    private Section testSection;
    private Assignment testAssignment;

    @BeforeEach
    void setUp() {
        testInstructorId = UUID.randomUUID();
        testCourseId = 1L;
        testSectionId = 100L;
        testAssignmentId = 10L;

        testSection = Section.builder()
                .id(testSectionId)
                .title("Test Section")
                .orderIndex(1)
                .build();

        testCourse = Course.builder()
                .id(testCourseId)
                .title("Test Course")
                .instructorId(testInstructorId)
                .sections(new ArrayList<>(List.of(testSection)))
                .build();

        testSection.setCourse(testCourse);

        testAssignment = Assignment.builder()
                .id(testAssignmentId)
                .courseId(testCourseId)
                .sectionId(null)
                .title("Test Assignment")
                .description("Test Description")
                .instructions("Test Instructions")
                .maxScore(100)
                .status(Assignment.AssignmentStatus.DRAFT)
                .createdBy(testInstructorId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createAssignment_ValidRequest_CreatesAssignment() {
        AssignmentRequest request = AssignmentRequest.builder()
                .courseId(testCourseId)
                .sectionId(testSectionId)
                .title("New Assignment")
                .description("New Description")
                .instructions("New Instructions")
                .maxScore(100)
                .dueDate(LocalDateTime.now().plusDays(7))
                .build();

        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.of(testSection));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> {
            Assignment saved = invocation.getArgument(0);
            saved.setId(testAssignmentId);
            return saved;
        });

        AssignmentResponse response = assignmentService.createAssignment(request, testInstructorId);

        assertNotNull(response);
        assertEquals(testAssignmentId, response.getId());
        assertEquals("New Assignment", response.getTitle());
        assertEquals(100, response.getMaxScore());
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
    }

    @Test
    void createAssignment_InvalidCourse_ThrowsException() {
        AssignmentRequest request = AssignmentRequest.builder()
                .courseId(testCourseId)
                .title("New Assignment")
                .maxScore(100)
                .build();

        when(courseRepository.findById(testCourseId)).thenReturn(Optional.empty());

        assertThrows(AssignmentException.class, () -> assignmentService.createAssignment(request, testInstructorId));
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void createAssignment_UnauthorizedInstructor_ThrowsException() {
        UUID otherInstructorId = UUID.randomUUID();
        AssignmentRequest request = AssignmentRequest.builder()
                .courseId(testCourseId)
                .title("New Assignment")
                .maxScore(100)
                .build();

        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));

        assertThrows(AssignmentException.class, () -> assignmentService.createAssignment(request, otherInstructorId));
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void createAssignment_SectionCourseMismatch_ThrowsException() {
        Long otherCourseId = 2L;
        Section otherSection = Section.builder()
                .id(testSectionId)
                .title("Other Section")
                .course(Course.builder().id(otherCourseId).build())
                .build();

        AssignmentRequest request = AssignmentRequest.builder()
                .courseId(testCourseId)
                .sectionId(testSectionId)
                .title("New Assignment")
                .maxScore(100)
                .build();

        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.of(otherSection));

        assertThrows(AssignmentException.class, () -> assignmentService.createAssignment(request, testInstructorId));
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void createAssignment_InvalidMaxScore_ThrowsException() {
        AssignmentRequest request = AssignmentRequest.builder()
                .courseId(testCourseId)
                .title("New Assignment")
                .maxScore(0)
                .build();

        assertThrows(AssignmentException.class, () -> assignmentService.createAssignment(request, testInstructorId));
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void createAssignment_MissingTitle_ThrowsException() {
        AssignmentRequest request = AssignmentRequest.builder()
                .courseId(testCourseId)
                .title("")
                .maxScore(100)
                .build();

        assertThrows(AssignmentException.class, () -> assignmentService.createAssignment(request, testInstructorId));
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void updateAssignment_ValidRequest_UpdatesAssignment() {
        AssignmentRequest request = AssignmentRequest.builder()
                .courseId(testCourseId)
                .title("Updated Title")
                .maxScore(150)
                .build();

        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> {
            Assignment saved = invocation.getArgument(0);
            return saved;
        });

        AssignmentResponse response = assignmentService.updateAssignment(testAssignmentId, request, testInstructorId);

        assertNotNull(response);
        assertEquals("Updated Title", response.getTitle());
        assertEquals(150, response.getMaxScore());
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
    }

    @Test
    void updateAssignment_NotFound_ThrowsException() {
        AssignmentRequest request = AssignmentRequest.builder()
                .courseId(testCourseId)
                .title("Updated Title")
                .maxScore(100)
                .build();

        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.empty());

        assertThrows(AssignmentException.class, () -> assignmentService.updateAssignment(testAssignmentId, request, testInstructorId));
        verify(courseRepository, never()).findById(any(Long.class));
    }

    @Test
    void getAssignment_ValidId_ReturnsAssignment() {
        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));

        AssignmentResponse response = assignmentService.getAssignment(testAssignmentId);

        assertNotNull(response);
        assertEquals(testAssignmentId, response.getId());
        assertEquals("Test Assignment", response.getTitle());
    }

    @Test
    void getAssignment_NotFound_ThrowsException() {
        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.empty());

        assertThrows(AssignmentException.class, () -> assignmentService.getAssignment(testAssignmentId));
    }

    @Test
    void deleteAssignment_ValidId_DeletesAssignment() {
        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));

        assignmentService.deleteAssignment(testAssignmentId, testInstructorId);

        verify(assignmentRepository, times(1)).delete(testAssignment);
    }

    @Test
    void deleteAssignment_Unauthorized_ThrowsException() {
        UUID otherInstructorId = UUID.randomUUID();
        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));

        assertThrows(AssignmentException.class, () -> assignmentService.deleteAssignment(testAssignmentId, otherInstructorId));
        verify(assignmentRepository, never()).delete(any(Assignment.class));
    }

    @Test
    void publishAssignment_ValidId_PublishesAssignment() {
        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);

        AssignmentResponse response = assignmentService.publishAssignment(testAssignmentId, testInstructorId);

        assertNotNull(response);
        assertEquals("PUBLISHED", response.getStatus());
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
    }

    @Test
    void unpublishAssignment_ValidId_UnpublishesAssignment() {
        when(assignmentRepository.findById(testAssignmentId)).thenReturn(Optional.of(testAssignment));
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);

        AssignmentResponse response = assignmentService.unpublishAssignment(testAssignmentId, testInstructorId);

        assertNotNull(response);
        assertEquals("DRAFT", response.getStatus());
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
    }

    @Test
    void getAssignmentsByCourse_ValidCourse_ReturnsAssignments() {
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(assignmentRepository.findByCourseId(testCourseId)).thenReturn(List.of(testAssignment));

        List<AssignmentResponse> response = assignmentService.getAssignmentsByCourse(testCourseId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(testAssignmentId, response.get(0).getId());
    }

    @Test
    void getAssignmentsBySection_ValidSection_ReturnsAssignments() {
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.of(testSection));
        when(assignmentRepository.findBySectionId(testSectionId)).thenReturn(List.of(testAssignment));

        List<AssignmentResponse> response = assignmentService.getAssignmentsBySection(testSectionId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(testAssignmentId, response.get(0).getId());
    }

    @Test
    void getPublishedAssignmentsByCourse_ValidCourse_ReturnsPublishedAssignments() {
        testAssignment.setStatus(Assignment.AssignmentStatus.PUBLISHED);
        
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(assignmentRepository.findByCourseIdAndStatus(testCourseId, Assignment.AssignmentStatus.PUBLISHED))
                .thenReturn(List.of(testAssignment));

        List<AssignmentResponse> response = assignmentService.getPublishedAssignmentsByCourse(testCourseId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("PUBLISHED", response.get(0).getStatus());
    }
}