package com.swachvega.apigateway.config;

import com.swachvega.apigateway.filter.RateLimitFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
@Slf4j
public class GatewayConfig {

    @Value("${rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter();
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .defaultHeader(HttpHeaders.ACCEPT, "application/json");
    }

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }
}
