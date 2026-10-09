package com.lms.courseservice.config;

import com.lms.courseservice.security.JwtFilter;
import com.lms.courseservice.security.ServiceAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final ServiceAuthFilter serviceAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable()) // Disable CORS - handled by API Gateway
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.GET, "/api/v1/courses/stats")
                    .hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")
                        // ============== PUBLIC ENDPOINTS (No Auth Required) ==============
                        // GET all courses
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses").permitAll()
                        // GET trending courses (public landing page) - specific pattern first
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/trending").permitAll()
                        // GET specific course and course list (wildcard matches /list, /{id})
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/*").permitAll()
                        // GET course details (comprehensive course information)
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/*/details").permitAll()
                        // GET course curriculum (hierarchical structure)
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/*/curriculum").permitAll()
                        // GET course sections
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/*/sections").permitAll()
                        // GET lectures in section - completely public for testing
                        .requestMatchers(HttpMethod.GET, "/api/v1/sections/*/lectures").permitAll()
                        // GET course preview
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/*/preview").permitAll()
                        // GET course requirements
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/*/requirements").permitAll()
                        // GET learning outcomes
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/*/outcomes").permitAll()
                        // GET course materials
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/*/materials").permitAll()
                        // GET course FAQs
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/*/faqs").permitAll()
                        // Track anonymous home/search engagement for featured ranking
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/*/impressions").permitAll()

                        // ============== INTERNAL ENDPOINTS (Service-to-Service) ==============
                        // Internal enrollment (from payment service)
                        .requestMatchers(HttpMethod.POST, "/api/v1/enrollments/internal/enroll").permitAll()
                        // Enrollment lookup by user (from admin service)
                        .requestMatchers(HttpMethod.GET, "/api/v1/enrollments/users/*").permitAll()
                        // Admin service operations (with service token)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/courses/*/status").permitAll()

                        // ============== AUTHENTICATED ENDPOINTS ==============
                        // Enrollment check (requires auth)
                        .requestMatchers(HttpMethod.GET, "/api/v1/enrollments/check/*").authenticated()

                        // ============== STUDENT-ONLY ENDPOINTS ==============
                        // Enroll in course (Student)
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/*/enroll", "/api/v1/courses/*/enroll/")
                        .hasAnyRole("STUDENT", "USER", "INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        // ============== INSTRUCTOR/ADMIN ENDPOINTS ==============
                        // GET course students (instructor/admin dashboard)
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/*/students")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Create course
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses", "/api/v1/courses/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Update course (full)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/courses/*")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN", "ADMIN")
                        // Update course (partial)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/courses/*")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN", "ADMIN")
                        // Delete course
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/*")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN", "ADMIN")

                        // ============== SECTION MANAGEMENT ==============
                        // Create section
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/*/sections", "/api/v1/courses/*/sections/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Update section
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/courses/sections/*", "/api/v1/courses/sections/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Delete section
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/sections/*", "/api/v1/courses/sections/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        // ============== LECTURE MANAGEMENT ==============
                        // Create lecture
                        .requestMatchers(HttpMethod.POST, "/api/v1/sections/*/lectures", "/api/v1/sections/*/lectures/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Update lecture
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/sections/*/lectures/*", "/api/v1/sections/*/lectures/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Delete lecture
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/sections/*/lectures/*", "/api/v1/sections/*/lectures/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        // ============== COURSE PREVIEW ==============
                        // Create or update preview
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/*/preview", "/api/v1/courses/*/preview/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/courses/*/preview", "/api/v1/courses/*/preview/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        // ============== COURSE REQUIREMENTS MANAGEMENT ==============
                        // Create requirement
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/*/requirements", "/api/v1/courses/*/requirements/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Update requirement (full and partial)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/courses/requirements/*", "/api/v1/courses/requirements/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/courses/requirements/*", "/api/v1/courses/requirements/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Delete requirement
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/requirements/*", "/api/v1/courses/requirements/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        // ============== LEARNING OUTCOMES MANAGEMENT ==============
                        // Create outcome
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/*/outcomes", "/api/v1/courses/*/outcomes/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Update outcome (full and partial)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/courses/outcomes/*", "/api/v1/courses/outcomes/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/courses/outcomes/*", "/api/v1/courses/outcomes/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Delete outcome
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/outcomes/*", "/api/v1/courses/outcomes/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        // ============== COURSE MATERIALS MANAGEMENT ==============
                        // Create material
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/*/materials", "/api/v1/courses/*/materials/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Update material (full and partial)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/courses/materials/*", "/api/v1/courses/materials/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/courses/materials/*", "/api/v1/courses/materials/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Delete material
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/materials/*", "/api/v1/courses/materials/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        // ============== COURSE FAQ MANAGEMENT ==============
                        // Create FAQ
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/*/faqs", "/api/v1/courses/*/faqs/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Update FAQ (full and partial)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/courses/faqs/*", "/api/v1/courses/faqs/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/courses/faqs/*", "/api/v1/courses/faqs/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Delete FAQ
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/faqs/*", "/api/v1/courses/faqs/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        // ============== MEDIA UPLOAD ==============
                        // Upload video to Cloudinary (Instructor/Admin only)
                        .requestMatchers(HttpMethod.POST, "/api/v1/upload/video", "/api/v1/upload/video/", "/api/v1/courses/upload/video", "/api/v1/courses/upload/video/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Upload banner image (Admin only)
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/banners/upload-image")
                        .hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")

                        // ============== BANNER MANAGEMENT (ADMIN) ==============
                        // Banner CRUD operations (Admin only)
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/banners")
                        .hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/banners")
                        .hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/banners/*")
                        .hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/banners/*")
                        .hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/admin/banners/*")
                        .hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/admin/banners/*")
                        .hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/admin/banners/reorder")
                        .hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/banners/*/analytics")
                        .hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")

                        // ============== PUBLIC BANNER ENDPOINTS ==============
                        // Get active banners (public)
                        .requestMatchers(HttpMethod.GET, "/api/v1/banners").permitAll()
                        // Track banner impressions and clicks (public)
                        .requestMatchers(HttpMethod.POST, "/api/v1/banners/*/impression").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/banners/*/click").permitAll()

                        // ============== PROGRESS TRACKING (Authenticated Students) ==============
                        // Start lecture
                        .requestMatchers(HttpMethod.POST, "/api/v1/progress/lectures/*/start").authenticated()
                        // Update lecture progress
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/progress/lectures/*").authenticated()
                        // Complete lecture
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/progress/lectures/*/complete").authenticated()
                        // Get lecture progress
                        .requestMatchers(HttpMethod.GET, "/api/v1/progress/lectures/*").authenticated()
                        // Get course progress
                        .requestMatchers(HttpMethod.GET, "/api/v1/progress/courses/*").authenticated()
                        // Resume learning
                        .requestMatchers(HttpMethod.GET, "/api/v1/progress/courses/*/resume").authenticated()

                        // ============== QUIZ MANAGEMENT (Instructor/Admin) ==============
                        // Create quiz
                        .requestMatchers(HttpMethod.POST, "/api/v1/quizzes", "/api/v1/quizzes/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Update quiz
                        .requestMatchers(HttpMethod.PUT, "/api/v1/quizzes/*")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Delete quiz
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/quizzes/*")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Publish/Unpublish quiz
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/quizzes/*/publish", "/api/v1/quizzes/*/unpublish")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Add question to quiz
                        .requestMatchers(HttpMethod.POST, "/api/v1/quizzes/*/questions", "/api/v1/quizzes/*/questions/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Update question
                        .requestMatchers(HttpMethod.PUT, "/api/v1/quizzes/questions/*")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Delete question
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/quizzes/questions/*")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        // ============== QUIZ STUDENT APIs ==============
                        // Get student quiz
                        .requestMatchers(HttpMethod.GET, "/api/v1/quizzes/*/student", "/api/v1/quizzes/*/student/questions")
                        .authenticated()
                        // Start quiz attempt
                        .requestMatchers(HttpMethod.POST, "/api/v1/quizzes/*/attempts", "/api/v1/quizzes/*/attempts/")
                        .authenticated()
                        // Get current attempt
                        .requestMatchers(HttpMethod.GET, "/api/v1/quizzes/*/attempts/current")
                        .authenticated()
                        // Submit answer
                        .requestMatchers(HttpMethod.POST, "/api/v1/quizzes/*/attempts/*/answers", "/api/v1/quizzes/*/attempts/*/answers/")
                        .authenticated()
                        // Submit quiz attempt
                        .requestMatchers(HttpMethod.POST, "/api/v1/quizzes/*/attempts/*/submit", "/api/v1/quizzes/*/attempts/*/submit/")
                        .authenticated()
                        // Get attempt result
                        .requestMatchers(HttpMethod.GET, "/api/v1/quizzes/*/attempts/*/result")
                        .authenticated()
                        // Get student attempts
                        .requestMatchers(HttpMethod.GET, "/api/v1/quizzes/*/attempts")
                        .authenticated()

                        // ============== CERTIFICATE PUBLIC VERIFICATION ==============
                        // Verify certificate (public) - must come before /api/v1/certificates/*
                        .requestMatchers(HttpMethod.GET, "/api/v1/certificates/verify/*")
                        .permitAll()

                        // ============== CERTIFICATE STUDENT APIs ==============
                        // Get course certificate
                        .requestMatchers(HttpMethod.GET, "/api/v1/certificates/courses/*")
                        .hasAnyRole("STUDENT", "USER", "INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Get certificate by ID
                        .requestMatchers(HttpMethod.GET, "/api/v1/certificates/*")
                        .hasAnyRole("STUDENT", "USER", "INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Get my certificates
                        .requestMatchers(HttpMethod.GET, "/api/v1/certificates/my")
                        .hasAnyRole("STUDENT", "USER", "INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        // ============== CERTIFICATE ADMIN APIs ==============
                        // Get certificates by course
                        .requestMatchers(HttpMethod.GET, "/api/v1/certificates/admin/courses/*")
                        .hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")
                        // Revoke certificate
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/certificates/admin/*/revoke")
                        .hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")

                        // ============== LIVE CLASS INSTRUCTOR/ADMIN APIs ==============
                        // Create live class
                        .requestMatchers(HttpMethod.POST, "/api/v1/live-classes")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Update live class
                        .requestMatchers(HttpMethod.PUT, "/api/v1/live-classes/*")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Get live class by ID
                        .requestMatchers(HttpMethod.GET, "/api/v1/live-classes/*")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Get live classes by course
                        .requestMatchers(HttpMethod.GET, "/api/v1/live-classes/course/*")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Get live classes by section
                        .requestMatchers(HttpMethod.GET, "/api/v1/live-classes/section/*")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Start live class
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/live-classes/*/start")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Complete live class
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/live-classes/*/complete")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Cancel live class
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/live-classes/*/cancel")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Delete live class
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/live-classes/*")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        // ============== LIVE CLASS STUDENT APIs ==============
                        // Get upcoming live classes for course
                        .requestMatchers(HttpMethod.GET, "/api/v1/live-classes/course/*/upcoming")
                        .hasAnyRole("STUDENT", "USER", "INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Join live class
                        .requestMatchers(HttpMethod.GET, "/api/v1/live-classes/*/join")
                        .hasAnyRole("STUDENT", "USER", "INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        // ============== LIVE CLASS ATTENDANCE STUDENT APIs ==============
                        // Join live class attendance
                        .requestMatchers(HttpMethod.POST, "/api/v1/live-classes/*/attendance/join")
                        .hasAnyRole("STUDENT", "USER", "INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Leave live class attendance
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/live-classes/*/attendance/leave")
                        .hasAnyRole("STUDENT", "USER", "INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Get my attendance
                        .requestMatchers(HttpMethod.GET, "/api/v1/live-classes/*/attendance/me")
                        .hasAnyRole("STUDENT", "USER", "INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        // ============== LIVE CLASS ATTENDANCE INSTRUCTOR/ADMIN APIs ==============
                        // Get attendance for live class
                        .requestMatchers(HttpMethod.GET, "/api/v1/live-classes/*/attendance")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Get attendance summary
                        .requestMatchers(HttpMethod.GET, "/api/v1/live-classes/*/attendance/summary")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        // ============== STUDENT DASHBOARD APIS ==============
                        // Get student dashboard overview
                        .requestMatchers(HttpMethod.GET, "/api/v1/dashboard/student")
                        .hasAnyRole("STUDENT", "USER")
                        // Get student courses
                        .requestMatchers(HttpMethod.GET, "/api/v1/dashboard/student/courses")
                        .hasAnyRole("STUDENT", "USER")
                        // Get course-specific dashboard
                        .requestMatchers(HttpMethod.GET, "/api/v1/dashboard/student/courses/*")
                        .hasAnyRole("STUDENT", "USER")

                        // Default: deny all other requests
                        .anyRequest().denyAll())

                .addFilterBefore(serviceAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
