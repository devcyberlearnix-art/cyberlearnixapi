package com.swachvega.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages = {
        "com.swachvega.commonlibs.entity"
})
@ComponentScan(basePackages = {
        "com.swachvega.commonlibs",
        "com.swachvega.apigateway"
})
public class ApiGatewayApplication {
    // SwachVega API Gateway - v1.0.1
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}