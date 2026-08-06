package com.lms.paymentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for RestTemplate to make REST calls to other services
 * Used by EnrollmentClient to communicate with Course Service (Port 8083)
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Create RestTemplate bean for service-to-service communication
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
