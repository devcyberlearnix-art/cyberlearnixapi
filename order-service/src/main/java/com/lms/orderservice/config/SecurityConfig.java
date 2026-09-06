package com.lms.orderservice.config;

import com.cyberlearnix.error.ApiSecurityErrorWriter;
import com.lms.orderservice.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {

        http
                .cors(cors -> cors.disable()) // Disable CORS - handled by API Gateway
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) ->
                                ApiSecurityErrorWriter.write(request, response, 401,
                                        "UNAUTHORIZED", "Authentication is required"))
                        .accessDeniedHandler((request, response, exception) ->
                                ApiSecurityErrorWriter.write(request, response, 403,
                                        "FORBIDDEN", "Insufficient permissions")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/orders/user/*")
                        .hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/orders/admin")
                        .hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/orders/*/status")
                        .hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")
                        .requestMatchers("/api/v1/orders/**").hasRole("STUDENT")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}