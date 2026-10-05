# Runtime - Use pre-built JAR with security and health check optimizations
FROM eclipse-temurin:21-jre-alpine

# Install dependencies and create non-root user
RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/Asia/Kolkata /etc/localtime && \
    echo "Asia/Kolkata" > /etc/timezone && \
    addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy the pre-built jar
COPY api-gateway/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-Duser.timezone=Asia/Kolkata", "-jar", "app.jar"]