package com.swachvega.apigateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Explicitly disable static resource handling for API paths
        // This prevents Spring WebFlux from treating API requests as static resource requests
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
