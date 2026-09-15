package com.lms.courseservice.service;

import com.cyberlearnix.audit.AuditLogger;
import com.lms.courseservice.dto.CourseRequirementsDTO;
import com.lms.courseservice.dto.CourseRequirementsResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.CourseRequirements;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.CourseRequirementsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseRequirementsServiceTest {

    @Mock
    private CourseRequirementsRepository courseRequirementsRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CacheInvalidationService cacheInvalidationService;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private CourseRequirementsService courseRequirementsService;

    private Course testCourse;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testCourse = Course.builder()
                .id(1L)
                .title("Test Course")
                .instructorId(testUserId)
                .build();
    }

    @Test
    void createRequirement_Success() {
        // Arrange
        Long courseId = 1L;
        CourseRequirementsDTO dto = CourseRequirementsDTO.builder()
                .courseId(courseId)
                .requirementType("PRIOR_KNOWLEDGE")
                .requirementText("Basic programming knowledge")
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
        when(courseRequirementsRepository.save(any(CourseRequirements.class))).thenAnswer(invocation -> {
            CourseRequirements req = invocation.getArgument(0);
            req.setId(1L);
            return req;
        });
        doNothing().when(auditLogger).logSuccess(any(), any(), any(), any(), any(), any(), any());

        // Act
        CourseRequirementsResponse response = courseRequirementsService.createRequirement(courseId, dto, testUserId, "127.0.0.1");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Course requirement created successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("PRIOR_KNOWLEDGE", response.getData().getRequirementType());
        assertEquals("Basic programming knowledge", response.getData().getRequirementText());

        verify(courseRepository).findById(courseId);
        verify(courseRequirementsRepository).save(any(CourseRequirements.class));
    }

    @Test
    void createRequirement_CourseNotFound() {
        // Arrange
        Long courseId = 999L;
        CourseRequirementsDTO dto = CourseRequirementsDTO.builder()
                .courseId(courseId)
                .requirementType("PRIOR_KNOWLEDGE")
                .requirementText("Basic programming knowledge")
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            courseRequirementsService.createRequirement(courseId, dto, testUserId, "127.0.0.1"));

        verify(courseRepository).findById(courseId);
        verify(courseRequirementsRepository, never()).save(any());
    }

    @Test
    void createRequirement_Unauthorized() {
        // Arrange
        Long courseId = 1L;
        UUID differentUserId = UUID.randomUUID();
        CourseRequirementsDTO dto = CourseRequirementsDTO.builder()
                .courseId(courseId)
                .requirementType("PRIOR_KNOWLEDGE")
                .requirementText("Basic programming knowledge")
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            courseRequirementsService.createRequirement(courseId, dto, differentUserId, "127.0.0.1"));

        verify(courseRepository).findById(courseId);
        verify(courseRequirementsRepository, never()).save(any());
    }

    @Test
    void getRequirementsByCourseId_Success() {
        // Arrange
        Long courseId = 1L;
        CourseRequirements requirement = CourseRequirements.builder()
                .id(1L)
                .course(testCourse)
                .requirementType("PRIOR_KNOWLEDGE")
                .requirementText("Basic programming knowledge")
                .build();

        when(courseRequirementsRepository.findByCourseId(courseId)).thenReturn(List.of(requirement));

        // Act
        List<CourseRequirementsDTO> requirements = courseRequirementsService.getRequirementsByCourseId(courseId);

        // Assert
        assertNotNull(requirements);
        assertEquals(1, requirements.size());
        assertEquals("PRIOR_KNOWLEDGE", requirements.get(0).getRequirementType());
        assertEquals("Basic programming knowledge", requirements.get(0).getRequirementText());

        verify(courseRequirementsRepository).findByCourseId(courseId);
    }

    @Test
    void updateRequirement_Success() {
        // Arrange
        Long requirementId = 1L;
        CourseRequirementsDTO dto = CourseRequirementsDTO.builder()
                .requirementType("SOFTWARE")
                .requirementText("Updated requirement text")
                .build();

        CourseRequirements existingRequirement = CourseRequirements.builder()
                .id(requirementId)
                .course(testCourse)
                .requirementType("PRIOR_KNOWLEDGE")
                .requirementText("Original requirement text")
                .build();

        when(courseRequirementsRepository.findById(requirementId)).thenReturn(Optional.of(existingRequirement));
        when(courseRequirementsRepository.save(any(CourseRequirements.class))).thenReturn(existingRequirement);
        doNothing().when(auditLogger).logSuccess(any(), any(), any(), any(), any(), any(), any());

        // Act
        CourseRequirementsResponse response = courseRequirementsService.updateRequirement(requirementId, dto, testUserId, "127.0.0.1");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Course requirement updated successfully", response.getMessage());
        assertNotNull(response.getData());

        verify(courseRequirementsRepository).findById(requirementId);
        verify(courseRequirementsRepository).save(any(CourseRequirements.class));
    }

    @Test
    void deleteRequirement_Success() {
        // Arrange
        Long requirementId = 1L;
        CourseRequirements existingRequirement = CourseRequirements.builder()
                .id(requirementId)
                .course(testCourse)
                .requirementType("PRIOR_KNOWLEDGE")
                .requirementText("Original requirement text")
                .build();

        when(courseRequirementsRepository.findById(requirementId)).thenReturn(Optional.of(existingRequirement));
        doNothing().when(courseRequirementsRepository).delete(any(CourseRequirements.class));
        doNothing().when(auditLogger).logSuccess(any(), any(), any(), any(), any(), any(), any());

        // Act
        CourseRequirementsResponse response = courseRequirementsService.deleteRequirement(requirementId, testUserId, "127.0.0.1");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Course requirement deleted successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(requirementId, response.getData().getId());

        verify(courseRequirementsRepository).findById(requirementId);
        verify(courseRequirementsRepository).delete(any(CourseRequirements.class));
    }
}