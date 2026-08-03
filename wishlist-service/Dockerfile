# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew :wishlist-service:clean :wishlist-service:build -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Set IST timezone
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Kolkata /etc/localtime && \
    echo "Asia/Kolkata" > /etc/timezone

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/wishlist-service/build/libs/wishlist-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8085

# Performance tuning for Java 21 and Virtual Threads
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Duser.timezone=Asia/Kolkata", \
            "-Dspring.threads.virtual.enabled=true", \
            "-jar", "app.jar"]