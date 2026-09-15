package com.swachvega.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String[] allowedMethods;

    @Value("${cors.allowed-headers:Authorization,Content-Type,Accept,Origin,X-Requested-With,ngrok-skip-browser-warning}")
    private String[] allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private Boolean allowCredentials;

    @Value("${cors.max-age:3600}")
    private Long maxAge;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allow configured origins from environment
        List<String> origins = Arrays.asList(allowedOrigins);
        origins.forEach(corsConfig::addAllowedOrigin);
        
        // Add wildcard for ngrok in development if localhost is allowed
        if (origins.contains("http://localhost:3000") || origins.contains("http://localhost:*")) {
            corsConfig.addAllowedOriginPattern("https://*.ngrok-free.app");
            corsConfig.addAllowedOriginPattern("https://*.ngrok-free.dev");
            corsConfig.addAllowedOriginPattern("https://*.ngrok.io");
        }
        
        // Allow configured HTTP methods
        Arrays.asList(allowedMethods).forEach(corsConfig::addAllowedMethod);
        
        // Allow configured headers
        Arrays.asList(allowedHeaders).forEach(corsConfig::addAllowedHeader);
        
        // Allow credentials (cookies, authorization headers)
        corsConfig.setAllowCredentials(allowCredentials);
        
        // Cache preflight requests
        corsConfig.setMaxAge(maxAge);
        
        // Apply to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}