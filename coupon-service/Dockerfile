# Stage 1: Build
FROM gradle:8.14.3-jdk21-alpine AS build
WORKDIR /app
COPY . .
RUN gradle :coupon-service:clean :coupon-service:build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Set IST timezone
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Kolkata /etc/localtime && \
    echo "Asia/Kolkata" > /etc/timezone

WORKDIR /app

# Copy the built jar from the build stage
# Note: coupon-service is version 1.0.0-SNAPSHOT in build.gradle
COPY --from=build /app/coupon-service/build/libs/coupon-service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-Duser.timezone=Asia/Kolkata", "-jar", "app.jar"]



