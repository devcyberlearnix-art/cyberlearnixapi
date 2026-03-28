# SwachVega API Gateway - Docker Deployment

## Quick Start

1. **Copy Environment Template**
   ```bash
   cp .env.template .env
   ```

2. **Update Environment Variables**
   Edit `.env` file with your production values:
   ```bash
   # IMPORTANT: Update these JWT secrets for production!
   JWT_ACCESS_TOKEN_SECRET=your-very-strong-secret-key-minimum-32-characters
   JWT_REFRESH_TOKEN_SECRET=your-very-strong-secret-key-minimum-32-characters
   
   # Update CORS origins for your domain
   CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://admin.yourdomain.com
   ```

3. **Build and Start Services**
   ```bash
   # Build latest images
   docker-compose build
   
   # Start all services
   docker-compose up -d
   
   # View logs
   docker-compose logs -f apigateway
   ```

## Services Overview

| Service | Port | Description | Health Check |
|---------|------|-------------|--------------|
| **API Gateway** | 8080 | Main entry point with JWT auth | http://localhost:8080/actuator/health |
| **User Service** | 8081 | User management & authentication | http://localhost:8081/actuator/health |
| **PostgreSQL** | 5432 | Primary database | Internal health check |
| **Redis** | 6379 | Session & cache storage | Internal health check |
| **RedisInsight** | 5540 | Redis management UI | http://localhost:5540 |

## Production Configuration

### JWT Security
- **Access Token**: 15 minutes expiry (configurable)
- **Refresh Token**: 30 days expiry (configurable)
- **Algorithm**: HMAC-SHA256 with secure keys
- **Claims**: Rich user profile with permissions & features

### Network Security
- **CORS**: Configurable origins, methods, headers
- **Rate Limiting**: 100 requests/minute (configurable)
- **Health Checks**: All services monitored
- **Isolated Network**: Services communicate via Docker network

### Monitoring & Logging
- **Health Endpoints**: `/actuator/health` for all services
- **Log Volumes**: Persistent logging with rotation
- **Metrics**: Prometheus-compatible endpoints

## Environment Variables

### JWT Configuration
```bash
JWT_ACCESS_TOKEN_SECRET=         # Minimum 32 characters
JWT_REFRESH_TOKEN_SECRET=        # Minimum 32 characters
JWT_ACCESS_TOKEN_EXPIRATION_MINUTES=15
JWT_REFRESH_TOKEN_EXPIRATION_DAYS=30
```

### Security Settings
```bash
CORS_ALLOWED_ORIGINS=           # Comma-separated domains
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
CORS_ALLOWED_HEADERS=Authorization,Content-Type,X-Requested-With
RATE_LIMITING_ENABLED=true
RATE_LIMITING_REQUESTS_PER_MINUTE=100
```

### Service Configuration
```bash
USERSERVICE_URL=http://userservice:8080
PRODUCTSERVICE_URL=http://productservice:8080
ORDERSERVICE_URL=http://orderservice:8080
SPRING_PROFILES_ACTIVE=production
```

## Docker Commands

### Development
```bash
# Start services
docker-compose up -d

# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f apigateway
docker-compose logs -f userservice

# Restart a service
docker-compose restart apigateway

# Scale services (if needed)
docker-compose up -d --scale userservice=2
```

### Production
```bash
# Pull latest images
docker-compose pull

# Deploy with no downtime
docker-compose up -d --no-deps apigateway

# Health check all services
docker-compose ps
```

### Maintenance
```bash
# Backup volumes
docker run --rm -v swachvega_pgdata:/data -v $(pwd):/backup ubuntu tar czf /backup/pgdata-backup.tar.gz -C /data .

# Clean up
docker-compose down
docker system prune -f

# Reset all data (CAUTION!)
docker-compose down -v
```

## Troubleshooting

### Common Issues

1. **JWT Token Issues**
   ```bash
   # Check API Gateway logs
   docker-compose logs apigateway | grep JWT
   
   # Verify environment variables
   docker-compose exec apigateway env | grep JWT
   ```

2. **Service Connection Issues**
   ```bash
   # Check network connectivity
   docker-compose exec apigateway ping userservice
   
   # Verify service health
   curl http://localhost:8080/actuator/health
   curl http://localhost:8081/actuator/health
   ```

3. **Database Connection Issues**
   ```bash
   # Check PostgreSQL logs
   docker-compose logs postgres
   
   # Test database connection
   docker-compose exec postgres psql -U swachvega -d swachvegadb -c "SELECT 1;"
   ```

### Health Checks
```bash
# API Gateway
curl http://localhost:8080/actuator/health

# User Service
curl http://localhost:8081/actuator/health

# Test JWT Authentication
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" http://localhost:8080/api/users/profile
```

## Security Checklist

- [ ] Updated JWT secrets to strong, unique values
- [ ] Configured CORS for your specific domains
- [ ] Set appropriate rate limiting
- [ ] Updated default database passwords
- [ ] Configured HTTPS in production (via reverse proxy)
- [ ] Set up log monitoring and alerting
- [ ] Configured backup strategies for volumes

## API Testing

Once services are running, you can test the complete authentication flow:

```bash
# 1. Send OTP
curl -X POST http://localhost:8080/api/consumer/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+1234567890", "deliveryMethod": "SMS"}'

# 2. Verify OTP (get JWT tokens)
curl -X POST http://localhost:8080/api/consumer/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+1234567890", "otp": "123456", "otpSessionId": "session-id"}'

# 3. Use JWT token for authenticated requests
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  http://localhost:8080/api/consumer/auth/profile
```
