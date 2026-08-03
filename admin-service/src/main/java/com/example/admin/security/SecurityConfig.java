package com.example.admin.security;



import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;



@Configuration

@RequiredArgsConstructor

public class SecurityConfig {



    private final JwtAuthFilter jwtAuthFilter;

    private final AdminAuthorizationFilter adminAuthorizationFilter;



    @Bean

    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {



        http

                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Public endpoints (sub-admin registration, and login flows called
                        // internally by User Service's unified /api/v1/auth/** fallback)
                        .requestMatchers(
                                "/api/v1/admins/register",
                                "/api/v1/admin/register",
                                "/api/v1/admin/login",
                                "/api/v1/admin/login/otp/request",
                                "/api/v1/admin/login/otp/verify"
                        ).permitAll()

                        // All other admin endpoints require authentication
                        .requestMatchers("/api/v1/admin/**").authenticated()

                        .anyRequest().denyAll())

                .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)

                .addFilterAfter(adminAuthorizationFilter, JwtAuthFilter.class)

                .httpBasic(httpBasic -> httpBasic.disable())

                .formLogin(form -> form.disable());



        return http.build();

    }



    @Bean

    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }

}
