package com.example.instructorservice.config;

import com.example.instructorservice.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/courses/**").permitAll()

                        // Student can submit instructor application
                        .requestMatchers(HttpMethod.POST, "/api/v1/instructors/applications")
                        .hasRole("STUDENT")

                        // Student can view application status
                        .requestMatchers(HttpMethod.GET, "/api/v1/instructors/applications/**")
                        .hasRole("STUDENT")

                        // Instructor/Admin APIs
                        .requestMatchers("/api/v1/instructors/**")
                        .hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}