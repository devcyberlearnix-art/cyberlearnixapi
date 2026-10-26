package com.example.notification.service;

import com.example.notification.client.CourseClient;
import com.example.notification.dto.AnnouncementResponse;
import com.example.notification.dto.CreateAnnouncementRequest;
import com.example.notification.dto.DetailedAnnouncementResponse;
import com.example.notification.entity.Announcement;
import com.example.notification.repository.AnnouncementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private CourseClient courseClient;

    private AnnouncementService announcementService;
    private Long testCourseId;
    private UUID testUserId;
    private UUID testUserId2;

    @BeforeEach
    void setUp() {
        announcementService = new AnnouncementService(announcementRepository, courseClient);
        testCourseId = 1L;
        testUserId = UUID.randomUUID();
        testUserId2 = UUID.randomUUID();
    }

    @Test
    void createAnnouncement_WithValidData_SavesAnnouncement() {
        // Arrange
        CreateAnnouncementRequest request = CreateAnnouncementRequest.builder()
                .title("Test Announcement")
                .message("Test message")
                .sendToAll(false)
                .courseId(testCourseId)
                .createdBy(testUserId)
                .build();

        Announcement savedAnnouncement = Announcement.builder()
                .id(UUID.randomUUID())
                .title("Test Announcement")
                .message("Test message")
                .sendToAll(false)
                .courseId(testCourseId)
                .createdBy(testUserId)
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        when(announcementRepository.save(any(Announcement.class))).thenReturn(savedAnnouncement);

        // Act
        AnnouncementResponse response = announcementService.createAnnouncement(request);

        // Assert
        assertNotNull(response);
        assertEquals("Test Announcement", response.getTitle());
        assertEquals("Test message", response.getMessage());
        assertEquals(testCourseId, response.getCourseId());
        verify(announcementRepository, times(1)).save(any(Announcement.class));
    }

    @Test
    void getAnnouncementsDetailByCourseId_WithEnrolledStudents_ReturnsDetailedResponse() {
        // Arrange
        List<UUID> enrolledStudents = List.of(testUserId, testUserId2);
        when(courseClient.getEnrolledStudentsByCourseId(testCourseId)).thenReturn(enrolledStudents);

        Announcement announcement = Announcement.builder()
                .id(UUID.randomUUID())
                .title("Course Announcement")
                .message("Important update")
                .sendToAll(true)
                .courseId(testCourseId)
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        when(announcementRepository.findByCourseIdAndActiveTrueOrderByCreatedAtDesc(testCourseId))
                .thenReturn(List.of(announcement));

        // Act
        List<DetailedAnnouncementResponse> responses = announcementService.getAnnouncementsDetailByCourseId(testCourseId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        DetailedAnnouncementResponse response = responses.get(0);
        assertEquals("Course Announcement", response.getTitle());
        assertEquals(2, response.getTargetUsers().size());
        assertTrue(response.getTargetUsers().contains(testUserId.toString()));
        assertTrue(response.getTargetUsers().contains(testUserId2.toString()));
        verify(courseClient, times(1)).getEnrolledStudentsByCourseId(testCourseId);
    }

    @Test
    void getAnnouncementsDetailByCourseId_WithNoEnrolledStudents_ReturnsEmptyTargetUsers() {
        // Arrange
        when(courseClient.getEnrolledStudentsByCourseId(testCourseId)).thenReturn(List.of());

        Announcement announcement = Announcement.builder()
                .id(UUID.randomUUID())
                .title("Course Announcement")
                .message("Important update")
                .sendToAll(true)
                .courseId(testCourseId)
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        when(announcementRepository.findByCourseIdAndActiveTrueOrderByCreatedAtDesc(testCourseId))
                .thenReturn(List.of(announcement));

        // Act
        List<DetailedAnnouncementResponse> responses = announcementService.getAnnouncementsDetailByCourseId(testCourseId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        DetailedAnnouncementResponse response = responses.get(0);
        assertEquals(0, response.getTargetUsers().size());
        verify(courseClient, times(1)).getEnrolledStudentsByCourseId(testCourseId);
    }

    @Test
    void getAnnouncementsDetailByCourseId_WhenCourseServiceUnavailable_ReturnsEmptyTargetUsers() {
        // Arrange
        when(courseClient.getEnrolledStudentsByCourseId(testCourseId))
                .thenThrow(new RuntimeException("Course service unavailable"));

        Announcement announcement = Announcement.builder()
                .id(UUID.randomUUID())
                .title("Course Announcement")
                .message("Important update")
                .sendToAll(true)
                .courseId(testCourseId)
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        when(announcementRepository.findByCourseIdAndActiveTrueOrderByCreatedAtDesc(testCourseId))
                .thenReturn(List.of(announcement));

        // Act
        List<DetailedAnnouncementResponse> responses = announcementService.getAnnouncementsDetailByCourseId(testCourseId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        DetailedAnnouncementResponse response = responses.get(0);
        assertEquals(0, response.getTargetUsers().size());
        verify(courseClient, times(1)).getEnrolledStudentsByCourseId(testCourseId);
    }

    @Test
    void getAnnouncementsDetailByCourseId_WhenCourseIdIsNull_ReturnsEmptyTargetUsers() {
        // Arrange
        Announcement announcement = Announcement.builder()
                .id(UUID.randomUUID())
                .title("Course Announcement")
                .message("Important update")
                .sendToAll(true)
                .courseId(null)
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        when(announcementRepository.findByCourseIdAndActiveTrueOrderByCreatedAtDesc(null))
                .thenReturn(List.of(announcement));

        // Act
        List<DetailedAnnouncementResponse> responses = announcementService.getAnnouncementsDetailByCourseId(null);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        DetailedAnnouncementResponse response = responses.get(0);
        assertEquals(0, response.getTargetUsers().size());
        verify(courseClient, never()).getEnrolledStudentsByCourseId(any());
    }

    @Test
    void getAnnouncementsDetailByCourseId_WithSpecificUserIds_ReturnsThoseUsers() {
        // Arrange
        List<UUID> specificUsers = List.of(testUserId);
        Announcement announcement = Announcement.builder()
                .id(UUID.randomUUID())
                .title("Targeted Announcement")
                .message("For specific users")
                .sendToAll(false)
                .userIds(specificUsers)
                .courseId(testCourseId)
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        when(announcementRepository.findByCourseIdAndActiveTrueOrderByCreatedAtDesc(testCourseId))
                .thenReturn(List.of(announcement));

        // Act
        List<DetailedAnnouncementResponse> responses = announcementService.getAnnouncementsDetailByCourseId(testCourseId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        DetailedAnnouncementResponse response = responses.get(0);
        assertEquals(1, response.getTargetUsers().size());
        assertEquals(testUserId.toString(), response.getTargetUsers().get(0));
        verify(courseClient, never()).getEnrolledStudentsByCourseId(any());
    }

    @Test
    void getAnnouncementsDetailByCourseId_WithNoAnnouncements_ReturnsEmptyList() {
        // Arrange
        when(announcementRepository.findByCourseIdAndActiveTrueOrderByCreatedAtDesc(testCourseId))
                .thenReturn(new ArrayList<>());

        // Act
        List<DetailedAnnouncementResponse> responses = announcementService.getAnnouncementsDetailByCourseId(testCourseId);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(courseClient, never()).getEnrolledStudentsByCourseId(any());
    }
}
