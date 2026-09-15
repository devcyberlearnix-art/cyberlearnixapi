package com.example.instructorservice;

import com.example.instructorservice.dto.CourseRequestDTO;
import com.example.instructorservice.entity.Course;
import com.example.instructorservice.entity.Instructor;
import com.example.instructorservice.repository.CourseRepository;
import com.example.instructorservice.repository.InstructorRepository;
import com.example.instructorservice.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CourseCreationIntegrationTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    private UUID testInstructorId;

    @BeforeEach
    void setUp() {
        testInstructorId = UUID.randomUUID();
        
        // Create test instructor
        Instructor instructor = new Instructor();
        instructor.setUserId(testInstructorId);
        instructor.setName("Test Instructor");
        instructor.setEmail("test@example.com");
        instructor.setVerified(true);
        instructorRepository.save(instructor);
    }

    @Test
    void testCreateCourseWithValidData() {
        // Arrange
        CourseRequestDTO request = new CourseRequestDTO();
        request.setTitle("Test Course");
        request.setSubtitle("Test Subtitle");
        request.setDescription("This is a test course description that meets the minimum length requirement");
        request.setPrice(29.99);
        request.setCategory("Testing");
        request.setStatus(Course.CourseStatus.DRAFT);
        request.setTags(new java.util.ArrayList<>(List.of("testing", "integration")));
        request.setThumbnailUrl("https://example.com/thumbnail.jpg");
        request.setPreviewVideoUrl("https://example.com/preview.mp4");
        request.setLevel("BEGINNER");
        request.setLanguage("en");

        // Act
        var response = courseService.createCourse(testInstructorId, request, "Bearer test-token");

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("Test Course", response.getData().getCore().getTitle());
        assertEquals("Test Subtitle", response.getData().getCore().getSubtitle());
        assertEquals("Testing", response.getData().getCore().getCategory());
        assertEquals("BEGINNER", response.getData().getCore().getLevel());
        assertEquals("en", response.getData().getCore().getLanguage());
    }

    @Test
    void testCreateCourseWithDefaultValues() {
        // Arrange - create course with minimal required fields
        CourseRequestDTO request = new CourseRequestDTO();
        request.setTitle("Minimal Course");
        request.setDescription("This is a minimal course description that meets requirements");
        request.setPrice(0.00);
        request.setCategory("Testing");
        // Don't set status - should default to DRAFT

        // Act
        var response = courseService.createCourse(testInstructorId, request, "Bearer test-token");

        // Assert - verify defaults are set correctly
        assertNotNull(response);
        assertEquals(Course.CourseStatus.DRAFT, response.getData().getStatus().getStatus());
        assertEquals("BEGINNER", response.getData().getCore().getLevel()); // Default level
        assertEquals("en", response.getData().getCore().getLanguage()); // Default language
    }

    @Test
    void testCreateCoursePersistence() {
        // Arrange
        CourseRequestDTO request = new CourseRequestDTO();
        request.setTitle("Persistence Test Course");
        request.setDescription("This course tests database persistence");
        request.setPrice(49.99);
        request.setCategory("Testing");
        request.setStatus(Course.CourseStatus.PUBLISHED);

        // Act
        var response = courseService.createCourse(testInstructorId, request, "Bearer test-token");
        Long courseId = response.getData().getIdentity().getCourseId();

        // Assert - verify course is persisted in database
        assertTrue(courseRepository.existsById(courseId));
        
        // Retrieve the course from database
        Course retrievedCourse = courseRepository.findById(courseId).orElse(null);
        assertNotNull(retrievedCourse);
        assertEquals("Persistence Test Course", retrievedCourse.getTitle());
        assertEquals(Course.CourseStatus.PUBLISHED, retrievedCourse.getStatus());
    }

    @Test
    void testCreateCourseWithDifferentStatuses() {
        // Test DRAFT status
        CourseRequestDTO draftRequest = new CourseRequestDTO();
        draftRequest.setTitle("Draft Course");
        draftRequest.setDescription("Draft course description");
        draftRequest.setPrice(10.00);
        draftRequest.setCategory("Testing");
        draftRequest.setStatus(Course.CourseStatus.DRAFT);
        
        var draftResponse = courseService.createCourse(testInstructorId, draftRequest, "Bearer test-token");
        assertEquals(Course.CourseStatus.DRAFT, draftResponse.getData().getStatus().getStatus());

        // Test PUBLISHED status
        CourseRequestDTO publishedRequest = new CourseRequestDTO();
        publishedRequest.setTitle("Published Course");
        publishedRequest.setDescription("Published course description");
        publishedRequest.setPrice(20.00);
        publishedRequest.setCategory("Testing");
        publishedRequest.setStatus(Course.CourseStatus.PUBLISHED);
        
        var publishedResponse = courseService.createCourse(testInstructorId, publishedRequest, "Bearer test-token");
        assertEquals(Course.CourseStatus.PUBLISHED, publishedResponse.getData().getStatus().getStatus());

        // Test ARCHIVED status
        CourseRequestDTO archivedRequest = new CourseRequestDTO();
        archivedRequest.setTitle("Archived Course");
        archivedRequest.setDescription("Archived course description");
        archivedRequest.setPrice(30.00);
        archivedRequest.setCategory("Testing");
        archivedRequest.setStatus(Course.CourseStatus.ARCHIVED);
        
        var archivedResponse = courseService.createCourse(testInstructorId, archivedRequest, "Bearer test-token");
        assertEquals(Course.CourseStatus.ARCHIVED, archivedResponse.getData().getStatus().getStatus());
    }

    @Test
    void testCreateCourseWithTags() {
        // Arrange
        CourseRequestDTO request = new CourseRequestDTO();
        request.setTitle("Tagged Course");
        request.setDescription("Course with tags");
        request.setPrice(39.99);
        request.setCategory("Testing");
        request.setTags(new java.util.ArrayList<>(List.of("java", "spring", "testing", "integration")));

        // Act
        var response = courseService.createCourse(testInstructorId, request, "Bearer test-token");

        // Assert - verify tags are preserved
        assertNotNull(response);
        assertNotNull(response.getData().getCore().getTags());
        assertEquals(4, response.getData().getCore().getTags().size());
        assertTrue(response.getData().getCore().getTags().contains("java"));
        assertTrue(response.getData().getCore().getTags().contains("spring"));
    }

    @Test
    void testCreateCourseWithInstructorAssociation() {
        // Arrange
        CourseRequestDTO request = new CourseRequestDTO();
        request.setTitle("Instructor Course");
        request.setDescription("Course with specific instructor");
        request.setPrice(39.99);
        request.setCategory("Testing");

        // Act
        var response = courseService.createCourse(testInstructorId, request, "Bearer test-token");

        // Assert - verify instructor association
        assertNotNull(response);
        assertNotNull(response.getData().getInstructor());
        assertEquals(testInstructorId, response.getData().getInstructor().getInstructorId());
        assertEquals("Test Instructor", response.getData().getInstructor().getName());
    }

    @Test
    void testCourseRetrievalAfterCreation() {
        // Arrange
        CourseRequestDTO request = new CourseRequestDTO();
        request.setTitle("Retrieval Test Course");
        request.setDescription("Course for testing retrieval");
        request.setPrice(59.99);
        request.setCategory("Testing");

        // Act
        var response = courseService.createCourse(testInstructorId, request, "Bearer test-token");
        Long courseId = response.getData().getIdentity().getCourseId();

        // Retrieve the course using service method
        var retrievedResponse = courseService.getCourseById(testInstructorId, courseId);

        // Assert - verify all fields match
        assertNotNull(retrievedResponse);
        assertEquals(courseId, retrievedResponse.getCourseId());
        assertEquals("Retrieval Test Course", retrievedResponse.getTitle());
        assertEquals("Course for testing retrieval", retrievedResponse.getDescription());
        assertEquals("Testing", retrievedResponse.getCategory());
        assertEquals(59.99, retrievedResponse.getPrice());
    }

    @Test
    void testGetCoursesByInstructor() {
        // Arrange - create multiple courses for the same instructor
        for (int i = 1; i <= 3; i++) {
            CourseRequestDTO request = new CourseRequestDTO();
            request.setTitle("Course " + i);
            request.setDescription("Description for course " + i);
            request.setPrice(10.00 * i);
            request.setCategory("Testing");
            courseService.createCourse(testInstructorId, request, "Bearer test-token");
        }

        // Act
        var courses = courseService.getCoursesByInstructor(testInstructorId);

        // Assert
        assertNotNull(courses);
        assertEquals(3, courses.size());
        assertTrue(courses.stream().anyMatch(c -> c.getTitle().equals("Course 1")));
        assertTrue(courses.stream().anyMatch(c -> c.getTitle().equals("Course 2")));
        assertTrue(courses.stream().anyMatch(c -> c.getTitle().equals("Course 3")));
    }

    @Test
    void testUpdateCourse() {
        // Arrange - create a course first
        CourseRequestDTO createRequest = new CourseRequestDTO();
        createRequest.setTitle("Original Title");
        createRequest.setDescription("Original description");
        createRequest.setPrice(29.99);
        createRequest.setCategory("Testing");
        
        var createResponse = courseService.createCourse(testInstructorId, createRequest, "Bearer test-token");
        Long courseId = createResponse.getData().getIdentity().getCourseId();

        // Act - update the course
        CourseRequestDTO updateRequest = new CourseRequestDTO();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated description");
        updateRequest.setPrice(49.99);
        updateRequest.setCategory("Updated Category");
        updateRequest.setLevel("ADVANCED");
        updateRequest.setLanguage("es");

        var updatedResponse = courseService.updateCourse(testInstructorId, courseId, updateRequest);

        // Assert - verify updates
        assertNotNull(updatedResponse);
        assertEquals("Updated Title", updatedResponse.getTitle());
        assertEquals("Updated description", updatedResponse.getDescription());
        assertEquals(49.99, updatedResponse.getPrice());
        assertEquals("Updated Category", updatedResponse.getCategory());
    }

    @Test
    void testSlugGeneration() {
        // Arrange
        CourseRequestDTO request = new CourseRequestDTO();
        request.setTitle("Test Course With Special Characters!@#$");
        request.setDescription("Test description");
        request.setPrice(19.99);
        request.setCategory("Testing");

        // Act
        var response = courseService.createCourse(testInstructorId, request, "Bearer test-token");

        // Assert - verify slug is generated correctly
        assertNotNull(response);
        assertNotNull(response.getData().getIdentity().getSlug());
        assertTrue(response.getData().getIdentity().getSlug().matches("[a-z0-9-]+"));
        assertFalse(response.getData().getIdentity().getSlug().contains("!"));
        assertFalse(response.getData().getIdentity().getSlug().contains("@"));
    }
}