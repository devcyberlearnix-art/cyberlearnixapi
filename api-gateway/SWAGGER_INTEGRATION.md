# Swagger/OpenAPI Configuration for API Gateway

## Overview
The SwachVega API Gateway is configured to serve Swagger documentation for all microservices, including the authentication APIs from the User Service.

## Access Points

### Through API Gateway (Recommended)
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs**: `http://localhost:8080/v3/api-docs`
- **Auth APIs**: All endpoints accessible through gateway at `http://localhost:8080/api/consumer/auth/*`

### Direct Service Access (Development Only)
- **User Service Swagger**: `http://localhost:8081/swagger-ui/index.html`
- **User Service API Docs**: `http://localhost:8081/v3/api-docs`

## Configuration Details

### API Gateway Routes
The following routes are configured to handle Swagger documentation:

```yaml
# Authentication API routing
- id: authservice
  uri: http://userservice:8081
  predicates:
    - Path=/api/consumer/auth/**

# Swagger UI routing
- id: userservice-swagger-ui
  uri: http://userservice:8081
  predicates:
    - Path=/swagger-ui/**

# API Docs routing  
- id: userservice-api-docs
  uri: http://userservice:8081
  predicates:
    - Path=/v3/api-docs/**
```

### SpringDoc Configuration
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    config-url: /v3/api-docs/swagger-config
    urls:
      - url: /v3/api-docs
        name: API Gateway
      - url: http://localhost:8081/v3/api-docs
        name: User Service (Auth)
```

## Authentication APIs Available

### 1. Send OTP
- **Endpoint**: `POST /api/consumer/auth/send-otp`
- **Gateway URL**: `http://localhost:8080/api/consumer/auth/send-otp`
- **Description**: Send OTP to Indian mobile number

### 2. Verify OTP
- **Endpoint**: `POST /api/consumer/auth/verify-otp`
- **Gateway URL**: `http://localhost:8080/api/consumer/auth/verify-otp`
- **Description**: Verify OTP and determine login/registration flow

### 3. Complete Registration
- **Endpoint**: `POST /api/consumer/auth/complete-registration`
- **Gateway URL**: `http://localhost:8080/api/consumer/auth/complete-registration`
- **Description**: Complete user registration for new users

## Testing the Setup

### 1. Start Services
```bash
# Start User Service (Port 8081)
cd userservice && ./gradlew bootRun

# Start API Gateway (Port 8080)
cd apigateway && ./gradlew bootRun
```

### 2. Access Swagger UI
Open browser and navigate to: `http://localhost:8080/swagger-ui.html`

### 3. Test Authentication Flow
1. Use "Send OTP" endpoint with Indian mobile number
2. Use "Verify OTP" endpoint with received OTP
3. If new user, use "Complete Registration" endpoint

## Production Configuration

### Environment Variables
```bash
# API Gateway
GATEWAY_PORT=8080
USER_SERVICE_URL=http://userservice:8081

# User Service  
USER_SERVICE_PORT=8081
DATABASE_URL=jdbc:postgresql://localhost:5432/swachvega_users
```

### Domain Configuration
For production, update server URLs in OpenAPI configs:
- API Gateway: `https://api.swachvega.com`
- Documentation: `https://api.swachvega.com/swagger-ui.html`

## Security Considerations

### CORS Configuration
The API Gateway should be configured with proper CORS settings for frontend access:

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: 
              - "http://localhost:3000"  # React dev server
              - "https://app.swachvega.com"  # Production frontend
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
```

### Rate Limiting
Rate limiting is already configured in the gateway for authentication endpoints.

## Troubleshooting

### Common Issues

1. **Swagger UI not loading**
   - Check if both API Gateway and User Service are running
   - Verify routes are correctly configured
   - Check browser console for CORS errors

2. **API documentation not showing**
   - Ensure SpringDoc dependencies are added to both services
   - Verify OpenAPI configuration in User Service
   - Check if `/v3/api-docs` endpoint is accessible

3. **Authentication endpoints not working**
   - Verify User Service is running on port 8081
   - Check API Gateway routes configuration
   - Ensure database is running and accessible

### Debug Commands
```bash
# Check service health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health

# Check API docs
curl http://localhost:8080/v3/api-docs
curl http://localhost:8081/v3/api-docs

# Test authentication endpoint
curl -X POST http://localhost:8080/api/consumer/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "9876543210", "deliveryMethod": "SMS"}'
```

## Benefits of Gateway-based Swagger

1. **Centralized Documentation**: All APIs accessible from single endpoint
2. **Production Ready**: Proper routing and security in place
3. **Frontend Integration**: Single base URL for all API calls
4. **Load Balancing**: Can distribute traffic across multiple service instances
5. **Monitoring**: Centralized logging and metrics collection

The setup ensures that your authentication APIs are properly documented and accessible through the API Gateway while maintaining security and scalability.
