package com.lms.courseservice.service;

import com.lms.courseservice.dto.LiveClassAttendanceResponse;
import com.lms.courseservice.dto.LiveClassAttendanceSummaryResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Enrollment;
import com.lms.courseservice.entity.LiveClass;
import com.lms.courseservice.entity.LiveClassAttendance;
import com.lms.courseservice.exception.LiveClassAttendanceException;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.EnrollmentRepository;
import com.lms.courseservice.repository.LiveClassAttendanceRepository;
import com.lms.courseservice.repository.LiveClassRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiveClassAttendanceServiceTest {

    @Mock
    private LiveClassAttendanceRepository attendanceRepository;

    @Mock
    private LiveClassRepository liveClassRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private LiveClassAttendanceService attendanceService;

    private UUID studentId;
    private UUID instructorId;
    private Long courseId;
    private Long liveClassId;
    private Course course;
    private Enrollment enrollment;
    private LiveClass liveClass;
    private LiveClassAttendance attendance;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        instructorId = UUID.randomUUID();
        courseId = 1L;
        liveClassId = 10L;

        course = Course.builder()
                .id(courseId)
                .title("Java Programming")
                .instructorId(instructorId)
                .build();

        enrollment = Enrollment.builder()
                .id(1L)
                .studentId(studentId)
                .courseId(courseId)
                .studentName("John Doe")
                .status("ACTIVE")
                .build();

        liveClass = LiveClass.builder()
                .id(liveClassId)
                .courseId(courseId)
                .instructorId(instructorId)
                .title("Spring Boot Live Session")
                .status(LiveClass.LiveClassStatus.SCHEDULED)
                .scheduledStartTime(LocalDateTime.now().plusDays(1))
                .scheduledEndTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .build();

        attendance = LiveClassAttendance.builder()
                .id(1L)
                .liveClassId(liveClassId)
                .studentId(studentId)
                .joinedAt(LocalDateTime.now().minusMinutes(30))
                .leftAt(LocalDateTime.now())
                .durationSeconds(1800L)
                .status(LiveClassAttendance.AttendanceStatus.ATTENDED)
                .build();
    }

    // ==================== JOIN TESTS ====================

    @Test
    void joinLiveClass_ValidEnrolledStudent_Success() {
        LiveClassAttendance newAttendance = LiveClassAttendance.builder()
                .id(1L)
                .liveClassId(liveClassId)
                .studentId(studentId)
                .joinedAt(LocalDateTime.now())
                .durationSeconds(0L)
                .status(LiveClassAttendance.AttendanceStatus.JOINED)
                .build();

        when(liveClassRepository.findById(liveClassId)).thenReturn(Optional.of(liveClass));
        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.of(enrollment));
        when(attendanceRepository.existsByLiveClassIdAndStudentId(liveClassId, studentId))
                .thenReturn(false);
        when(attendanceRepository.save(any(LiveClassAttendance.class))).thenReturn(newAttendance);

        LiveClassAttendanceResponse response = attendanceService.joinLiveClass(liveClassId, studentId);

        assertNotNull(response);
        assertEquals(liveClassId, response.getLiveClassId());
        assertEquals(studentId, response.getStudentId());
        assertEquals("JOINED", response.getStatus());
        assertNotNull(response.getJoinedAt());
        assertEquals(0L, response.getDurationSeconds());
        verify(attendanceRepository).save(any(LiveClassAttendance.class));
    }

    @Test
    void joinLiveClass_NotEnrolled_ThrowsException() {
        when(liveClassRepository.findById(liveClassId)).thenReturn(Optional.of(liveClass));
        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.empty());

        assertThrows(LiveClassAttendanceException.class, 
                () -> attendanceService.joinLiveClass(liveClassId, studentId));
    }

    @Test
    void joinLiveClass_LiveClassNotFound_ThrowsException() {
        when(liveClassRepository.findById(liveClassId)).thenReturn(Optional.empty());

        assertThrows(LiveClassAttendanceException.class, 
                () -> attendanceService.joinLiveClass(liveClassId, studentId));
    }

    @Test
    void joinLiveClass_CancelledClass_ThrowsException() {
        liveClass.setStatus(LiveClass.LiveClassStatus.CANCELLED);
        when(liveClassRepository.findById(liveClassId)).thenReturn(Optional.of(liveClass));
        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.of(enrollment));

        assertThrows(LiveClassAttendanceException.class, 
                () -> attendanceService.joinLiveClass(liveClassId, studentId));
    }

    @Test
    void joinLiveClass_CompletedClass_ThrowsException() {
        liveClass.setStatus(LiveClass.LiveClassStatus.COMPLETED);
        when(liveClassRepository.findById(liveClassId)).thenReturn(Optional.of(liveClass));
        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.of(enrollment));

        assertThrows(LiveClassAttendanceException.class, 
                () -> attendanceService.joinLiveClass(liveClassId, studentId));
    }

    @Test
    void joinLiveClass_DuplicateJoin_ThrowsException() {
        when(liveClassRepository.findById(liveClassId)).thenReturn(Optional.of(liveClass));
        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.of(enrollment));
        when(attendanceRepository.existsByLiveClassIdAndStudentId(liveClassId, studentId))
                .thenReturn(true);

        assertThrows(LiveClassAttendanceException.class, 
                () -> attendanceService.joinLiveClass(liveClassId, studentId));
    }

    // ==================== LEAVE TESTS ====================

    @Test
    void leaveLiveClass_ValidLeave_Success() {
        attendance.setStatus(LiveClassAttendance.AttendanceStatus.JOINED);
        attendance.setLeftAt(null);
        attendance.setDurationSeconds(0L);

        when(attendanceRepository.findByLiveClassIdAndStudentId(liveClassId, studentId))
                .thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(LiveClassAttendance.class))).thenReturn(attendance);

        LiveClassAttendanceResponse response = attendanceService.leaveLiveClass(liveClassId, studentId);

        assertNotNull(response);
        assertEquals("ATTENDED", response.getStatus());
        assertNotNull(response.getLeftAt());
        assertTrue(response.getDurationSeconds() > 0);
        verify(attendanceRepository).save(attendance);
    }

    @Test
    void leaveLiveClass_AttendanceNotFound_ThrowsException() {
        when(attendanceRepository.findByLiveClassIdAndStudentId(liveClassId, studentId))
                .thenReturn(Optional.empty());

        assertThrows(LiveClassAttendanceException.class, 
                () -> attendanceService.leaveLiveClass(liveClassId, studentId));
    }

    @Test
    void leaveLiveClass_WrongStudent_ThrowsException() {
        UUID otherStudentId = UUID.randomUUID();
        attendance.setStudentId(otherStudentId);

        when(attendanceRepository.findByLiveClassIdAndStudentId(liveClassId, studentId))
                .thenReturn(Optional.of(attendance));

        assertThrows(LiveClassAttendanceException.class, 
                () -> attendanceService.leaveLiveClass(liveClassId, studentId));
    }

    @Test
    void leaveLiveClass_AlreadyLeft_ThrowsException() {
        attendance.setStatus(LiveClassAttendance.AttendanceStatus.ATTENDED);

        when(attendanceRepository.findByLiveClassIdAndStudentId(liveClassId, studentId))
                .thenReturn(Optional.of(attendance));

        assertThrows(LiveClassAttendanceException.class, 
                () -> attendanceService.leaveLiveClass(liveClassId, studentId));
    }

    @Test
    void leaveLiveClass_DurationCalculatedCorrectly() {
        attendance.setStatus(LiveClassAttendance.AttendanceStatus.JOINED);
        attendance.setJoinedAt(LocalDateTime.now().minusMinutes(30));
        attendance.setLeftAt(null);
        attendance.setDurationSeconds(0L);

        when(attendanceRepository.findByLiveClassIdAndStudentId(liveClassId, studentId))
                .thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(LiveClassAttendance.class))).thenReturn(attendance);

        attendanceService.leaveLiveClass(liveClassId, studentId);

        assertEquals(1800L, attendance.getDurationSeconds()); // 30 minutes = 1800 seconds
    }

    // ==================== STUDENT ACCESS TESTS ====================

    @Test
    void getMyAttendance_StudentCanViewOwnAttendance_Success() {
        when(attendanceRepository.findByLiveClassIdAndStudentId(liveClassId, studentId))
                .thenReturn(Optional.of(attendance));

        LiveClassAttendanceResponse response = attendanceService.getMyAttendance(liveClassId, studentId);

        assertNotNull(response);
        assertEquals(liveClassId, response.getLiveClassId());
        assertEquals(studentId, response.getStudentId());
    }

    @Test
    void getMyAttendance_NotFound_ThrowsException() {
        when(attendanceRepository.findByLiveClassIdAndStudentId(liveClassId, studentId))
                .thenReturn(Optional.empty());

        assertThrows(LiveClassAttendanceException.class, 
                () -> attendanceService.getMyAttendance(liveClassId, studentId));
    }

    @Test
    void getMyAttendance_WrongStudent_ThrowsException() {
        UUID otherStudentId = UUID.randomUUID();
        attendance.setStudentId(otherStudentId);

        when(attendanceRepository.findByLiveClassIdAndStudentId(liveClassId, studentId))
                .thenReturn(Optional.of(attendance));

        assertThrows(LiveClassAttendanceException.class, 
                () -> attendanceService.getMyAttendance(liveClassId, studentId));
    }

    // ==================== INSTRUCTOR ACCESS TESTS ====================

    @Test
    void getAttendanceByLiveClass_CourseOwnerCanView_Success() {
        when(liveClassRepository.findById(liveClassId)).thenReturn(Optional.of(liveClass));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(attendanceRepository.findByLiveClassId(liveClassId))
                .thenReturn(List.of(attendance));

        List<LiveClassAttendanceResponse> response = attendanceService.getAttendanceByLiveClass(liveClassId, instructorId);

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void getAttendanceByLiveClass_WrongInstructor_ThrowsException() {
        UUID otherInstructorId = UUID.randomUUID();
        Course otherCourse = Course.builder()
                .id(courseId)
                .instructorId(otherInstructorId)
                .build();

        when(liveClassRepository.findById(liveClassId)).thenReturn(Optional.of(liveClass));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(otherCourse));

        assertThrows(LiveClassAttendanceException.class, 
                () -> attendanceService.getAttendanceByLiveClass(liveClassId, instructorId));
    }

    @Test
    void getAttendanceSummary_CorrectValues_Success() {
        when(liveClassRepository.findById(liveClassId)).thenReturn(Optional.of(liveClass));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.countByCourseId(courseId)).thenReturn(10L);
        when(attendanceRepository.countByLiveClassId(liveClassId)).thenReturn(8L);
        when(attendanceRepository.countCurrentlyJoined(liveClassId)).thenReturn(2L);
        when(attendanceRepository.countAttended(liveClassId)).thenReturn(6L);
        when(attendanceRepository.countAbsent(liveClassId)).thenReturn(0L);
        when(attendanceRepository.sumDurationSeconds(liveClassId)).thenReturn(5400L); // 6 attended * 900 seconds average

        LiveClassAttendanceSummaryResponse response = attendanceService.getAttendanceSummary(liveClassId, instructorId);

        assertNotNull(response);
        assertEquals(liveClassId, response.getLiveClassId());
        assertEquals(10L, response.getTotalEnrolledStudents());
        assertEquals(8L, response.getTotalJoined());
        assertEquals(6L, response.getTotalAttended());
        assertEquals(2L, response.getTotalCurrentlyJoined());
        assertEquals(0L, response.getTotalAbsent());
        assertEquals(60.0, response.getAttendancePercentage());
        assertEquals(900L, response.getAverageDurationSeconds());
    }

    @Test
    void getAttendanceSummary_CourseOwnerCanView_Success() {
        when(liveClassRepository.findById(liveClassId)).thenReturn(Optional.of(liveClass));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.countByCourseId(courseId)).thenReturn(10L);
        when(attendanceRepository.countByLiveClassId(liveClassId)).thenReturn(0L);
        when(attendanceRepository.countCurrentlyJoined(liveClassId)).thenReturn(0L);
        when(attendanceRepository.countAttended(liveClassId)).thenReturn(0L);
        when(attendanceRepository.countAbsent(liveClassId)).thenReturn(0L);
        when(attendanceRepository.sumDurationSeconds(liveClassId)).thenReturn(null);

        LiveClassAttendanceSummaryResponse response = attendanceService.getAttendanceSummary(liveClassId, instructorId);

        assertNotNull(response);
        assertEquals(liveClassId, response.getLiveClassId());
    }

    @Test
    void getAttendanceSummary_WrongInstructor_ThrowsException() {
        UUID otherInstructorId = UUID.randomUUID();
        Course otherCourse = Course.builder()
                .id(courseId)
                .instructorId(otherInstructorId)
                .build();

        when(liveClassRepository.findById(liveClassId)).thenReturn(Optional.of(liveClass));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(otherCourse));

        assertThrows(LiveClassAttendanceException.class, 
                () -> attendanceService.getAttendanceSummary(liveClassId, instructorId));
    }

    @Test
    void getAttendanceSummary_CorrectTotalEnrolled_Success() {
        when(liveClassRepository.findById(liveClassId)).thenReturn(Optional.of(liveClass));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.countByCourseId(courseId)).thenReturn(15L);
        when(attendanceRepository.countByLiveClassId(liveClassId)).thenReturn(0L);
        when(attendanceRepository.countCurrentlyJoined(liveClassId)).thenReturn(0L);
        when(attendanceRepository.countAttended(liveClassId)).thenReturn(0L);
        when(attendanceRepository.countAbsent(liveClassId)).thenReturn(0L);
        when(attendanceRepository.sumDurationSeconds(liveClassId)).thenReturn(null);

        LiveClassAttendanceSummaryResponse response = attendanceService.getAttendanceSummary(liveClassId, instructorId);

        assertEquals(15L, response.getTotalEnrolledStudents());
    }

    @Test
    void getAttendanceSummary_CorrectJoinedCount_Success() {
        when(liveClassRepository.findById(liveClassId)).thenReturn(Optional.of(liveClass));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.countByCourseId(courseId)).thenReturn(10L);
        when(attendanceRepository.countByLiveClassId(liveClassId)).thenReturn(7L);
        when(attendanceRepository.countCurrentlyJoined(liveClassId)).thenReturn(0L);
        when(attendanceRepository.countAttended(liveClassId)).thenReturn(0L);
        when(attendanceRepository.countAbsent(liveClassId)).thenReturn(0L);
        when(attendanceRepository.sumDurationSeconds(liveClassId)).thenReturn(null);

        LiveClassAttendanceSummaryResponse response = attendanceService.getAttendanceSummary(liveClassId, instructorId);

        assertEquals(7L, response.getTotalJoined());
    }

    @Test
    void getAttendanceSummary_CorrectAttendedCount_Success() {
        when(liveClassRepository.findById(liveClassId)).thenReturn(Optional.of(liveClass));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.countByCourseId(courseId)).thenReturn(10L);
        when(attendanceRepository.countByLiveClassId(liveClassId)).thenReturn(5L);
        when(attendanceRepository.countCurrentlyJoined(liveClassId)).thenReturn(0L);
        when(attendanceRepository.countAttended(liveClassId)).thenReturn(5L);
        when(attendanceRepository.countAbsent(liveClassId)).thenReturn(0L);
        when(attendanceRepository.sumDurationSeconds(liveClassId)).thenReturn(null);

        LiveClassAttendanceSummaryResponse response = attendanceService.getAttendanceSummary(liveClassId, instructorId);

        assertEquals(5L, response.getTotalAttended());
    }

    @Test
    void getAttendanceSummary_CorrectCurrentlyJoinedCount_Success() {
        when(liveClassRepository.findById(liveClassId)).thenReturn(Optional.of(liveClass));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.countByCourseId(courseId)).thenReturn(10L);
        when(attendanceRepository.countByLiveClassId(liveClassId)).thenReturn(5L);
        when(attendanceRepository.countCurrentlyJoined(liveClassId)).thenReturn(3L);
        when(attendanceRepository.countAttended(liveClassId)).thenReturn(2L);
        when(attendanceRepository.countAbsent(liveClassId)).thenReturn(0L);
        when(attendanceRepository.sumDurationSeconds(liveClassId)).thenReturn(null);

        LiveClassAttendanceSummaryResponse response = attendanceService.getAttendanceSummary(liveClassId, instructorId);

        assertEquals(3L, response.getTotalCurrentlyJoined());
    }

    @Test
    void getAttendanceSummary_CorrectAttendancePercentage_Success() {
        when(liveClassRepository.findById(liveClassId)).thenReturn(Optional.of(liveClass));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.countByCourseId(courseId)).thenReturn(10L);
        when(attendanceRepository.countByLiveClassId(liveClassId)).thenReturn(0L);
        when(attendanceRepository.countCurrentlyJoined(liveClassId)).thenReturn(0L);
        when(attendanceRepository.countAttended(liveClassId)).thenReturn(8L);
        when(attendanceRepository.countAbsent(liveClassId)).thenReturn(0L);
        when(attendanceRepository.sumDurationSeconds(liveClassId)).thenReturn(null);

        LiveClassAttendanceSummaryResponse response = attendanceService.getAttendanceSummary(liveClassId, instructorId);

        assertEquals(80.0, response.getAttendancePercentage());
    }

    @Test
    void getAttendanceSummary_CorrectAverageDuration_Success() {
        when(liveClassRepository.findById(liveClassId)).thenReturn(Optional.of(liveClass));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.countByCourseId(courseId)).thenReturn(10L);
        when(attendanceRepository.countByLiveClassId(liveClassId)).thenReturn(0L);
        when(attendanceRepository.countCurrentlyJoined(liveClassId)).thenReturn(0L);
        when(attendanceRepository.countAttended(liveClassId)).thenReturn(5L);
        when(attendanceRepository.countAbsent(liveClassId)).thenReturn(0L);
        when(attendanceRepository.sumDurationSeconds(liveClassId)).thenReturn(4500L); // 5 attended * 900 seconds average

        LiveClassAttendanceSummaryResponse response = attendanceService.getAttendanceSummary(liveClassId, instructorId);

        assertEquals(900L, response.getAverageDurationSeconds());
    }
}
