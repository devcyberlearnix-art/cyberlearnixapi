package com.user.register.security;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration

@EnableWebSecurity

@EnableMethodSecurity

@RequiredArgsConstructor

public class UnifiedSecurityConfig {

    private final UnifiedJwtAuthenticationFilter unifiedJwtAuthenticationFilter;

    private final UnifiedJwtAuthenticationEntryPoint unauthorizedHandler;
    private final ServiceAuthFilter serviceAuthFilter;

    @Bean

    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable()) // Disable CORS - handled by API Gateway

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))

                .authorizeHttpRequests(auth -> auth

                        // Public endpoints
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/auth/login",
                                "/api/v1/auth/login/**",
                                "/api/v1/auth/register",
                                "/api/v1/auth/register/**",
                                "/api/v1/auth/register/email",
                                "/api/v1/auth/register/resend-otp",
                                "/api/v1/auth/verify-email",
                                "/api/v1/auth/login/otp/**",
                                "/api/v1/auth/password/**",
                                "/api/v1/auth/otp/**",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout",
                                "/api/v1/auth/switch-role",
                                "/api/v1/auth/change-password",
                                "/api/v1/auth/upload/profile-photo")
                        .permitAll()

                        // OAuth2 endpoints

                        .requestMatchers("/oauth2/**").permitAll()

                        // Health check endpoints

                        .requestMatchers("/actuator/health").permitAll()

                        .requestMatchers("/actuator/info").permitAll()

                        .requestMatchers("/api/v1/admin/internal/users/*/refresh-eligibility").permitAll()

                        // Swagger documentation

                        .requestMatchers("/swagger-ui/**").permitAll()

                        .requestMatchers("/v3/api-docs/**").permitAll()

                        // H2 console

                        .requestMatchers("/h2-console/**").permitAll()

                        // Public profile endpoints

                        .requestMatchers("/api/v1/public/**").permitAll()

                        .requestMatchers("/api/v1/users/public/upload-photo").permitAll()

                        // User management endpoints (require authentication)

                        .requestMatchers("/api/v1/users/me").authenticated()

                        .requestMatchers("/api/v1/users/me/photo").permitAll()

                        .requestMatchers("/api/v1/users/me/sessions/**").authenticated()

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/{id}")
                        .hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")

                        // Email change — all three steps require an authenticated user
                        .requestMatchers("/api/v1/users/email/**").authenticated()

                        // Password change — requires authentication
                        .requestMatchers("/api/v1/users/change-password/**").authenticated()

                        // Admin user management endpoints (require admin role or service token)

                        .requestMatchers("/api/v1/users/stats").hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")

                        .requestMatchers("/api/v1/users").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/users/{id}").permitAll()

                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/{id}/status")
                        .hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")

                        // Instructor application endpoints

                        .requestMatchers("/api/v1/instructors/applications").authenticated()

                        .requestMatchers("/api/v1/instructors/applications/me").authenticated()

                        // Student endpoints

                        .requestMatchers("/api/v1/students/**")
                        .hasAnyRole("STUDENT", "INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        // Instructor endpoints

                        .requestMatchers("/api/v1/instructors/**").hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        // Admin endpoints

                        .requestMatchers("/api/v1/admins/**").hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")

                        .requestMatchers("/api/v1/admin/instructors/**").authenticated() // Business logic handles role
                                                                                         // check

                        // Super Admin only endpoints

                        .requestMatchers("/api/v1/super-admin/**").hasRole("MAIN_ADMIN")

                        // Course management

                        .requestMatchers("/api/v1/courses/**").authenticated()

                        // Payment endpoints

                        .requestMatchers("/api/v1/payments/**").authenticated()

                        // Any other request requires authentication

                        .anyRequest().authenticated()

                )

                .addFilterBefore(serviceAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(unifiedJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

    @Bean

    public BCryptPasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }

}
