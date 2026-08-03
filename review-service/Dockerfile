# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY review-service/pom.xml review-service/pom.xml
COPY review-service/src review-service/src
WORKDIR /app/review-service
RUN mvn -B package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Kolkata /etc/localtime && \
    echo "Asia/Kolkata" > /etc/timezone

WORKDIR /app

COPY --from=build /app/review-service/target/review-service-1.0.0.jar app.jar

EXPOSE 8089

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Duser.timezone=Asia/Kolkata", \
            "-jar", "app.jar"]
