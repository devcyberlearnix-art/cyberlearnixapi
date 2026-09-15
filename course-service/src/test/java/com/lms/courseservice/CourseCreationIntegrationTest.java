package com.lms.courseservice;

import com.lms.courseservice.dto.CourseRequestDTO;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CourseCreationIntegrationTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    private UUID testInstructorId;

    @BeforeEach
    void setUp() {
        testInstructorId = UUID.randomUUID();
    }

    @Test
    void testCreateCourseWithValidData() {
        // Arrange
        CourseRequestDTO request = CourseRequestDTO.builder()
            .title("Test Course")
            .subtitle("Test Subtitle")
            .description("This is a test course description that meets the minimum length requirement")
            .category("Testing")
            .level("BEGINNER")
            .language("en")
            .price(new BigDecimal("29.99"))
            .thumbnail("https://example.com/thumbnail.jpg")
            .instructorId(testInstructorId)
            .status("DRAFT")
            .premium(false)
            .build();

        // Act
        Course createdCourse = courseService.createCourseFromDTO(request);

        // Assert
        assertNotNull(createdCourse);
        assertNotNull(createdCourse.getId());
        assertEquals("Test Course", createdCourse.getTitle());
        assertEquals("Test Subtitle", createdCourse.getSubtitle());
        assertEquals("This is a test course description that meets the minimum length requirement", createdCourse.getDescription());
        assertEquals("Testing", createdCourse.getCategory());
        assertEquals("BEGINNER", createdCourse.getLevel());
        assertEquals("en", createdCourse.getLanguage());
        assertEquals(new BigDecimal("29.99"), createdCourse.getPrice());
        assertEquals("https://example.com/thumbnail.jpg", createdCourse.getThumbnail());
        assertEquals(testInstructorId, createdCourse.getInstructorId());
        assertEquals("DRAFT", createdCourse.getStatus());
        assertFalse(createdCourse.getPremium());
        assertEquals(0L, createdCourse.getSearchCount());
        assertEquals(0L, createdCourse.getViewCount());
    }

    @Test
    void testCreateCourseWithDefaultValues() {
        // Arrange - create course with minimal required fields
        CourseRequestDTO request = CourseRequestDTO.builder()
            .title("Minimal Course")
            .description("This is a minimal course description that meets requirements")
            .category("Testing")
            .price(new BigDecimal("0.00"))
            .instructorId(testInstructorId)
            .build();

        // Act
        Course createdCourse = courseService.createCourseFromDTO(request);

        // Assert - verify defaults are set correctly
        assertNotNull(createdCourse);
        assertEquals("DRAFT", createdCourse.getStatus()); // Default status
        assertFalse(createdCourse.getPremium()); // Default premium
        assertEquals(0L, createdCourse.getSearchCount()); // Default search count
        assertEquals(0L, createdCourse.getViewCount()); // Default view count
        assertNull(createdCourse.getLevel()); // No default for level
        assertNull(createdCourse.getLanguage()); // No default for language
    }

    @Test
    void testCreateCoursePersistence() {
        // Arrange
        CourseRequestDTO request = CourseRequestDTO.builder()
            .title("Persistence Test Course")
            .description("This course tests database persistence")
            .category("Testing")
            .price(new BigDecimal("49.99"))
            .instructorId(testInstructorId)
            .status("PUBLISHED")
            .build();

        // Act
        Course createdCourse = courseService.createCourseFromDTO(request);
        Long courseId = createdCourse.getId();

        // Assert - verify course is persisted in database
        assertTrue(courseRepository.existsById(courseId));
        
        // Retrieve the course from database
        Course retrievedCourse = courseRepository.findById(courseId).orElse(null);
        assertNotNull(retrievedCourse);
        assertEquals("Persistence Test Course", retrievedCourse.getTitle());
        assertEquals("PUBLISHED", retrievedCourse.getStatus());
        assertEquals(testInstructorId, retrievedCourse.getInstructorId());
    }

    @Test
    void testCreateCourseWithDifferentStatuses() {
        // Test DRAFT status
        Course draftCourse = courseService.createCourseFromDTO(
            CourseRequestDTO.builder()
                .title("Draft Course")
                .description("Draft course description")
                .category("Testing")
                .price(new BigDecimal("10.00"))
                .instructorId(testInstructorId)
                .status("DRAFT")
                .build()
        );
        assertEquals("DRAFT", draftCourse.getStatus());

        // Test PUBLISHED status
        Course publishedCourse = courseService.createCourseFromDTO(
            CourseRequestDTO.builder()
                .title("Published Course")
                .description("Published course description")
                .category("Testing")
                .price(new BigDecimal("20.00"))
                .instructorId(testInstructorId)
                .status("PUBLISHED")
                .build()
        );
        assertEquals("PUBLISHED", publishedCourse.getStatus());

        // Test ARCHIVED status
        Course archivedCourse = courseService.createCourseFromDTO(
            CourseRequestDTO.builder()
                .title("Archived Course")
                .description("Archived course description")
                .category("Testing")
                .price(new BigDecimal("30.00"))
                .instructorId(testInstructorId)
                .status("ARCHIVED")
                .build()
        );
        assertEquals("ARCHIVED", archivedCourse.getStatus());
    }

    @Test
    void testCreateCourseWithPremiumFlag() {
        // Test premium course
        Course premiumCourse = courseService.createCourseFromDTO(
            CourseRequestDTO.builder()
                .title("Premium Course")
                .description("Premium course description")
                .category("Testing")
                .price(new BigDecimal("99.99"))
                .instructorId(testInstructorId)
                .premium(true)
                .build()
        );
        assertTrue(premiumCourse.getPremium());

        // Test free course
        Course freeCourse = courseService.createCourseFromDTO(
            CourseRequestDTO.builder()
                .title("Free Course")
                .description("Free course description")
                .category("Testing")
                .price(new BigDecimal("0.00"))
                .instructorId(testInstructorId)
                .premium(false)
                .build()
        );
        assertFalse(freeCourse.getPremium());
    }

    @Test
    void testCreateCourseWithInstructorAssociation() {
        // Arrange
        UUID specificInstructorId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        
        CourseRequestDTO request = CourseRequestDTO.builder()
            .title("Instructor Course")
            .description("Course with specific instructor")
            .category("Testing")
            .price(new BigDecimal("39.99"))
            .instructorId(specificInstructorId)
            .build();

        // Act
        Course createdCourse = courseService.createCourseFromDTO(request);

        // Assert - verify instructor association
        assertEquals(specificInstructorId, createdCourse.getInstructorId());
    }

    @Test
    void testCourseRetrievalAfterCreation() {
        // Arrange
        CourseRequestDTO request = CourseRequestDTO.builder()
            .title("Retrieval Test Course")
            .description("Course for testing retrieval")
            .category("Testing")
            .price(new BigDecimal("59.99"))
            .instructorId(testInstructorId)
            .build();

        // Act
        Course createdCourse = courseService.createCourseFromDTO(request);
        Long courseId = createdCourse.getId();

        // Retrieve the course using service method
        Course retrievedCourse = courseService.getCourseById(courseId);

        // Assert - verify all fields match
        assertNotNull(retrievedCourse);
        assertEquals(courseId, retrievedCourse.getId());
        assertEquals("Retrieval Test Course", retrievedCourse.getTitle());
        assertEquals("Course for testing retrieval", retrievedCourse.getDescription());
        assertEquals("Testing", retrievedCourse.getCategory());
        assertEquals(new BigDecimal("59.99"), retrievedCourse.getPrice());
        assertEquals(testInstructorId, retrievedCourse.getInstructorId());
    }
}