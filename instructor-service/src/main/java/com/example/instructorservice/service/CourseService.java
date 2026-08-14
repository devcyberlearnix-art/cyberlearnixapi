package com.example.instructorservice.service;

import com.example.instructorservice.dto.*;
import com.example.instructorservice.entity.Course;
import com.example.instructorservice.entity.Enrollment;
import com.example.instructorservice.entity.Instructor;
import com.example.instructorservice.exeception.NotFoundException;
import com.example.instructorservice.exeception.ValidationException;
import com.example.instructorservice.integration.CourseIntegrationService;
import com.example.instructorservice.repository.CourseRepository;
import com.example.instructorservice.repository.InstructorRepository;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.instructorservice.repository.ContentRepository;
import com.example.instructorservice.repository.EnrollmentRepository;
import com.example.instructorservice.repository.PaymentRepository;
import com.example.instructorservice.repository.ReviewRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Service
@RequiredArgsConstructor
public class CourseService {
        private final EnrollmentRepository enrollmentRepository;
        private final ReviewRepository reviewRepository;
        private final PaymentRepository paymentRepository;
        private final ContentRepository contentRepository;
        private final CourseRepository courseRepository;
        private final InstructorRepository instructorRepository;
        private final CourseIntegrationService courseIntegrationService;

        public CourseFullResponseDTO createCourse(UUID userId, CourseRequestDTO request, String authorization) {

                Instructor instructor = instructorRepository.findByUserId(userId)
                                .orElseGet(() -> {
                                        Instructor i = new Instructor();
                                        i.setUserId(userId);
                                        i.setName("Auto Instructor");

                                        return instructorRepository.save(i);
                                });

                Course.CourseStatus status = request.getStatus();
                if (status == null) {
                        status = Course.CourseStatus.DRAFT;
                }

                // ✅ Build course (tags included here is correct)
                Course course = Course.builder()
                                .title(request.getTitle())
                                .description(request.getDescription())
                                .price(request.getPrice())
                                .category(request.getCategory())
                                .subtitle(request.getSubtitle())
                                .thumbnailUrl(request.getThumbnailUrl())
                                .previewVideoUrl(request.getPreviewVideoUrl())
                                .status(status)
                                .createdAt(LocalDateTime.now())
                                .instructor(instructor)
                                .slug(generateSlug(request.getTitle()))
                                .tags(request.getTags()) // ✅ safe here
                                .build();

                // status timestamps
                if (status == Course.CourseStatus.PUBLISHED) {
                        course.setPublishedAt(LocalDateTime.now());
                } else if (status == Course.CourseStatus.ARCHIVED) {
                        course.setArchivedAt(LocalDateTime.now());
                }

                // 🔥 IMPORTANT: force insert BEFORE element collection processing
                course = courseRepository.save(course);

                // ✅ SYNC TO COURSE SERVICE (Port 8083)
                try {
                        Long courseServiceId = courseIntegrationService.syncCourseCreation(course, request, authorization);
                        course.setCourseServiceId(courseServiceId);
                        course.setSyncStatus("SYNCED");
                        course = courseRepository.save(course);
                        System.out.println("✓ Course synced to Course Service with ID: " + course.getCourseServiceId());
                } catch (Exception e) {
                        course.setSyncStatus("FAILED");
                        course = courseRepository.save(course);
                        System.err.println("✗ Failed to sync course to Course Service: " + e.getMessage());
                        // Course still created locally even if sync fails - instructor can retry later
                }

                return mapCourse(course, null);
        }

        // =========================
        // PUBLISH COURSE
        // =========================
        public CourseFullResponseDTO publishCourse(UUID courseId) {

                Course course = courseRepository.findById(Long.valueOf(courseId.toString()))
                                .orElseThrow(() -> new RuntimeException("Course not found"));

                course.setStatus(Course.CourseStatus.PUBLISHED);
                course.setPublishedAt(LocalDateTime.now());
                course = courseRepository.save(course);

                // ✅ SYNC PUBLISH TO COURSE SERVICE (Port 8083)
                try {
                        courseIntegrationService.syncCoursePublish(course);
                        System.out.println("✓ Course published in Course Service: " + course.getCourseServiceId());
                } catch (Exception e) {
                        System.err.println("✗ Failed to publish course in Course Service: " + e.getMessage());
                        // Course is published locally; sync can be retried
                }

                return mapCourse(course, "Course published successfully");
        }

        // =========================
        // ARCHIVE COURSE
        // =========================
        public CourseFullResponseDTO archiveCourse(Long courseId) {

                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new RuntimeException("Course not found"));

                course.setStatus(Course.CourseStatus.ARCHIVED);
                course.setArchivedAt(LocalDateTime.now());
                course = courseRepository.save(course);

                // ✅ SYNC DELETE TO COURSE SERVICE (Port 8083)
                try {
                        if (course.getCourseServiceId() != null) {
                                courseIntegrationService.syncCourseDeletion(course);
                                System.out.println("✓ Course archived in Course Service: " + course.getCourseServiceId());
                        }
                } catch (Exception e) {
                        System.err.println("✗ Failed to archive course in Course Service: " + e.getMessage());
                        // Course is archived locally; sync can be retried
                }

                return mapCourse(course, "Course archived successfully");
        }

        // =========================
        // COMMON RESPONSE MAPPER
        // =========================
        private CourseFullResponseDTO mapCourse(Course course, String message) {

                Course.CourseStatus status = course.getStatus();

                // ================= MESSAGE =================
                if (message == null) {
                        switch (status) {
                                case DRAFT -> message = "Course created successfully";
                                case PUBLISHED -> message = "Course published successfully";
                                case ARCHIVED -> message = "Course archived successfully";
                                default -> message = "Course processed successfully";
                        }
                }

                // ================= ANALYTICS =================
                long enrolledStudents = enrollmentRepository.countByCourse(course);
                long activeLearners = enrollmentRepository.countActiveByCourse(course);
                double completionRate = enrollmentRepository.calculateCompletionRate(course);
                double averageRating = reviewRepository.averageRatingByCourse(course);
                long totalReviews = reviewRepository.countByCourse(course);
                double totalRevenue = paymentRepository.totalRevenueByCourse(course);

                // ================= CONTENT =================
                int sections = contentRepository.countSectionsByCourse(course);
                int lectures = contentRepository.countLecturesByCourse(course);
                int assignments = contentRepository.countAssignmentsByCourse(course);
                int quizzes = contentRepository.countQuizzesByCourse(course);
                int totalDurationMinutes = contentRepository.totalDurationByCourse(course);

                // ================= INSTRUCTOR =================
                Instructor instructor = course.getInstructor();
                String headline = instructor.getHeadline() != null ? instructor.getHeadline() : "Instructor";
                double instructorRating = reviewRepository.averageRatingByCourse(course);
                boolean verified = instructor.getVerified() != null ? instructor.getVerified() : true;
                // ================= BUILD RESPONSE =================
                return CourseFullResponseDTO.builder()
                                .success(true)
                                .message(message)
                                .requestId(UUID.randomUUID().toString())
                                .data(CourseFullResponseDTO.CourseData.builder()

                                                // IDENTITY
                                                .identity(CourseFullResponseDTO.Identity.builder()
                                                                .courseId(course.getId())
                                                                .slug(course.getSlug())
                                                                .build())

                                                // CORE
                                                .core(CourseFullResponseDTO.Core.builder()
                                                                .title(course.getTitle())
                                                                .subtitle(course.getSubtitle())
                                                                .description(course.getDescription())
                                                                .language(course.getLanguage() != null
                                                                                ? course.getLanguage()
                                                                                : "en")
                                                                .level(course.getLevel() != null ? course.getLevel()
                                                                                : "BEGINNER")
                                                                .category(course.getCategory())
                                                                .tags(course.getTags())
                                                                .build())

                                                // STATUS
                                                .status(CourseFullResponseDTO.Status.builder()
                                                                .status(status)
                                                                .isPublished(status == Course.CourseStatus.PUBLISHED)
                                                                .isArchived(status == Course.CourseStatus.ARCHIVED)
                                                                .visibility(status == Course.CourseStatus.PUBLISHED
                                                                                ? "PUBLIC"
                                                                                : "PRIVATE")
                                                                .approvalStatus(status == Course.CourseStatus.DRAFT
                                                                                ? "PENDING"
                                                                                : "APPROVED")
                                                                .publishedBy(status == Course.CourseStatus.PUBLISHED
                                                                                ? "INSTRUCTOR"
                                                                                : null)
                                                                .build())

                                                // INSTRUCTOR
                                                .instructor(CourseFullResponseDTO.InstructorDTO.builder()
                                                                .instructorId(instructor.getId())
                                                                .name(instructor.getName())
                                                                .email(instructor.getEmail())
                                                                .headline(headline)
                                                                .rating(instructorRating)
                                                                .verified(verified)
                                                                .build())

                                                // CONTENT
                                                .content(CourseFullResponseDTO.Content.builder()
                                                                .sections(sections)
                                                                .lectures(lectures)
                                                                .totalDurationMinutes(totalDurationMinutes)
                                                                .assignments(assignments)
                                                                .quizzes(quizzes)
                                                                .build())

                                                // MEDIA
                                                .media(CourseFullResponseDTO.Media.builder()
                                                                .thumbnailUrl(course.getThumbnailUrl())
                                                                .previewVideoUrl(course.getPreviewVideoUrl())
                                                                .build())

                                                // ANALYTICS
                                                .analytics(CourseFullResponseDTO.Analytics.builder()
                                                                .enrolledStudents((int) enrolledStudents)
                                                                .activeLearners((int) activeLearners)
                                                                .completionRate(completionRate)
                                                                .averageRating(averageRating)
                                                                .totalReviews((int) totalReviews)
                                                                .totalRevenue(totalRevenue)
                                                                .build())

                                                // VISIBILITY
                                                .visibility(CourseFullResponseDTO.Visibility.builder()
                                                                .isPublic(status == Course.CourseStatus.PUBLISHED)
                                                                .isSearchable(status == Course.CourseStatus.PUBLISHED)
                                                                .allowPreview(true)
                                                                .build())

                                                // TIMESTAMPS
                                                .timestamps(CourseFullResponseDTO.Timestamps.builder()
                                                                .createdAt(course.getCreatedAt())
                                                                .updatedAt(course.getUpdatedAt() != null
                                                                                ? course.getUpdatedAt()
                                                                                : course.getCreatedAt())
                                                                .publishedAt(course.getPublishedAt())
                                                                .archivedAt(course.getArchivedAt())
                                                                .build())

                                                // LINKS
                                                .links(CourseFullResponseDTO.Links.builder()
                                                                .self("/api/v1/courses/" + course.getId())
                                                                .enroll("/api/v1/courses/" + course.getId() + "/enroll")
                                                                .reviews("/api/v1/courses/" + course.getId() + "/reviews")
                                                                .content("/api/v1/courses/" + course.getId() + "/content")
                                                                .build())

                                                .build())
                                .build();
        }

        // =========================
        // SLUG GENERATOR
        // =========================
        private String generateSlug(String title) {
                return title.toLowerCase()
                                .replace(" ", "-")
                                .replaceAll("[^a-z0-9-]", "");
        }

        private Instructor findInstructorByIdOrUserId(UUID idOrUserId) {
                return instructorRepository.findById(idOrUserId)
                                .or(() -> instructorRepository.findByUserId(idOrUserId))
                                .orElseThrow(() -> new NotFoundException(
                                                "Instructor not found with id: " + idOrUserId));
        }

        public List<CourseResponseDTO> getCoursesByInstructor(UUID instructorId) {

                // 🔍 Validate instructor exists (checks by primary key id or userId)
                Instructor instructor = findInstructorByIdOrUserId(instructorId);

                // 📦 Fetch courses
                List<Course> courses = courseRepository.findByInstructor(instructor);

                // ✅ Optional: handle empty list
                if (courses.isEmpty()) {
                        return List.of(); // or throw exception if needed
                }

                // 🔄 Convert to DTO
                return courses.stream()
                                .map(course -> CourseResponseDTO.builder()
                                                .courseId(course.getId())
                                                .title(course.getTitle())
                                                .description(course.getDescription())
                                                .price(course.getPrice())
                                                .category(course.getCategory())
                                                .status(course.getStatus().name()) // ✅ FIXED
                                                .instructorId(course.getInstructor().getUserId())
                                                .createdAt(course.getCreatedAt())
                                                .build())
                                .toList();
        }

        public CourseResponseDTO getCourseById(UUID instructorId, Long courseId) {

                // 🔍 Fetch course directly with instructor validation
                Course course = courseRepository
                                .findByIdAndInstructorId(courseId, instructorId)
                                .orElseThrow(() -> new NotFoundException(
                                                "Course not found with id: " + courseId +
                                                                " for instructor: " + instructorId));

                // 🔄 Convert to DTO
                return CourseResponseDTO.builder()
                                .courseId(course.getId())
                                .title(course.getTitle())
                                .description(course.getDescription())
                                .price(course.getPrice())
                                .category(course.getCategory())
                                .status(course.getStatus().name()) // ✅ FIXED
                                .instructorId(course.getInstructor().getUserId())
                                .createdAt(course.getCreatedAt())
                                .build();
        }

        public CourseResponseDTO getCourseById(Long courseId) {
                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));

                return CourseResponseDTO.builder()
                                .courseId(course.getId())
                                .title(course.getTitle())
                                .description(course.getDescription())
                                .price(course.getPrice())
                                .category(course.getCategory())
                                .status(course.getStatus().name())
                                .instructorId(course.getInstructor().getId())
                                .createdAt(course.getCreatedAt())
                                .build();
        }

        public CourseResponseDTO updateCourse(
                        UUID instructorId,
                        Long courseId,
                        CourseRequestDTO request) {

                // 🔍 Fetch course + validate instructor
                Course course = courseRepository
                                .findByIdAndInstructorId(courseId, instructorId)
                                .orElseThrow(() -> new NotFoundException(
                                                "Course not found with id: " + courseId +
                                                                " for instructor: " + instructorId));

                // ✏️ Update only non-null fields
                if (request.getTitle() != null) {
                        course.setTitle(request.getTitle());
                }

                if (request.getDescription() != null) {
                        course.setDescription(request.getDescription());
                }

                if (request.getPrice() != null) {
                        course.setPrice(request.getPrice());
                }

                if (request.getCategory() != null) {
                        course.setCategory(request.getCategory());
                }

                if (request.getStatus() != null) {
                        course.setStatus(request.getStatus());
                }

                // 💾 Save
                Course updated = courseRepository.save(course);

                // ✅ SYNC UPDATE TO COURSE SERVICE (Port 8083)
                try {
                        if (updated.getCourseServiceId() != null) {
                                courseIntegrationService.syncCourseUpdate(updated, request);
                                System.out.println("✓ Course updated in Course Service: " + updated.getCourseServiceId());
                        }
                } catch (Exception e) {
                        System.err.println("✗ Failed to sync course update to Course Service: " + e.getMessage());
                        // Course is updated locally; sync can be retried
                }

                // 🔄 Convert to DTO
                return CourseResponseDTO.builder()
                                .courseId(updated.getId())
                                .title(updated.getTitle())
                                .description(updated.getDescription())
                                .price(updated.getPrice())
                                .category(updated.getCategory())
                                .status(updated.getStatus().name())
                                .instructorId(updated.getInstructor().getUserId())
                                .createdAt(updated.getCreatedAt())
                                .build();
        }

        public CourseResponseDTO deleteCourse(UUID instructorId, Long courseId) {

                // 🔍 Fetch course + validate ownership
                Course course = courseRepository
                                .findByIdAndInstructorId(courseId, instructorId)
                                .orElseThrow(() -> new NotFoundException(
                                                "Course not found with id: " + courseId +
                                                                " for instructor: " + instructorId));

                // 🔴 Soft delete → change status
                course.setStatus(Course.CourseStatus.ARCHIVED);

                // 💾 Save updated course
                Course updated = courseRepository.save(course);

                // ✅ SYNC DELETE TO COURSE SERVICE (Port 8083)
                try {
                        if (updated.getCourseServiceId() != null) {
                                courseIntegrationService.syncCourseDeletion(updated);
                                System.out.println("✓ Course deleted from Course Service: " + updated.getCourseServiceId());
                        }
                } catch (Exception e) {
                        System.err.println("✗ Failed to sync course deletion to Course Service: " + e.getMessage());
                        // Course is archived locally; deletion sync can be retried
                }

                // 🔄 Convert to DTO (return updated data)
                return CourseResponseDTO.builder()
                                .courseId(updated.getId())
                                .title(updated.getTitle())
                                .description(updated.getDescription())
                                .price(updated.getPrice())
                                .category(updated.getCategory())
                                .status(updated.getStatus().name()) // ARCHIVED
                                .instructorId(updated.getInstructor().getUserId())
                                .createdAt(updated.getCreatedAt())
                                .build();
        }

        public StudentResponseDTO getEnrolledStudents(UUID instructorId, Long courseId) {
                // 1️⃣ Validate instructor and course
                Instructor instructor = findInstructorByIdOrUserId(instructorId);
                Course course = courseRepository.findByIdAndInstructorId(courseId, instructorId)
                                .or(() -> courseRepository.findById(courseId))
                                .orElseThrow(() -> new NotFoundException(
                                                "Course not found with id: " + courseId + " for instructor: " + instructorId));

                // 2️⃣ First try fetching live enrollment data from course-service (port 8083)
                Long targetCourseId = course.getCourseServiceId() != null ? course.getCourseServiceId() : courseId;
                List<Map<String, Object>> remoteStudents = courseIntegrationService.fetchEnrolledStudents(targetCourseId);
                if (remoteStudents.isEmpty() && course.getCourseServiceId() != null) {
                        remoteStudents = courseIntegrationService.fetchEnrolledStudents(courseId);
                }

                List<StudentResponseDTO.StudentData> students = new java.util.ArrayList<>();
                if (!remoteStudents.isEmpty()) {
                        for (Map<String, Object> m : remoteStudents) {
                                UUID studentUuid = null;
                                Object sId = m.get("studentId");
                                if (sId != null) {
                                        try {
                                                studentUuid = UUID.fromString(sId.toString());
                                        } catch (Exception ignored) {}
                                }

                                LocalDateTime enrolledTime = null;
                                Object eAt = m.get("enrolledAt");
                                if (eAt != null) {
                                        try {
                                                enrolledTime = LocalDateTime.parse(eAt.toString());
                                        } catch (Exception ignored) {}
                                }

                                students.add(StudentResponseDTO.StudentData.builder()
                                                .studentId(studentUuid)
                                                .name(m.get("studentName") != null ? m.get("studentName").toString() : null)
                                                .status(m.get("status") != null ? m.get("status").toString() : "ACTIVE")
                                                .enrolledAt(enrolledTime != null ? enrolledTime : LocalDateTime.now())
                                                .lastActivityAt(LocalDateTime.now())
                                                .build());
                        }
                } else {
                        // 3️⃣ Fallback to local enrollment repository in instructor database
                        List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);
                        for (Enrollment enrollment : enrollments) {
                                students.add(StudentResponseDTO.StudentData.builder()
                                                .studentId(enrollment.getStudentId())
                                                .status(enrollment.getStatus())
                                                .enrolledAt(enrollment.getEnrolledAt())
                                                .lastActivityAt(enrollment.getLastActivityAt())
                                                .build());
                        }
                }

                // 4️⃣ Return response
                return StudentResponseDTO.builder()
                                .success(true)
                                .message("Enrolled students fetched successfully")
                                .requestId(UUID.randomUUID().toString())
                                .data(students)
                                .build();
        }

        public StudentProgressResponseDTO getStudentProgress(
                        UUID instructorId,
                        Long courseId,
                        UUID studentId) {
                // 1️⃣ Validate instructor and course
                Instructor instructor = findInstructorByIdOrUserId(instructorId);
                Course course = courseRepository.findByIdAndInstructorId(courseId, instructorId)
                                .or(() -> courseRepository.findById(courseId))
                                .orElseThrow(() -> new NotFoundException(
                                                "Course not found with id: " + courseId + " for instructor: " + instructorId));

                // 2️⃣ Fetch enrollment for this student
                Enrollment enrollment = (Enrollment) enrollmentRepository.findByCourseAndStudentId(course, studentId)
                                .orElse(null);

                StudentProgressResponseDTO.StudentProgressData data;
                if (enrollment != null) {
                        data = StudentProgressResponseDTO.StudentProgressData
                                        .builder()
                                        .studentId(enrollment.getStudentId())
                                        .courseId(course.getId())
                                        .status(enrollment.getStatus())
                                        .completionRate(enrollment.getCompletionRate())
                                        .enrolledAt(enrollment.getEnrolledAt())
                                        .lastActivityAt(enrollment.getLastActivityAt())
                                        .build();
                } else {
                        // Return default active progress if found via course-service
                        data = StudentProgressResponseDTO.StudentProgressData
                                        .builder()
                                        .studentId(studentId)
                                        .courseId(course.getId())
                                        .status("ACTIVE")
                                        .completionRate(0.0)
                                        .enrolledAt(LocalDateTime.now())
                                        .lastActivityAt(LocalDateTime.now())
                                        .build();
                }

                return StudentProgressResponseDTO.builder()
                                .success(true)
                                .message("Student progress fetched successfully")
                                .requestId(UUID.randomUUID().toString())
                                .data(data)
                                .build();
        }

        public GradeResponseDTO assignOrUpdateGrade(UUID instructorId, Long courseId, GradeRequestDTO request) {
                // 1️⃣ Validate instructor and course
                Instructor instructor = findInstructorByIdOrUserId(instructorId);
                Course course = courseRepository.findByIdAndInstructorId(courseId, instructorId)
                                .or(() -> courseRepository.findById(courseId))
                                .orElseThrow(() -> new NotFoundException("Course not found for this instructor"));

                // 2️⃣ Parse studentId string to UUID safely
                if (request.getStudentId() == null || request.getStudentId().trim().isEmpty()) {
                        throw new ValidationException("Student ID must not be empty");
                }
                UUID studentUuid;
                try {
                        studentUuid = UUID.fromString(request.getStudentId().trim());
                } catch (Exception e) {
                        throw new ValidationException("Invalid student ID format: " + request.getStudentId());
                }

                // 3️⃣ Fetch or auto-create enrollment in instructor database
                Enrollment enrollment = enrollmentRepository
                                .findByCourseAndStudentId(course, studentUuid)
                                .orElseGet(() -> {
                                        Enrollment newEnrollment = new Enrollment();
                                        newEnrollment.setCourse(course);
                                        newEnrollment.setStudentId(studentUuid);
                                        newEnrollment.setStatus("Active");
                                        newEnrollment.setEnrolledAt(LocalDateTime.now());
                                        newEnrollment.setLastActivityAt(LocalDateTime.now());
                                        return enrollmentRepository.save(newEnrollment);
                                });

                // 4️⃣ Parse and validate grade value
                Object rawGrade = request.getGrade();
                if (rawGrade == null) {
                        throw new ValidationException("Grade value must not be null");
                }
                Double gradeValue = parseGradeValue(rawGrade);

                // 5️⃣ Update grade
                enrollment.setGrade(gradeValue);
                enrollment.setLastActivityAt(LocalDateTime.now());
                enrollmentRepository.save(enrollment);

                // 6️⃣ Return response
                return GradeResponseDTO.builder()
                                .studentId(enrollment.getStudentId())
                                .courseId(courseId)
                                .grade(enrollment.getGrade())
                                .updatedAt(LocalDateTime.now())
                                .build();
        }

        public List<GradeResponseDTO> getGradesByCourse(UUID instructorId, Long courseId) {
                Instructor instructor = findInstructorByIdOrUserId(instructorId);
                Course course = courseRepository.findByIdAndInstructorId(courseId, instructorId)
                                .or(() -> courseRepository.findById(courseId))
                                .orElseThrow(() -> new NotFoundException("Course not found for this instructor"));

                List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);
                return enrollments.stream()
                                .map(e -> GradeResponseDTO.builder()
                                                .studentId(e.getStudentId())
                                                .courseId(courseId)
                                                .grade(e.getGrade())
                                                .updatedAt(e.getLastActivityAt() != null ? e.getLastActivityAt() : LocalDateTime.now())
                                                .build())
                                .toList();
        }

        private Double parseGradeValue(Object rawGrade) {
                if (rawGrade instanceof Number) {
                        return ((Number) rawGrade).doubleValue();
                }
                String str = rawGrade.toString().trim();
                if (str.isEmpty()) {
                        throw new RuntimeException("Grade value must not be empty");
                }
                try {
                        return Double.parseDouble(str);
                } catch (NumberFormatException e) {
                        switch (str.toUpperCase()) {
                                case "A+": return 4.0;
                                case "A":  return 4.0;
                                case "A-": return 3.7;
                                case "B+": return 3.3;
                                case "B":  return 3.0;
                                case "B-": return 2.7;
                                case "C+": return 2.3;
                                case "C":  return 2.0;
                                case "C-": return 1.7;
                                case "D+": return 1.3;
                                case "D":  return 1.0;
                                case "D-": return 0.7;
                                case "F":  return 0.0;
                                default:
                                        throw new RuntimeException("Invalid grade value: '" + str + "'. Must be a numeric value (e.g. 95.0, 4.0) or a standard letter grade (A+, A, A-, B+, B, C+, C, D, F).");
                        }
                }
        }

        public CourseFullResponseDTO getCourseAnalytics(UUID instructorId, Long courseId) {
                // 1️⃣ Validate course belongs to instructor
                Course course = courseRepository.findByIdAndInstructorId(courseId, instructorId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Course not found with id: " + courseId + " for instructor: "
                                                                + instructorId));

                // 2️⃣ Analytics calculations
                long enrolledStudents = enrollmentRepository.countByCourse(course);
                long activeLearners = enrollmentRepository.countActiveByCourse(course);
                double completionRate = enrollmentRepository.calculateCompletionRate(course);
                double averageRating = reviewRepository.averageRatingByCourse(course);
                long totalReviews = reviewRepository.countByCourse(course);
                double totalRevenue = paymentRepository.totalRevenueByCourse(course);

                // 3️⃣ Content info
                int sections = contentRepository.countSectionsByCourse(course);
                int lectures = contentRepository.countLecturesByCourse(course);
                int assignments = contentRepository.countAssignmentsByCourse(course);
                int quizzes = contentRepository.countQuizzesByCourse(course);
                int totalDurationMinutes = contentRepository.totalDurationByCourse(course);

                // 4️⃣ Instructor info
                Instructor instructor = course.getInstructor();
                String headline = instructor.getHeadline() != null ? instructor.getHeadline() : "Instructor";
                double instructorRating = reviewRepository.averageRatingByCourse(course);
                boolean verified = instructor.getVerified() != null ? instructor.getVerified() : true;

                // 5️⃣ Student progress
                List<StudentProgressResponseDTO.StudentProgressData> studentProgress = enrollmentRepository
                                .findByCourse(course).stream()
                                .map(e -> StudentProgressResponseDTO.StudentProgressData.builder()
                                                .studentId(e.getStudentId())
                                                .status(e.getStatus())
                                                .completionRate(e.getCompletionRate())
                                                .enrolledAt(e.getEnrolledAt())
                                                .lastActivityAt(e.getLastActivityAt())
                                                .build())
                                .toList();

                // 6️⃣ Build CourseData
                CourseFullResponseDTO.CourseData courseData = CourseFullResponseDTO.CourseData.builder()
                                .identity(CourseFullResponseDTO.Identity.builder()
                                                .courseId(course.getId())
                                                .slug(course.getSlug())
                                                .build())
                                .core(CourseFullResponseDTO.Core.builder()
                                                .title(course.getTitle())
                                                .subtitle(course.getSubtitle())
                                                .description(course.getDescription())
                                                .language(course.getLanguage() != null ? course.getLanguage() : "en")
                                                .level(course.getLevel() != null ? course.getLevel() : "BEGINNER")
                                                .category(course.getCategory())
                                                .tags(course.getTags())
                                                .build())
                                .status(CourseFullResponseDTO.Status.builder()
                                                .status(course.getStatus())
                                                .isPublished(course.getStatus() == Course.CourseStatus.PUBLISHED)
                                                .isArchived(course.getStatus() == Course.CourseStatus.ARCHIVED)
                                                .visibility(course.getStatus() == Course.CourseStatus.PUBLISHED
                                                                ? "PUBLIC"
                                                                : "PRIVATE")
                                                .approvalStatus(course.getStatus() == Course.CourseStatus.DRAFT
                                                                ? "PENDING"
                                                                : "APPROVED")
                                                .publishedBy(course.getStatus() == Course.CourseStatus.PUBLISHED
                                                                ? "INSTRUCTOR"
                                                                : null)
                                                .build())
                                .instructor(CourseFullResponseDTO.InstructorDTO.builder()
                                                .instructorId(instructor.getId())
                                                .name(instructor.getName())
                                                .email(instructor.getEmail())
                                                .headline(headline)
                                                .rating(instructorRating)
                                                .verified(verified)
                                                .build())
                                .content(CourseFullResponseDTO.Content.builder()
                                                .sections(sections)
                                                .lectures(lectures)
                                                .assignments(assignments)
                                                .quizzes(quizzes)
                                                .totalDurationMinutes(totalDurationMinutes)
                                                .build())
                                .analytics(CourseFullResponseDTO.Analytics.builder()
                                                .enrolledStudents((int) enrolledStudents)
                                                .activeLearners((int) activeLearners)
                                                .completionRate(completionRate)
                                                .averageRating(averageRating)
                                                .totalReviews((int) totalReviews)
                                                .totalRevenue(totalRevenue)
                                                .build())
                                .studentProgress(studentProgress) // ✅ Now works
                                .media(CourseFullResponseDTO.Media.builder()
                                                .thumbnailUrl(course.getThumbnailUrl())
                                                .previewVideoUrl(course.getPreviewVideoUrl())
                                                .build())
                                .timestamps(CourseFullResponseDTO.Timestamps.builder()
                                                .createdAt(course.getCreatedAt())
                                                .updatedAt(course.getUpdatedAt() != null ? course.getUpdatedAt()
                                                                : course.getCreatedAt())
                                                .publishedAt(course.getPublishedAt())
                                                .archivedAt(course.getArchivedAt())
                                                .build())
                                .links(CourseFullResponseDTO.Links.builder()
                                                .self("/api/courses/" + course.getId())
                                                .enroll("/api/courses/" + course.getId() + "/enroll")
                                                .reviews("/api/courses/" + course.getId() + "/reviews")
                                                .content("/api/courses/" + course.getId() + "/content")
                                                .build())
                                .build();

                // 7️⃣ Build full response
                return CourseFullResponseDTO.builder()
                                .success(true)
                                .message("Course analytics fetched successfully")
                                .requestId(UUID.randomUUID().toString())
                                .data(courseData)
                                .build();
        }
}