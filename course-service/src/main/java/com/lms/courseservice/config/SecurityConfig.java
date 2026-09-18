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

                        // Default: deny all other requests
                        .anyRequest().denyAll())

                .addFilterBefore(serviceAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}