package com.lms.courseservice.config;

import com.lms.courseservice.security.JwtFilter;
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
                        // GET course sections
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/*/sections").permitAll()
                        // GET lectures in section
                        .requestMatchers(HttpMethod.GET, "/api/v1/sections/*/lectures").permitAll()
                        // GET course preview
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/*/preview").permitAll()
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
                        .hasAnyRole("STUDENT", "USER", "INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN", "ADMIN")

                        // ============== INSTRUCTOR/ADMIN ENDPOINTS ==============
                        // GET course students (instructor/admin dashboard)
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/*/students")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN", "ADMIN")
                        // Create course
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses", "/api/v1/courses/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN", "ADMIN")
                        // Update course (full)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/courses/*")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Update course (partial)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/courses/*")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        // Delete course
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/*")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        // ============== SECTION MANAGEMENT ==============
                        // Create section
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/*/sections", "/api/v1/courses/*/sections/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN", "ADMIN")
                        // Update section
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/courses/sections/*", "/api/v1/courses/sections/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN", "ADMIN")
                        // Delete section
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/sections/*", "/api/v1/courses/sections/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN", "ADMIN")

                        // ============== LECTURE MANAGEMENT ==============
                        // Create lecture
                        .requestMatchers(HttpMethod.POST, "/api/v1/sections/*/lectures", "/api/v1/sections/*/lectures/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN", "ADMIN")
                        // Update lecture
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/sections/*/lectures/*", "/api/v1/sections/*/lectures/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN", "ADMIN")
                        // Delete lecture
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/sections/*/lectures/*", "/api/v1/sections/*/lectures/*/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN", "ADMIN")

                        // ============== COURSE PREVIEW ==============
                        // Create or update preview
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/*/preview", "/api/v1/courses/*/preview/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/courses/*/preview", "/api/v1/courses/*/preview/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN", "ADMIN")

                        // ============== MEDIA UPLOAD ==============
                        // Upload video to Cloudinary (Instructor/Admin only)
                        .requestMatchers(HttpMethod.POST, "/api/v1/upload/video", "/api/v1/upload/video/", "/api/v1/courses/upload/video", "/api/v1/courses/upload/video/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN", "ADMIN")

                        // Default: deny all other requests
                        .anyRequest().denyAll())

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}