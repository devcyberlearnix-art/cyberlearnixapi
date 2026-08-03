package com.example.instructorservice.service;

import com.example.instructorservice.dto.CourseEarningDTO;
import com.example.instructorservice.dto.DashboardResponseDTO;
import com.example.instructorservice.dto.InstructorEarningsResponse;
import com.example.instructorservice.dto.MonthlyEarningDTO;
import com.example.instructorservice.entity.Course;
import com.example.instructorservice.repository.CourseRepository;
import com.example.instructorservice.repository.ContentRepository;
import com.example.instructorservice.repository.EnrollmentRepository;
import com.example.instructorservice.repository.PaymentRepository;
import com.example.instructorservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final ContentRepository contentRepository;

    public DashboardResponseDTO getDashboard(UUID instructorId) {

        // Fetch all courses of instructor
        List<Course> courses = courseRepository.findByInstructorId(instructorId);

        int totalCourses = courses.size();
        long publishedCount = courses.stream().filter(c -> c.getStatus() == Course.CourseStatus.PUBLISHED).count();
        long draftCount = courses.stream().filter(c -> c.getStatus() == Course.CourseStatus.DRAFT).count();
        long archivedCount = courses.stream().filter(c -> c.getStatus() == Course.CourseStatus.ARCHIVED).count();

        int totalStudents = courses.stream()
                .mapToInt(course -> (int) enrollmentRepository.countByCourse(course))
                .sum();

        double totalRevenue = courses.stream()
                .mapToDouble(course ->
                        java.util.Optional.ofNullable(
                                paymentRepository.totalRevenueByCourse(course)
                        ).orElse(0.0)
                )
                .sum();
        double averageRating = courses.stream()
                .mapToDouble(course ->
                        java.util.Optional.ofNullable(
                                reviewRepository.averageRatingByCourse(course)
                        ).orElse(0.0)
                )
                .average()
                .orElse(0.0);
        // Per-course detailed data
        List<DashboardResponseDTO.CourseData> courseDataList = courses.stream()
                .map(course -> {
                    int enrolled = (int) enrollmentRepository.countByCourse(course);
                    double revenue = paymentRepository.totalRevenueByCourse(course);
                    double rating = reviewRepository.averageRatingByCourse(course);
                    double completion = enrollmentRepository.calculateCompletionRate(course);

                    DashboardResponseDTO.ContentSummary contentSummary = DashboardResponseDTO.ContentSummary.builder()
                            .sections(contentRepository.countSectionsByCourse(course))
                            .lectures(contentRepository.countLecturesByCourse(course))
                            .assignments(contentRepository.countAssignmentsByCourse(course))
                            .quizzes(contentRepository.countQuizzesByCourse(course))
                            .totalDurationMinutes(contentRepository.totalDurationByCourse(course))
                            .build();

                    return DashboardResponseDTO.CourseData.builder()
                            .courseId(course.getId())
                            .title(course.getTitle())
                            .slug(course.getSlug())
                            .status(course.getStatus().name())
                            .enrolledStudents(enrolled)
                            .revenue(revenue)
                            .averageRating(rating)
                            .completionRate(completion)
                            .createdAt(course.getCreatedAt())
                            .publishedAt(course.getPublishedAt())
                            .archivedAt(course.getArchivedAt())
                            .contentSummary(contentSummary)
                            .build();
                }).collect(Collectors.toList());

        DashboardResponseDTO.DashboardData dashboardData = DashboardResponseDTO.DashboardData.builder()
                .totalCourses(totalCourses)
                .publishedCourses((int) publishedCount)
                .draftCourses((int) draftCount)
                .archivedCourses((int) archivedCount)
                .totalStudents(totalStudents)
                .totalRevenue(totalRevenue)
                .averageRating(averageRating)
                .courses(courseDataList)
                .build();

        return DashboardResponseDTO.builder()
                .success(true)
                .message("Instructor dashboard fetched successfully")
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .data(dashboardData)
                .build();
    }

    public InstructorEarningsResponse getInstructorEarnings(UUID instructorId) {

        List<Course> courses = courseRepository.findByInstructorId(instructorId);

        double totalRevenue = 0.0;
        long totalEnrollments = 0;

        List<CourseEarningDTO> courseEarnings = new ArrayList<>();

        for (Course course : courses) {

            long enrollments = Optional.ofNullable(
                    enrollmentRepository.countByCourse(course)
            ).orElse(0L);

            double revenue = Optional.ofNullable(
                    paymentRepository.totalRevenueByCourse(course)
            ).orElse(0.0);

            totalRevenue += revenue;
            totalEnrollments += enrollments;

            courseEarnings.add(
                    CourseEarningDTO.builder()
                            .courseId(course.getId())
                            .title(course.getTitle())
                            .price(course.getPrice())
                            .enrollments(enrollments)
                            .revenue(revenue)
                            .build()
            );
        }

        // 🔥 Monthly earnings (SAFE)
        List<MonthlyEarningDTO> monthlyEarnings =
                Optional.ofNullable(
                        paymentRepository.getMonthlyEarningsByInstructor(instructorId)
                ).orElse(new ArrayList<>());

        double platformFee = totalRevenue * 0.10;
        double netEarnings = totalRevenue - platformFee;

        return InstructorEarningsResponse.builder()
                .instructorId(instructorId)
                .totalRevenue(totalRevenue)
                .netEarnings(netEarnings)
                .platformFee(platformFee)
                .totalEnrollments(totalEnrollments)
                .totalCourses(courses.size())
                .courseEarnings(courseEarnings)
                .monthlyEarnings(monthlyEarnings)
                .build();
    }
}