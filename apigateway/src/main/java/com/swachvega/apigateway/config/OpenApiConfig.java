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
                        .title("SwachVega API Gateway")
                        .version("1.0.0")
                        .description("Central API Gateway for SwachVega platform - routes to all microservices:\n\n" +
                                "• **User Service** - Authentication, user management, and profiles\n" +
                                "• **Product Service** - Product catalog, categories, and details\n" +
                                "• **Order Service** - Order processing, cart, and checkout\n" +
                                "• **Inventory Service** - Stock management and availability\n" +
                                "• **Search Service** - Product search, autocomplete, and recommendations\n" +
                                "• **Store Service** - Store locations, hours, and details\n\n" +
                                "**Service Access:**\n" +
                                "- User Service: `/userservice/swagger-ui/`\n" +
                                "- Product Service: `/productservice/swagger-ui/`\n" +
                                "- Order Service: `/orderservice/swagger-ui/`\n" +
                                "- Inventory Service: `/inventoryservice/swagger-ui/`\n" +
                                "- Search Service: `/searchservice/swagger-ui/`\n" +
                                "- Store Service: `/storeservice/swagger-ui/`")
                        .contact(new Contact()
                                .name("SwachVega API Team")
                                .email("api@swachvega.com")
                                .url("https://swachvega.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development API Gateway"),
                        new Server()
                                .url("https://api.swachvega.com")
                                .description("Production API Gateway")));
    }
}
