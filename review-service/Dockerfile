# Stage 1: Build
FROM gradle:8.14.3-jdk21-alpine AS build
WORKDIR /app
COPY . .
RUN gradle :review-service:clean :review-service:build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Kolkata /etc/localtime && \
    echo "Asia/Kolkata" > /etc/timezone

WORKDIR /app

COPY --from=build /app/review-service/build/libs/review-service-1.0.0.jar app.jar

EXPOSE 8089

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Duser.timezone=Asia/Kolkata", \
            "-jar", "app.jar"]
