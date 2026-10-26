package com.example.notification.config;

import com.cyberlearnix.error.ApiSecurityErrorWriter;
import com.cyberlearnix.logging.TraceIdFilter;
import com.example.notification.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CORS - handled by API Gateway
                .cors(cors -> cors.disable())
                // Disable CSRF (important for APIs)
                .csrf(csrf -> csrf.disable())

                // Disable default login form
                .formLogin(form -> form.disable())

                // Disable HTTP Basic
                .httpBasic(basic -> basic.disable())

                // Stateless session (JWT-based)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                ApiSecurityErrorWriter.write(request, response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "UNAUTHORIZED", "Authentication required"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                ApiSecurityErrorWriter.write(request, response, HttpServletResponse.SC_FORBIDDEN,
                                        "FORBIDDEN", "Access denied: insufficient permissions"))
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/announcements/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/announcements/**").authenticated()
                        .requestMatchers("/api/v1/users/**").authenticated()
                        .requestMatchers("/api/v1/push/**").permitAll()
                        .requestMatchers("/api/v1/notifications/**").authenticated()
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("MAIN_ADMIN", "SUB_ADMIN")
                        .anyRequest().authenticated()
                )

                .addFilterBefore(new TraceIdFilter(), SecurityContextHolderFilter.class)
                // Add JWT filter before Spring Security's auth filter
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
