# Gateway and Microservice Map

## API Gateway Configuration

### Gateway Details

- **Service Name**: api-gateway
- **Port**: 8080
- **Framework**: Spring Cloud Gateway
- **Configuration File**: `api-gateway/src/main/resources/application.yml`

### Gateway Base URL

**Development**: `http://localhost:8080`
**Production**: Configured via environment variable

### Gateway Routes

| Route ID | Service | Port | Gateway Path Predicate | Internal Service URL | Path Rewrite |
|----------|---------|------|------------------------|---------------------|--------------|
| courseservice-banners-admin | Course Service | 8083 | `/api/v1/admin/banners/**` | http://localhost:8083 | StripPrefix=0 |
| courseservice-banners-public | Course Service | 8083 | `/api/v1/banners/**` | http://localhost:8083 | StripPrefix=0 |
| adminservice | Admin Service | 8087 | `/api/v1/admin/**` | http://localhost:8087 | StripPrefix=0 |
| userservice-auth | User Service | 8091 | `/api/v1/auth/**` | http://localhost:8091 | StripPrefix=0 |
| userservice-oauth | User Service | 8091 | `/oauth2/**`, `/login/oauth2/**` | http://localhost:8091 | StripPrefix=0 |
| userservice-otp-resend | User Service | 8091 | `/api/v1/auth/otp/resend` | http://localhost:8091 | StripPrefix=0 |
| userservice-sessions | User Service | 8091 | `/api/v1/users/me/sessions/**` | http://localhost:8091 | StripPrefix=0 |
| userservice-users | User Service | 8091 | `/api/v1/users/**` | http://localhost:8091 | StripPrefix=0 |
| userservice-instructors | User Service | 8091 | `/api/v1/instructors/**` | http://localhost:8091 | StripPrefix=0 |
| courseservice-courses | Course Service | 8083 | `/api/v1/courses/**` | http://localhost:8083 | StripPrefix=0 |
| courseservice-upload | Course Service | 8083 | `/api/v1/upload/**` | http://localhost:8083 | StripPrefix=0 |
| courseservice-enrollments | Course Service | 8083 | `/api/v1/enrollments/**` | http://localhost:8083 | StripPrefix=0 |
| courseservice-dashboard | Course Service | 8083 | `/api/v1/dashboard/**` | http://localhost:8083 | StripPrefix=0 |
| courseservice-sections | Course Service | 8083 | `/api/v1/sections/**` | http://localhost:8083 | StripPrefix=0 |
| cartservice | Cart Service | 8081 | `/api/v1/cart/**` | http://localhost:8081 | StripPrefix=0 |
| couponservice | Coupon Service | 8082 | `/api/v1/coupons/**` | http://localhost:8082 | StripPrefix=0 |
| wishlistservice | Wishlist Service | 8090 | `/api/v1/wishlist/**` | http://localhost:8090 | StripPrefix=0 |
| orderservice | Order Service | 8084 | `/api/v1/orders/**` | http://localhost:8084 | StripPrefix=0 |
| paymentservice | Payment Service | 8085 | `/api/v1/payments/**` | http://localhost:8085 | StripPrefix=0 |
| paymentservice-test | Payment Service | 8085 | `/test/**` | http://localhost:8085 | StripPrefix=0 |
| instructorservice | Instructor Service | 8088 | `/api/v1/instructors/{id}/courses`, `/api/v1/instructors/{id}/courses/**`, `/api/v1/instructors/{id}/dashboard`, `/api/v1/instructors/{id}/earnings` | http://localhost:8088 | StripPrefix=0 |
| reviewservice | Review Service | 8089 | `/api/v1/reviews/**` | http://localhost:8089 | StripPrefix=0 |
| notificationservice | Notification Service | 8093 | `/api/v1/notifications/**` | http://localhost:8093 | StripPrefix=0 |
| notificationservice-system | Notification Service | 8093 | `/api/v1/system/**` | http://localhost:8093 | StripPrefix=0 |
| notificationservice-templates | Notification Service | 8093 | `/api/v1/templates/**` | http://localhost:8093 | StripPrefix=0 |
| notificationservice-preferences | Notification Service | 8093 | `/api/v1/preferences/**` | http://localhost:8093 | StripPrefix=0 |
| notificationservice-security | Notification Service | 8093 | `/api/v1/security/**` | http://localhost:8093 | StripPrefix=0 |

### Swagger Routes (Service-Level)

| Route ID | Service | Gateway Path | Internal Path |
|----------|---------|--------------|---------------|
| userservice-swagger-ui | User Service | `/userservice/swagger-ui/**` | `/swagger-ui/**` |
| userservice-api-docs | User Service | `/userservice/v3/api-docs/**` | `/v3/api-docs/**` |
| courseservice-swagger-ui | Course Service | `/courseservice/swagger-ui/**` | `/swagger-ui/**` |
| courseservice-api-docs | Course Service | `/courseservice/v3/api-docs/**` | `/v3/api-docs/**` |

### Gateway-Only Routes

| Route ID | Gateway Path | Purpose |
|----------|--------------|---------|
| apigateway-swagger-ui-html | `/swagger-ui.html` | Gateway Swagger UI HTML |
| apigateway-swagger-ui | `/swagger-ui/**` | Gateway Swagger UI resources |
| apigateway-api-docs | `/v3/api-docs/**` | Gateway OpenAPI docs |

## Microservice Configuration

### Service Inventory

| Service | Default Port | Config File | Database | Main Package |
|---------|--------------|-------------|----------|---------------|
| API Gateway | 8080 | application.yml | Redis (cache) | com.swachvega.apigateway |
| User Service | 8091 | application.properties | lms_user_db | com.user.register |
| Admin Service | 8087 | application.properties | lms_admin_db | com.example.admin |
| Course Service | 8083 | application.properties | lms_course_db | com.lms.courseservice |
| Instructor Service | 8088 | application.properties | lms_instructor_db | com.example.instructorservice |
| Cart Service | 8081 | application.properties | lms_cart_db | com.lms.cart_service |
| Coupon Service | 8082 | application.properties | lms_coupon_db | com.lms.coupon_service |
| Order Service | 8084 | application.properties | lms_order_db | com.lms.orderservice |
| Payment Service | 8085 | application.properties | lms_payment_db | com.lms.paymentservice |
| Review Service | 8089 | application.yml | lms_review_db | com.lms.review |
| Notification Service | 8093 | application.properties | lms_notification_db | com.example.notification |
| Wishlist Service | 8090 | application.yaml | lms_wishlist_db | com.lms.wishlist_service |

### Service Environment Variables

#### Database Variables (All Services)

```bash
DB_HOST=localhost
DB_PORT=15432
DB_USER=cyberlearnix
DB_PASSWORD=cyberlearnix123
SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
SPRING_DATASOURCE_USERNAME=${DB_USER}
SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
```

#### Redis Variables

```bash
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
SPRING_DATA_REDIS_PASSWORD=
```

#### JWT Variables (All Services)

```bash
JWT_SECRET=8c4e9d2f1a7b6c5d9e3f0a1b7c8d4e5f9a2b6c1d8e7f3a4b5c9d1e6f8a2b7c3
JWT_ISSUER=cyberlearnix
JWT_AUDIENCE=cyberlearnix-clients
```

#### Service-Specific URLs

```bash
USER_SERVICE_URL=http://localhost:8091
ADMIN_SERVICE_URL=http://localhost:8087
COURSESERVICE_URL=http://localhost:8083
INSTRUCTOR_SERVICE_URL=http://localhost:8088
CARTSERVICE_URL=http://localhost:8081
COUPONSERVICE_URL=http://localhost:8082
ORDERSERVICE_URL=http://localhost:8084
PAYMENTSERVICE_URL=http://localhost:8085
REVIEWSERVICE_URL=http://localhost:8089
NOTIFICATIONSERVICE_URL=http://localhost:8093
WISHLISTSERVICE_URL=http://localhost:8090
```

#### Gateway Variables

```bash
SPRING_PROFILES_ACTIVE=local
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS,PATCH
CORS_ALLOWED_HEADERS=Authorization,Content-Type,Accept,Origin,X-Requested-With,ngrok-skip-browser-warning
CORS_ALLOW_CREDENTIALS=true
CORS_MAX_AGE=3600
RATE_LIMIT_ENABLED=true
RATE_LIMIT_REQUESTS_PER_MINUTE=100
```

#### User Service Variables

```bash
SERVER_PORT=8091
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=cyberlearnix@gmail.com
SPRING_MAIL_PASSWORD=<app-specific-password>
APP_ENCRYPTION_KEY=1234567890123456
APP_UPLOAD_BASE_DIR=uploads/instructor-applications
SERVICE_AUTH_ENABLED=true
SERVICE_AUTH_SECRET=shared-service-secret-key-2024
SERVICE_AUTH_HEADER=X-Service-Auth
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

#### Course Service Variables

```bash
SERVER_PORT=8083
JPA_DDL_AUTO=update
SERVICE_AUTH_ENABLED=false
SERVICE_AUTH_SECRET=course-service-secret-key-2024
SERVICE_AUTH_HEADER=X-Service-Auth
```

#### Admin Service Variables

```bash
SERVER_PORT=8087
MAIN_ADMIN_EMAIL=dev.cyberlearnix@gmail.com
MAIN_ADMIN_PASSWORD=MainAdmin@123
MAIN_ADMIN_FIRST_NAME=Main
MAIN_ADMIN_LAST_NAME=Admin
MAIN_ADMIN_MOBILE=+919182602591
```

#### Payment Service Variables

```bash
PAYU_MERCHANT_KEY=XidEdD
PAYU_SALT=4JPMTxOzkJMchiHLrdFcSgkcXTuzx1Bd
PAYU_BASE_URL=https://secure.payu.in
PAYU_POSTSERVICE_PATH=/merchant/postservice
PAYU_PAYMENT_LINKS_BASE_URL=https://uatoneapi.payu.in
INVOICE_MERCHANT_NAME=ABC Technologies
INVOICE_MERCHANT_GSTIN=36ABCDE1234F1Z5
INVOICE_MERCHANT_EMAIL=support@abc.com
INVOICE_GST_RATE=0.18
PAYMENT_CALLBACK_BASE_URL=http://localhost:8085
```

#### Notification Service Variables

```bash
PORT=8093
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
TWILIO_ACCOUNT_SID=
TWILIO_AUTH_TOKEN=
TWILIO_WHATSAPP_FROM=whatsapp:+14155238886
APP_SECURITY_BASE_URL=https://cyberlearnix.com
NOTIF_RATE_MAX=3
NOTIF_RATE_WINDOW_MINUTES=15
```

## Service-to-Service Communication

### Internal Communication Pattern

Services communicate via:
1. **HTTP/REST with Feign Clients** (Spring Cloud OpenFeign)
2. **Kafka** (Event-driven for notifications)

### Service Authentication

Service-to-service authentication uses:
- **Header**: `X-Service-Auth`
- **Secret**: Shared secret key (configured per service)
- **Enabled**: Configurable via `SERVICE_AUTH_ENABLED`

### Known Service Dependencies

| Service | Depends On | Communication Method |
|---------|------------|---------------------|
| Admin Service | User Service, Course Service, Instructor Service, Order Service, Payment Service, Review Service, Cart Service, Coupon Service, Wishlist Service, Notification Service | HTTP/Feign |
| Course Service | Review Service, User Service | HTTP/Feign |
| Cart Service | Coupon Service, Instructor Service, Course Service, Order Service | HTTP/Feign |
| Review Service | Course Service, User Service | HTTP/Feign |
| Notification Service | Course Service | HTTP/Feign |
| Wishlist Service | Course Service, Cart Service | HTTP/Feign |

## Gateway Features

### CORS Configuration

CORS is handled programmatically via `CorsWebFilter` bean in `CorsConfig.java` (not via YAML globalcors).

### Rate Limiting

Rate limiting is enabled but implementation details need verification in gateway filter code.

### JWT Authentication Filter

Gateway uses `JwtAuthenticationFilter` to:
- Validate JWT tokens on protected routes
- Extract user context (userId, role, email)
- Pass headers to downstream services
- Blacklist expired/revoked tokens

### File Upload Configuration

Gateway level:
- Max file size: 10MB
- Max request size: 10MB

Service level (varies):
- Course Service: 500MB (videos)
- User Service: 25MB (instructor docs), 10MB (profile photos)
- Admin Service: 5MB (banner images)

## Missing or Inconsistent Routes

### Known Issues

1. **Instructor Service Route Limitation**: Gateway only routes specific instructor paths:
   - `/api/v1/instructors/{id}/courses`
   - `/api/v1/instructors/{id}/courses/**`
   - `/api/v1/instructors/{id}/dashboard`
   - `/api/v1/instructors/{id}/earnings`

   Other instructor endpoints in the service may not be accessible through the gateway.

2. **User Service Instructor Routes**: The gateway routes `/api/v1/instructors/**` to User Service, but Instructor Service has its own controllers. This may cause routing conflicts or confusion.

3. **Banner Routes**: Banner admin routes go to Course Service (`/api/v1/admin/banners/**`), while general admin routes go to Admin Service (`/api/v1/admin/**`). The banner route must come before the general admin route to work correctly.

### Unverified Routes

The following route predicates exist but need verification:
- Notification Service security verification routes (`/api/v1/security/**`)
- Payment Service test routes (`/test/**`)

## Dependencies Required to Run APIs

### External Dependencies

1. **PostgreSQL Database**: Port 15432 (or configured port)
2. **Redis**: Port 6379
3. **Kafka**: Port 9092 (for user login events)
4. **SMTP Server**: Gmail SMTP (for OTP emails)
5. **Google Drive API**: For file storage (service account credentials)
6. **PayU Payment Gateway**: For payment processing
7. **Twilio**: For WhatsApp notifications (optional)

### Internal Dependencies

All services must be running for full functionality:
- API Gateway (required entry point)
- User Service (required for auth)
- Course Service (required for course catalog)
- Cart Service (required for shopping)
- Order Service (required for orders)
- Payment Service (required for payments)
- Admin Service (required for admin operations)
- Other services as needed (Review, Notification, Wishlist, Instructor, Coupon)

## Gateway Configuration File Location

`C:\Users\SHIVASAI\OneDrive\Desktop\LMS\api-gateway\src\main\resources\application.yml`

## Service Configuration Files

| Service | Config File Path |
|---------|------------------|
| User Service | `user-service/src/main/resources/application.properties` |
| Admin Service | `admin-service/src/main/resources/application.properties` |
| Course Service | `course-service/src/main/resources/application.properties` |
| Instructor Service | `instructor-service/src/main/resources/application.properties` |
| Cart Service | `cart-service/src/main/resources/application.properties` |
| Coupon Service | `coupon-service/src/main/resources/application.properties` |
| Order Service | `order-service/src/main/resources/application.properties` |
| Payment Service | `payment-service/src/main/resources/application.properties` |
| Review Service | `review-service/src/main/resources/application.yml` |
| Notification Service | `notification-service/src/main/resources/application.properties` |
| Wishlist Service | `wishlist-service/src/main/resources/application.yaml` |
