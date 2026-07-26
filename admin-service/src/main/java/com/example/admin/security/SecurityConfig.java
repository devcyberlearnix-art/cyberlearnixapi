package com.example.admin.security;



import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.config.Customizer;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

                        .requestMatchers(

                                "/api/v1/admins/register",

                                "/api/v1/admin/register",

                                "/api/v1/admin/verify-email",

                                "/api/v1/admin/resend-otp",

                                "/api/v1/admin/password/forgot",

                                "/api/v1/admin/password/reset")

                        .permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/users").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/orders", "/api/v1/admin/payments", "/api/v1/admin/reviews").permitAll()

                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/courses/*/approve", "/api/v1/admin/courses/*/reject").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/courses", "/api/v1/admin/courses/*").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/content/*").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/courses/*/sections").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/sections/*/lectures").permitAll()

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/admin/sections/*", "/api/v1/admin/sections/*/lectures/*").permitAll()

                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/sections/*/lectures/*/approve", "/api/v1/admin/sections/*/lectures/*/reject").permitAll()

                        .requestMatchers("/api/v1/admin/**").authenticated()

                        .anyRequest().permitAll())

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

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