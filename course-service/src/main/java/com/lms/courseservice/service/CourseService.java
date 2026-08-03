package com.lms.courseservice.service;

import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Enrollment;
import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.EnrollmentRepository;
import com.lms.courseservice.exception.EnrollmentException;
import com.lms.courseservice.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final LectureRepository lectureRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public Course createCourse(Course course) {
        course.setStatus("Draft");
        return courseRepository.save(course);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
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

        return courseRepository.save(course);
    }

    public void deleteCourse(Long id) {
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

    // 🔥 FIXED → UUID
    public void enrollFreeCourse(Long courseId, UUID userId) {
        Course course = getCourseById(courseId);

        BigDecimal price = course.getPrice() == null
                ? BigDecimal.ZERO
                : course.getPrice();

        if (price.compareTo(BigDecimal.ZERO) > 0) {
            throw new EnrollmentException(
                    "This is a paid course. Please complete payment first.");
        }

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

        enrollmentRepository.save(enrollment);
    }
}