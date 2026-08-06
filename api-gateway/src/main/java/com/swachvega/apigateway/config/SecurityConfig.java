package com.swachvega.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable) // Disable CSRF for REST APIs
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow browser preflight requests
                .anyExchange().permitAll() // Allow frontend API requests - JWT validation is handled by JwtAuthenticationFilter
            )
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) // Disable basic auth
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable) // Disable form login
            .build();
    }
}
