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

                        // Public endpoints (sub-admin registration only)
                        .requestMatchers(
                                "/api/v1/admins/register",
                                "/api/v1/admin/register"
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
