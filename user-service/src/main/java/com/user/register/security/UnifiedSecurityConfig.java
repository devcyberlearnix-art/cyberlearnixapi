package com.user.register.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class UnifiedSecurityConfig {

    private final UnifiedJwtAuthenticationFilter unifiedJwtAuthenticationFilter;
    private final UnifiedJwtAuthenticationEntryPoint unauthorizedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/verify-email").permitAll()
                        .requestMatchers("/api/v1/auth/login/otp/**").permitAll()
                        .requestMatchers("/api/v1/auth/password/**").permitAll()
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/auth/logout").permitAll()
                        .requestMatchers("/api/v1/auth/switch-role").permitAll()
                        
                        // OAuth2 endpoints
                        .requestMatchers("/oauth2/**").permitAll()
                        
                        // Health check endpoints
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        
                        // Swagger documentation
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        
                        // H2 console
                        .requestMatchers("/h2-console/**").permitAll()
                        
                        // Public profile endpoints
                        .requestMatchers("/api/v1/public/**").permitAll()
                        
                        // User management endpoints (require authentication)
                        .requestMatchers("/api/v1/users/me").authenticated()
                        .requestMatchers("/api/v1/users/me/photo").authenticated()
                        .requestMatchers("/api/v1/users/me/sessions/**").authenticated()
                        
                        // Admin user management endpoints (require admin role or service token)
                        .requestMatchers("/api/v1/users").permitAll()
                        .requestMatchers("/api/v1/users/{id}").permitAll()
                        .requestMatchers("/api/v1/users/{id}/status").permitAll()
                        .requestMatchers("/api/v1/users/{id}/**").permitAll()
                        
                        // Instructor application endpoints
                        .requestMatchers("/api/v1/instructors/applications").authenticated()
                        .requestMatchers("/api/v1/instructors/applications/me").authenticated()
                        
                        // Student endpoints
                        .requestMatchers("/api/v1/students/**").hasAnyRole("STUDENT", "INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        
                        // Instructor endpoints
                        .requestMatchers("/api/v1/instructors/**").hasAnyRole("INSTRUCTOR", "MAIN_ADMIN", "SUB_ADMIN")
                        
                        // Admin endpoints
                        .requestMatchers("/api/v1/admins/**").hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")
                        
                        // Super Admin only endpoints
                        .requestMatchers("/api/v1/super-admin/**").hasRole("MAIN_ADMIN")
                        
                        // Course management
                        .requestMatchers("/api/v1/courses/**").authenticated()
                        
                        // Payment endpoints
                        .requestMatchers("/api/v1/payments/**").authenticated()
                        
                        // Any other request requires authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(unifiedJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
