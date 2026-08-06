# Stage 1: Build
FROM gradle:8.14.3-jdk21-alpine AS build
WORKDIR /app
COPY . .
RUN gradle :api-gateway:clean :api-gateway:build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

# Set IST timezone (Debian)
RUN apt-get update && apt-get install -y tzdata && \
    ln -snf /usr/share/zoneinfo/Asia/Kolkata /etc/localtime && \
    echo "Asia/Kolkata" > /etc/timezone && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/api-gateway/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=Asia/Kolkata", "-jar", "app.jar"]



