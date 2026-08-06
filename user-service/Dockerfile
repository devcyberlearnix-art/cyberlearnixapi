# Stage 1: Build

FROM gradle:8.14.3-jdk21-alpine AS build

WORKDIR /app

COPY . .

RUN gradle :user-service:clean :user-service:build -x test --no-daemon



# Stage 2: Runtime

FROM eclipse-temurin:21-jre-alpine



# Set IST timezone

RUN apk add --no-cache tzdata && \

    cp /usr/share/zoneinfo/Asia/Kolkata /etc/localtime && \

    echo "Asia/Kolkata" > /etc/timezone



WORKDIR /app



# Copy the built jar from the build stage

# Note: we exclude -plain.jar to ensure we get the boot jar

COPY --from=build /app/user-service/build/libs/*-SNAPSHOT.jar app.jar



EXPOSE 8086



# JVM memory settings and timezone

ENTRYPOINT ["java", "-Duser.timezone=Asia/Kolkata", "-jar", "app.jar"]




