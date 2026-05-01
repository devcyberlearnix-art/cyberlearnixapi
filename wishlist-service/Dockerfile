# Stage 1: Build stage (Optional if you prefer building outside Docker)
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install tzdata to set the correct timezone (Hyderabad/Kolkata)
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Kolkata /etc/localtime && \
    echo "Asia/Kolkata" > /etc/timezone

# Copy the built JAR from the build stage or your local build folder
# Note: Ensure the JAR name matches your actual build output
COPY build/libs/*-SNAPSHOT.jar app.jar

# Your service runs on 8085 as per your Gateway config
EXPOSE 8085

# Performance tuning for Java 21 and Virtual Threads
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Duser.timezone=Asia/Kolkata", \
            "-Dspring.threads.virtual.enabled=true", \
            "-jar", "app.jar"]