package com.user.register.config;

import com.user.register.filter.RequestLoggingFilter;
import com.user.register.security.JwtAuthFilter;
import com.user.register.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Profile("disabled")
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthFilter jwtAuthFilter;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;
        private final RequestLoggingFilter requestLoggingFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .cors(Customizer.withDefaults())
                                .csrf(csrf -> csrf.disable())
                                .logout(logout -> logout.disable())

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // ✅ Critical: Allow SecurityContextHolder changes from JwtAuthFilter
                                // to be visible to the AuthorizationFilter without explicit save
                                .securityContext(sc -> sc.requireExplicitSave(false))

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api/v1/auth/register",
                                                                "/api/v1/auth/verify-email",
                                                                "/api/v1/auth/login/**",
                                                                "/api/v1/auth/refresh",
                                                                "/api/v1/auth/password/**",
                                                                "/api/v1/auth/logout",
                                                                "/api/v1/auth/switch-role",
                                                                "/oauth2/**",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**",
                                                                "/actuator/**",
                                                                "/h2-console/**",
                                                                "/error")
                                                .permitAll()

                                                .requestMatchers("/api/v1/users/me", "/api/v1/users/me/photo").authenticated()
                                                .requestMatchers("/api/v1/users/me/sessions/**").authenticated()
                                                .requestMatchers("/api/v1/instructors/applications").authenticated()
                                                .requestMatchers("/api/v1/instructors/applications/me").authenticated()
                                                .anyRequest().permitAll())

                                .oauth2Login(oauth -> oauth
                                                .loginPage("/login")
                                                .successHandler(oAuth2SuccessHandler))

                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        System.out.println("=== AUTHENTICATION ENTRY POINT CALLED ===");
                                                        System.out.println("Path: " + request.getRequestURI());
                                                        response.setStatus(
                                                                        jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                                                        response.setContentType("application/json");
                                                        response.getWriter().write(
                                                                        "{\"success\":false,\"message\":\"Unauthorized: "
                                                                                        + authException.getMessage()
                                                                                        + "\",\"data\":null}");
                                                }))

                                // ✅ Place filter before UsernamePasswordAuthenticationFilter (guaranteed to be
                                // in the chain)
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(requestLoggingFilter, org.springframework.security.web.context.SecurityContextPersistenceFilter.class);

                return http.build();
        }

        /**
         * Prevent JwtAuthFilter from being auto-registered as a servlet filter by
         * Spring Boot.
         * Without this, @Component causes it to run TWICE: once outside the Security
         * chain
         * (where SecurityContextHolderFilter hasn't run yet) and once inside. The
         * outside
         * execution's SecurityContext gets cleared by SecurityContextHolderFilter.
         */
        @Bean
        public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter filter) {
                FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
                registration.setEnabled(false); // Disable servlet auto-registration
                return registration;
        }

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
