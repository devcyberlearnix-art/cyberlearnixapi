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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveClassAttendanceService {

    private final LiveClassAttendanceRepository attendanceRepository;
    private final LiveClassRepository liveClassRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    // ==================== STUDENT APIs ====================

    /**
     * Join a live class and create attendance record
     */
    @Transactional
    public LiveClassAttendanceResponse joinLiveClass(Long liveClassId, UUID studentId) {
        // Validate live class exists
        LiveClass liveClass = liveClassRepository.findById(liveClassId)
                .orElseThrow(() -> new LiveClassAttendanceException("Live class not found with id: " + liveClassId));

        // Validate student is enrolled in the course
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, liveClass.getCourseId())
                .orElseThrow(() -> new LiveClassAttendanceException("You are not enrolled in this course"));

        // Validate live class status
        if (liveClass.getStatus() == LiveClass.LiveClassStatus.CANCELLED) {
            throw new LiveClassAttendanceException("Cannot join a cancelled live class");
        }

        if (liveClass.getStatus() == LiveClass.LiveClassStatus.COMPLETED) {
            throw new LiveClassAttendanceException("Cannot join a completed live class");
        }

        // Check if attendance already exists
        if (attendanceRepository.existsByLiveClassIdAndStudentId(liveClassId, studentId)) {
            throw new LiveClassAttendanceException("You have already joined this live class");
        }

        // Create attendance record
        LiveClassAttendance attendance = LiveClassAttendance.builder()
                .liveClassId(liveClassId)
                .studentId(studentId)
                .joinedAt(LocalDateTime.now())
                .durationSeconds(0L)
                .status(LiveClassAttendance.AttendanceStatus.JOINED)
                .build();

        attendance = attendanceRepository.save(attendance);
        log.info("Student {} joined live class {}", studentId, liveClassId);

        return LiveClassAttendanceResponse.fromEntity(attendance);
    }

    /**
     * Leave a live class and record leave time
     */
    @Transactional
    public LiveClassAttendanceResponse leaveLiveClass(Long liveClassId, UUID studentId) {
        // Validate attendance exists
        LiveClassAttendance attendance = attendanceRepository.findByLiveClassIdAndStudentId(liveClassId, studentId)
                .orElseThrow(() -> new LiveClassAttendanceException("Attendance record not found"));

        // Validate ownership
        if (!attendance.getStudentId().equals(studentId)) {
            throw new LiveClassAttendanceException("You are not authorized to leave this attendance record");
        }

        // Validate attendance status
        if (attendance.getStatus() != LiveClassAttendance.AttendanceStatus.JOINED) {
            throw new LiveClassAttendanceException("You have already left this live class");
        }

        // Record leave time
        LocalDateTime leftAt = LocalDateTime.now();
        attendance.setLeftAt(leftAt);

        // Calculate duration
        long durationSeconds = ChronoUnit.SECONDS.between(attendance.getJoinedAt(), leftAt);
        if (durationSeconds < 0) {
            throw new LiveClassAttendanceException("Invalid duration calculated");
        }

        attendance.setDurationSeconds(durationSeconds);
        attendance.setStatus(LiveClassAttendance.AttendanceStatus.ATTENDED);

        attendance = attendanceRepository.save(attendance);
        log.info("Student {} left live class {} after {} seconds", studentId, liveClassId, durationSeconds);

        return LiveClassAttendanceResponse.fromEntity(attendance);
    }

    /**
     * Get student's own attendance for a live class
     */
    @Transactional(readOnly = true)
    public LiveClassAttendanceResponse getMyAttendance(Long liveClassId, UUID studentId) {
        LiveClassAttendance attendance = attendanceRepository.findByLiveClassIdAndStudentId(liveClassId, studentId)
                .orElseThrow(() -> new LiveClassAttendanceException("Attendance record not found"));

        // Validate ownership
        if (!attendance.getStudentId().equals(studentId)) {
            throw new LiveClassAttendanceException("You are not authorized to view this attendance record");
        }

        return LiveClassAttendanceResponse.fromEntity(attendance);
    }

    // ==================== INSTRUCTOR/ADMIN APIs ====================

    /**
     * Get all attendance records for a live class
     */
    @Transactional(readOnly = true)
    public List<LiveClassAttendanceResponse> getAttendanceByLiveClass(Long liveClassId, UUID instructorId) {
        // Validate live class exists
        LiveClass liveClass = liveClassRepository.findById(liveClassId)
                .orElseThrow(() -> new LiveClassAttendanceException("Live class not found with id: " + liveClassId));

        // Validate instructor owns the course
        Course course = courseRepository.findById(liveClass.getCourseId())
                .orElseThrow(() -> new LiveClassAttendanceException("Course not found with id: " + liveClass.getCourseId()));
        if (!course.getInstructorId().equals(instructorId)) {
            throw new LiveClassAttendanceException("You are not authorized to view attendance for this live class");
        }

        return attendanceRepository.findByLiveClassId(liveClassId).stream()
                .map(LiveClassAttendanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get attendance summary for a live class
     */
    @Transactional(readOnly = true)
    public LiveClassAttendanceSummaryResponse getAttendanceSummary(Long liveClassId, UUID instructorId) {
        // Validate live class exists
        LiveClass liveClass = liveClassRepository.findById(liveClassId)
                .orElseThrow(() -> new LiveClassAttendanceException("Live class not found with id: " + liveClassId));

        // Validate instructor owns the course
        Course course = courseRepository.findById(liveClass.getCourseId())
                .orElseThrow(() -> new LiveClassAttendanceException("Course not found with id: " + liveClass.getCourseId()));
        if (!course.getInstructorId().equals(instructorId)) {
            throw new LiveClassAttendanceException("You are not authorized to view attendance summary for this live class");
        }

        // Get total enrolled students
        long totalEnrolledStudents = enrollmentRepository.countByCourseId(liveClass.getCourseId());

        // Get attendance statistics
        long totalJoined = attendanceRepository.countByLiveClassId(liveClassId);
        long totalCurrentlyJoined = attendanceRepository.countCurrentlyJoined(liveClassId);
        long totalAttended = attendanceRepository.countAttended(liveClassId);
        long totalAbsent = attendanceRepository.countAbsent(liveClassId);

        // Calculate attendance percentage
        double attendancePercentage = 0.0;
        if (totalEnrolledStudents > 0) {
            attendancePercentage = ((double) totalAttended / totalEnrolledStudents) * 100.0;
        }

        // Calculate average duration
        Long sumDurationSeconds = attendanceRepository.sumDurationSeconds(liveClassId);
        long averageDurationSeconds = 0L;
        if (totalAttended > 0 && sumDurationSeconds != null) {
            averageDurationSeconds = sumDurationSeconds / totalAttended;
        }

        return LiveClassAttendanceSummaryResponse.builder()
                .liveClassId(liveClassId)
                .totalEnrolledStudents(totalEnrolledStudents)
                .totalJoined(totalJoined)
                .totalAttended(totalAttended)
                .totalCurrentlyJoined(totalCurrentlyJoined)
                .totalAbsent(totalAbsent)
                .attendancePercentage(attendancePercentage)
                .averageDurationSeconds(averageDurationSeconds)
                .build();
    }
}
