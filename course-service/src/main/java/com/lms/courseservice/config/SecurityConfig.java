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

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ============== PUBLIC ENDPOINTS (No Auth Required) ==============
                        // GET all courses
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses").permitAll()
                        // GET specific course
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/*").permitAll()
                        // GET course sections
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/*/sections").permitAll()
                        // GET lectures in section
                        .requestMatchers(HttpMethod.GET, "/api/v1/sections/*/lectures").permitAll()
                        // GET course preview
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/*/preview").permitAll()
                        // GET course students (for admin dashboard)
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/*/students").permitAll()

                        // ============== INTERNAL ENDPOINTS (Service-to-Service) ==============
                        // Internal enrollment (from payment service)
                        .requestMatchers(HttpMethod.POST, "/api/v1/enrollments/internal/enroll").permitAll()
                        // Admin service operations (with service token)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/courses/*").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/courses/*").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/courses/*/status").permitAll()

                        // ============== AUTHENTICATED ENDPOINTS ==============
                        // Enrollment check (requires auth)
                        .requestMatchers(HttpMethod.GET, "/api/v1/enrollments/check/*").authenticated()

                        // ============== STUDENT-ONLY ENDPOINTS ==============
                        // Enroll in course (Student)
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/*/enroll", "/api/v1/courses/*/enroll/")
                        .hasAnyRole("STUDENT", "USER", "INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN", "ADMIN")

                        // ============== INSTRUCTOR/ADMIN ENDPOINTS ==============
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
                        // Create preview
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/*/preview", "/api/v1/courses/*/preview/")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN", "ADMIN")

                        // Default: deny all other requests
                        .anyRequest().denyAll())

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}