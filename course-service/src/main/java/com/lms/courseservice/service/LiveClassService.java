package com.lms.courseservice.service;

import com.lms.courseservice.dto.LiveClassJoinResponse;
import com.lms.courseservice.dto.LiveClassRequest;
import com.lms.courseservice.dto.LiveClassResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Enrollment;
import com.lms.courseservice.entity.LiveClass;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.exception.LiveClassException;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.EnrollmentRepository;
import com.lms.courseservice.repository.LiveClassRepository;
import com.lms.courseservice.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
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
public class LiveClassService {

    private final LiveClassRepository liveClassRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;

    // ==================== INSTRUCTOR/ADMIN APIs ====================

    /**
     * Create a new live class
     */
    @Transactional
    public LiveClassResponse createLiveClass(LiveClassRequest request, UUID instructorId) {
        // Validate course exists
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new LiveClassException("Course not found with id: " + request.getCourseId()));

        // Validate instructor owns the course
        if (!course.getInstructorId().equals(instructorId)) {
            throw new LiveClassException("You are not authorized to create live classes for this course");
        }

        // Validate section if provided
        if (request.getSectionId() != null) {
            Section section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new LiveClassException("Section not found with id: " + request.getSectionId()));
            if (!section.getCourse().getId().equals(request.getCourseId())) {
                throw new LiveClassException("Section does not belong to the specified course");
            }
        }

        // Validate schedule
        validateSchedule(request.getScheduledStartTime(), request.getScheduledEndTime());

        // Validate meeting provider
        try {
            LiveClass.MeetingProvider.valueOf(request.getMeetingProvider().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new LiveClassException("Invalid meeting provider. Must be one of: ZOOM, GOOGLE_MEET, TEAMS, OTHER");
        }

        // Create live class
        LiveClass liveClass = LiveClass.builder()
                .courseId(request.getCourseId())
                .sectionId(request.getSectionId())
                .instructorId(instructorId)
                .title(request.getTitle())
                .description(request.getDescription())
                .meetingUrl(request.getMeetingUrl())
                .meetingProvider(LiveClass.MeetingProvider.valueOf(request.getMeetingProvider().toUpperCase()))
                .scheduledStartTime(request.getScheduledStartTime())
                .scheduledEndTime(request.getScheduledEndTime())
                .maxParticipants(request.getMaxParticipants())
                .status(LiveClass.LiveClassStatus.SCHEDULED)
                .build();

        liveClass = liveClassRepository.save(liveClass);
        log.info("Live class created: {} for course: {} by instructor: {}", liveClass.getId(), request.getCourseId(), instructorId);

        return LiveClassResponse.fromEntity(liveClass);
    }

    /**
     * Update an existing live class
     */
    @Transactional
    public LiveClassResponse updateLiveClass(Long liveClassId, LiveClassRequest request, UUID instructorId) {
        LiveClass liveClass = liveClassRepository.findById(liveClassId)
                .orElseThrow(() -> new LiveClassException("Live class not found with id: " + liveClassId));

        // Validate instructor owns the live class
        if (!liveClass.getInstructorId().equals(instructorId)) {
            throw new LiveClassException("You are not authorized to update this live class");
        }

        // Validate course if changed
        if (!liveClass.getCourseId().equals(request.getCourseId())) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new LiveClassException("Course not found with id: " + request.getCourseId()));
            if (!course.getInstructorId().equals(instructorId)) {
                throw new LiveClassException("You are not authorized to create live classes for this course");
            }
        }

        // Validate section if provided and changed
        if (request.getSectionId() != null && !request.getSectionId().equals(liveClass.getSectionId())) {
            Section section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new LiveClassException("Section not found with id: " + request.getSectionId()));
            if (!section.getCourse().getId().equals(request.getCourseId())) {
                throw new LiveClassException("Section does not belong to the specified course");
            }
        }

        // Can only update if SCHEDULED
        if (liveClass.getStatus() != LiveClass.LiveClassStatus.SCHEDULED) {
            throw new LiveClassException("Can only update live classes with SCHEDULED status");
        }

        // Validate schedule if changed
        if (!liveClass.getScheduledStartTime().equals(request.getScheduledStartTime()) ||
            !liveClass.getScheduledEndTime().equals(request.getScheduledEndTime())) {
            validateSchedule(request.getScheduledStartTime(), request.getScheduledEndTime());
        }

        // Validate meeting provider if changed
        if (request.getMeetingProvider() != null) {
            try {
                LiveClass.MeetingProvider.valueOf(request.getMeetingProvider().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new LiveClassException("Invalid meeting provider. Must be one of: ZOOM, GOOGLE_MEET, TEAMS, OTHER");
            }
        }

        // Update fields
        liveClass.setCourseId(request.getCourseId());
        liveClass.setSectionId(request.getSectionId());
        liveClass.setTitle(request.getTitle());
        liveClass.setDescription(request.getDescription());
        liveClass.setMeetingUrl(request.getMeetingUrl());
        if (request.getMeetingProvider() != null) {
            liveClass.setMeetingProvider(LiveClass.MeetingProvider.valueOf(request.getMeetingProvider().toUpperCase()));
        }
        liveClass.setScheduledStartTime(request.getScheduledStartTime());
        liveClass.setScheduledEndTime(request.getScheduledEndTime());
        liveClass.setMaxParticipants(request.getMaxParticipants());

        liveClass = liveClassRepository.save(liveClass);
        log.info("Live class updated: {}", liveClassId);

        return LiveClassResponse.fromEntity(liveClass);
    }

    /**
     * Get live class by ID
     */
    @Transactional(readOnly = true)
    public LiveClassResponse getLiveClassById(Long liveClassId, UUID instructorId) {
        LiveClass liveClass = liveClassRepository.findById(liveClassId)
                .orElseThrow(() -> new LiveClassException("Live class not found with id: " + liveClassId));

        // Validate instructor owns the live class
        if (!liveClass.getInstructorId().equals(instructorId)) {
            throw new LiveClassException("You are not authorized to view this live class");
        }

        return LiveClassResponse.fromEntity(liveClass);
    }

    /**
     * Get all live classes for a course
     */
    @Transactional(readOnly = true)
    public List<LiveClassResponse> getLiveClassesByCourse(Long courseId, UUID instructorId) {
        // Validate course exists and instructor owns it
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new LiveClassException("Course not found with id: " + courseId));
        if (!course.getInstructorId().equals(instructorId)) {
            throw new LiveClassException("You are not authorized to view live classes for this course");
        }

        return liveClassRepository.findByCourseId(courseId).stream()
                .map(LiveClassResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all live classes for a section
     */
    @Transactional(readOnly = true)
    public List<LiveClassResponse> getLiveClassesBySection(Long sectionId, UUID instructorId) {
        // Validate section exists and instructor owns the course
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new LiveClassException("Section not found with id: " + sectionId));
        if (!section.getCourse().getInstructorId().equals(instructorId)) {
            throw new LiveClassException("You are not authorized to view live classes for this section");
        }

        return liveClassRepository.findBySectionId(sectionId).stream()
                .map(LiveClassResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming live classes for a course
     */
    @Transactional(readOnly = true)
    public List<LiveClassResponse> getUpcomingLiveClassesByCourse(Long courseId, UUID instructorId) {
        // Validate course exists and instructor owns it
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new LiveClassException("Course not found with id: " + courseId));
        if (!course.getInstructorId().equals(instructorId)) {
            throw new LiveClassException("You are not authorized to view live classes for this course");
        }

        return liveClassRepository.findUpcomingByCourseId(courseId, LocalDateTime.now()).stream()
                .map(LiveClassResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Start a live class (SCHEDULED -> LIVE)
     */
    @Transactional
    public void startLiveClass(Long liveClassId, UUID instructorId) {
        LiveClass liveClass = liveClassRepository.findById(liveClassId)
                .orElseThrow(() -> new LiveClassException("Live class not found with id: " + liveClassId));

        // Validate instructor owns the live class
        if (!liveClass.getInstructorId().equals(instructorId)) {
            throw new LiveClassException("You are not authorized to start this live class");
        }

        // Validate state transition
        if (liveClass.getStatus() != LiveClass.LiveClassStatus.SCHEDULED) {
            throw new LiveClassException("Can only start live classes with SCHEDULED status");
        }

        liveClass.setStatus(LiveClass.LiveClassStatus.LIVE);
        liveClassRepository.save(liveClass);
        log.info("Live class started: {}", liveClassId);
    }

    /**
     * Complete a live class (LIVE -> COMPLETED)
     */
    @Transactional
    public void completeLiveClass(Long liveClassId, UUID instructorId, String recordingUrl) {
        LiveClass liveClass = liveClassRepository.findById(liveClassId)
                .orElseThrow(() -> new LiveClassException("Live class not found with id: " + liveClassId));

        // Validate instructor owns the live class
        if (!liveClass.getInstructorId().equals(instructorId)) {
            throw new LiveClassException("You are not authorized to complete this live class");
        }

        // Validate state transition
        if (liveClass.getStatus() != LiveClass.LiveClassStatus.LIVE) {
            throw new LiveClassException("Can only complete live classes with LIVE status");
        }

        liveClass.setStatus(LiveClass.LiveClassStatus.COMPLETED);
        liveClass.setRecordingUrl(recordingUrl);
        liveClassRepository.save(liveClass);
        log.info("Live class completed: {}", liveClassId);
    }

    /**
     * Cancel a live class (SCHEDULED -> CANCELLED or LIVE -> CANCELLED)
     */
    @Transactional
    public void cancelLiveClass(Long liveClassId, UUID instructorId) {
        LiveClass liveClass = liveClassRepository.findById(liveClassId)
                .orElseThrow(() -> new LiveClassException("Live class not found with id: " + liveClassId));

        // Validate instructor owns the live class
        if (!liveClass.getInstructorId().equals(instructorId)) {
            throw new LiveClassException("You are not authorized to cancel this live class");
        }

        // Validate state transition
        if (liveClass.getStatus() != LiveClass.LiveClassStatus.SCHEDULED && 
            liveClass.getStatus() != LiveClass.LiveClassStatus.LIVE) {
            throw new LiveClassException("Can only cancel live classes with SCHEDULED or LIVE status");
        }

        liveClass.setStatus(LiveClass.LiveClassStatus.CANCELLED);
        liveClass.setCancelledAt(LocalDateTime.now());
        liveClassRepository.save(liveClass);
        log.info("Live class cancelled: {}", liveClassId);
    }

    /**
     * Delete a live class
     */
    @Transactional
    public void deleteLiveClass(Long liveClassId, UUID instructorId) {
        LiveClass liveClass = liveClassRepository.findById(liveClassId)
                .orElseThrow(() -> new LiveClassException("Live class not found with id: " + liveClassId));

        // Validate instructor owns the live class
        if (!liveClass.getInstructorId().equals(instructorId)) {
            throw new LiveClassException("You are not authorized to delete this live class");
        }

        // Can only delete if SCHEDULED
        if (liveClass.getStatus() != LiveClass.LiveClassStatus.SCHEDULED) {
            throw new LiveClassException("Can only delete live classes with SCHEDULED status");
        }

        liveClassRepository.delete(liveClass);
        log.info("Live class deleted: {}", liveClassId);
    }

    // ==================== STUDENT APIs ====================

    /**
     * Get upcoming live classes for a course (student view)
     */
    @Transactional(readOnly = true)
    public List<LiveClassResponse> getUpcomingLiveClassesForStudent(Long courseId, UUID studentId) {
        // Validate student is enrolled in the course
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new LiveClassException("You are not enrolled in this course"));

        return liveClassRepository.findUpcomingByCourseId(courseId, LocalDateTime.now()).stream()
                .map(LiveClassResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Join a live class (student view)
     */
    @Transactional(readOnly = true)
    public LiveClassJoinResponse joinLiveClass(Long liveClassId, UUID studentId) {
        LiveClass liveClass = liveClassRepository.findById(liveClassId)
                .orElseThrow(() -> new LiveClassException("Live class not found with id: " + liveClassId));

        // Validate student is enrolled in the course
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, liveClass.getCourseId())
                .orElseThrow(() -> new LiveClassException("You are not enrolled in this course"));

        // Validate live class status
        if (liveClass.getStatus() == LiveClass.LiveClassStatus.CANCELLED) {
            throw new LiveClassException("This live class has been cancelled");
        }

        if (liveClass.getStatus() == LiveClass.LiveClassStatus.COMPLETED) {
            throw new LiveClassException("This live class has already completed");
        }

        // Return limited information for student
        return LiveClassJoinResponse.builder()
                .id(liveClass.getId())
                .title(liveClass.getTitle())
                .scheduledStartTime(liveClass.getScheduledStartTime())
                .scheduledEndTime(liveClass.getScheduledEndTime())
                .status(liveClass.getStatus().name())
                .meetingProvider(liveClass.getMeetingProvider().name())
                .meetingUrl(liveClass.getMeetingUrl())
                .build();
    }

    // ==================== VALIDATION HELPERS ====================

    private void validateSchedule(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new LiveClassException("Start time and end time are required");
        }

        if (startTime.isAfter(endTime)) {
            throw new LiveClassException("Start time must be before end time");
        }

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new LiveClassException("Start time must be in the future");
        }

        // Validate duration is reasonable (max 8 hours)
        if (startTime.plusHours(8).isBefore(endTime)) {
            throw new LiveClassException("Live class duration cannot exceed 8 hours");
        }
    }
}
