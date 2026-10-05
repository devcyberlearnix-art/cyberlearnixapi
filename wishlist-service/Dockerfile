# Runtime - Use pre-built JAR with security and health check optimizations
FROM eclipse-temurin:21-jre-alpine

# Install dependencies and create non-root user
RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/Asia/Kolkata /etc/localtime && \
    echo "Asia/Kolkata" > /etc/timezone && \
    addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy the pre-built jar
COPY wishlist-service/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8090

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8090/actuator/health || exit 1

# Run the application with existing JVM optimizations
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Duser.timezone=Asia/Kolkata", \
            "-Dspring.threads.virtual.enabled=true", \
            "-jar", "app.jar"]