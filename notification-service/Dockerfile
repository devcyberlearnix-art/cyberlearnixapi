# Stage 1: Build
FROM gradle:8.14.3-jdk21-alpine AS build
WORKDIR /app
COPY . .
RUN gradle :notification-service:clean :notification-service:build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Set IST timezone
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Kolkata /etc/localtime && \
    echo "Asia/Kolkata" > /etc/timezone

WORKDIR /app

# Copy Spring Boot jar from build stage
COPY --from=build /app/notification-service/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8093

ENTRYPOINT ["java", "-Duser.timezone=Asia/Kolkata", "-jar", "app.jar"]



