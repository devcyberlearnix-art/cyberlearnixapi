package com.swachvega.apigateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CyberLearnix API Gateway")
                        .version("1.0.0")
                        .description("Central API Gateway for CyberLearnix LMS - routes to all microservices:\n\n" +
                                "• **User Service** - Authentication, registration, profiles, and instructors\n" +
                                "• **Course Service** - Courses, sections, and lectures\n" +
                                "• **Cart Service** - Shopping cart management\n" +
                                "• **Coupon Service** - Discount coupons and promotions\n\n" +
                                "**Service Swagger UIs:**\n" +
                                "- User Service: `/userservice/swagger-ui/`\n" +
                                "- Course Service: `/courseservice/swagger-ui/`\n" +
                                "- Cart Service: `/cartservice/swagger-ui/`\n" +
                                "- Coupon Service: `/couponservice/swagger-ui/`")
                        .contact(new Contact()
                                .name("CyberLearnix API Team")
                                .email("api@cyberlearnix.com")
                                .url("https://cyberlearnix.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development"),
                        new Server()
                                .url("https://api.cyberlearnix.com")
                                .description("Production")));
    }
}
