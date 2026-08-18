package com.lms.courseservice.service;

import com.lms.courseservice.dto.CourseRatingSummary;
import com.lms.courseservice.dto.FeaturedCourseResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Enrollment;
import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.EnrollmentRepository;
import com.lms.courseservice.exception.EnrollmentException;
import com.lms.courseservice.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lms.courseservice.entity.CoursePreview;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.repository.CoursePreviewRepository;
import com.lms.courseservice.repository.SectionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final LectureRepository lectureRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    private final CoursePreviewRepository coursePreviewRepository;
    private final SectionRepository sectionRepository;

    private final ReviewRatingClient reviewRatingClient;

    public Course createCourse(Course course) {
        if (course.getStatus() == null || course.getStatus().isBlank()) {
            course.setStatus("DRAFT");
        }
        return courseRepository.save(course);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Map<String, Object> getCourseStats() {
        List<Course> courses = courseRepository.findAll();
        long published = courses.stream().filter(course -> "PUBLISHED".equalsIgnoreCase(course.getStatus())).count();
        long draft = courses.stream().filter(course -> "DRAFT".equalsIgnoreCase(course.getStatus())).count();
        long archived = courses.stream().filter(course -> "ARCHIVED".equalsIgnoreCase(course.getStatus())).count();
        return Map.of(
                "totalCourses", courses.size(),
                "publishedCourses", published,
                "draftCourses", draft,
                "archivedCourses", archived);
    }

            public List<FeaturedCourseResponse> getFeaturedCourses(int requestedLimit) {
            int limit = Math.max(1, Math.min(requestedLimit, 12));
            List<FeaturedCandidate> candidates = courseRepository.findByStatusIgnoreCase("PUBLISHED").stream()
                .map(course -> new FeaturedCandidate(
                    course,
                    reviewRatingClient.getCourseRating(course.getId()),
                    enrollmentRepository.countByCourseId(course.getId())))
                .toList();

            long maxEnrollments = candidates.stream().mapToLong(FeaturedCandidate::enrollments).max().orElse(0L);
            long maxSearches = candidates.stream().mapToLong(candidate -> valueOrZero(candidate.course().getSearchCount())).max().orElse(0L);
            long maxViews = candidates.stream().mapToLong(candidate -> valueOrZero(candidate.course().getViewCount())).max().orElse(0L);

            return candidates.stream()
                .map(candidate -> toFeaturedCourse(candidate, maxEnrollments, maxSearches, maxViews))
                .sorted(Comparator.comparingDouble(FeaturedCourseResponse::getFeaturedScore).reversed()
                    .thenComparing(Comparator.comparingDouble(FeaturedCourseResponse::getRating).reversed())
                    .thenComparing(Comparator.comparingLong(FeaturedCourseResponse::getStudents).reversed())
                    .thenComparing(FeaturedCourseResponse::getId))
                .limit(limit)
                .toList();
            }

            @Transactional
            public void trackImpression(Long courseId, String source) {
            int updated = "SEARCH".equalsIgnoreCase(source)
                ? courseRepository.incrementSearchAndViewCount(courseId)
                : courseRepository.incrementViewCount(courseId);
            if (updated == 0) {
                throw new RuntimeException("Course not found with id: " + courseId);
            }
            }

            private FeaturedCourseResponse toFeaturedCourse(
                FeaturedCandidate candidate,
                long maxEnrollments,
                long maxSearches,
                long maxViews) {
            Course course = candidate.course();
            CourseRatingSummary ratingSummary = candidate.ratingSummary();
            double averageRating = ratingSummary.getAverageRating() == null ? 0.0 : ratingSummary.getAverageRating();
            long totalReviews = valueOrZero(ratingSummary.getTotalReviews());
            long searchCount = valueOrZero(course.getSearchCount());
            long viewCount = valueOrZero(course.getViewCount());
            double reviewConfidence = Math.min(1.0, Math.log1p(totalReviews) / Math.log1p(20));
            double score = (Boolean.TRUE.equals(course.getPremium()) ? 20.0 : 0.0)
                + (averageRating / 5.0 * 35.0 * reviewConfidence)
                + normalizedLogScore(candidate.enrollments(), maxEnrollments, 25.0)
                + normalizedLogScore(searchCount, maxSearches, 15.0)
                + normalizedLogScore(viewCount, maxViews, 5.0);

            return FeaturedCourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .subtitle(course.getSubtitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .level(course.getLevel())
                .language(course.getLanguage())
                .price(course.getPrice())
                .thumbnail(course.getThumbnail())
                .instructorId(course.getInstructorId())
                .status(course.getStatus())
                .premium(Boolean.TRUE.equals(course.getPremium()))
                .students(candidate.enrollments())
                .rating(Math.round(averageRating * 10.0) / 10.0)
                .totalReviews(totalReviews)
                .searchCount(searchCount)
                .viewCount(viewCount)
                .featuredScore(Math.round(score * 100.0) / 100.0)
                .tag(Boolean.TRUE.equals(course.getPremium()) ? "Premium" : averageRating >= 4.5 ? "Top Rated" : "Popular")
                .build();
            }

            private double normalizedLogScore(long value, long maximum, double weight) {
            return maximum == 0 ? 0.0 : Math.log1p(value) / Math.log1p(maximum) * weight;
            }

            private long valueOrZero(Long value) {
            return value == null ? 0L : value;
            }

            private record FeaturedCandidate(Course course, CourseRatingSummary ratingSummary, long enrollments) {
            }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
    }

    public Course updateCourse(Long id, Course updatedCourse) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        if (updatedCourse.getTitle() != null)
            course.setTitle(updatedCourse.getTitle());

        if (updatedCourse.getSubtitle() != null)
            course.setSubtitle(updatedCourse.getSubtitle());

        if (updatedCourse.getDescription() != null)
            course.setDescription(updatedCourse.getDescription());

        if (updatedCourse.getCategory() != null)
            course.setCategory(updatedCourse.getCategory());

        if (updatedCourse.getLevel() != null)
            course.setLevel(updatedCourse.getLevel());

        if (updatedCourse.getLanguage() != null)
            course.setLanguage(updatedCourse.getLanguage());

        if (updatedCourse.getPrice() != null)
            course.setPrice(updatedCourse.getPrice());

        if (updatedCourse.getThumbnail() != null)
            course.setThumbnail(updatedCourse.getThumbnail());

        if (updatedCourse.getStatus() != null)
            course.setStatus(updatedCourse.getStatus());

        if (updatedCourse.getInstructorId() != null)
            course.setInstructorId(updatedCourse.getInstructorId());

        if (updatedCourse.getPremium() != null)
            course.setPremium(updatedCourse.getPremium());

        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        // 1. Check if course exists
        getCourseById(id);

        // 2. Delete Course Previews
        List<CoursePreview> previews = coursePreviewRepository.findByCourseId(id);
        if (!previews.isEmpty()) {
            coursePreviewRepository.deleteAll(previews);
        }

        // 3. Delete Enrollments
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(id);
        if (!enrollments.isEmpty()) {
            enrollmentRepository.deleteAll(enrollments);
        }

        // 4. Delete Sections and their Lectures
        List<Section> sections = sectionRepository.findByCourseId(id);
        for (Section section : sections) {
            List<Lecture> lectures = lectureRepository.findBySectionId(section.getId());
            if (!lectures.isEmpty()) {
                lectureRepository.deleteAll(lectures);
            }
        }
        if (!sections.isEmpty()) {
            sectionRepository.deleteAll(sections);
        }

        // 5. Finally delete the course
        courseRepository.deleteById(id);
    }

    public Lecture enableLecturePreview(Long courseId, Long lectureId) {

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));

        if (!lecture.getSection().getCourse().getId().equals(courseId)) {
            throw new RuntimeException("Lecture does not belong to this course");
        }

        lecture.setPreviewEnabled(true);

        return lectureRepository.save(lecture);
    }

    // 🔥 FIXED → UUID
    // 🔥 FIXED → UUID
    public List<UUID> getStudents(Long courseId) {

        return enrollmentRepository.findByCourseId(courseId)
                .stream()
                .map(Enrollment::getStudentId)
                .toList();
    }

    public List<com.lms.courseservice.dto.EnrolledStudentInfo> getEnrolledStudentDetails(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId)
                .stream()
                .map(e -> new com.lms.courseservice.dto.EnrolledStudentInfo(
                        e.getStudentId(),
                        e.getStudentName() != null ? e.getStudentName() : (e.getStudentId() != null ? "Student " + e.getStudentId().toString().substring(0, 8) : "Student"),
                        e.getEnrolledAt() != null ? e.getEnrolledAt() : java.time.LocalDateTime.now(),
                        e.getStatus() != null ? e.getStatus() : "Active",
                        e.getProgress() != null ? e.getProgress() : 0.0
                ))
                .toList();
    }

    // Enroll student in course (free or paid)
    public void enrollFreeCourse(Long courseId, UUID userId) {
        // Verify course exists
        getCourseById(courseId);
        createEnrollment(courseId, userId);
    }

    public void enrollAfterPayment(Long courseId, UUID userId) {
        createEnrollment(courseId, userId);
    }

    private void createEnrollment(Long courseId, UUID userId) {

        if (enrollmentRepository.existsByStudentIdAndCourseId(userId, courseId)) {
            throw new EnrollmentException("Student is already enrolled.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(userId);
        enrollment.setEnrolledAt(java.time.LocalDateTime.now());
        enrollment.setStatus("ACTIVE");

        enrollmentRepository.save(enrollment);
    }
}